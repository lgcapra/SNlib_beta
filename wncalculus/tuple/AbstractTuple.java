package wncalculus.tuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import wncalculus.classfunction.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.*;
import wncalculus.util.Util;

/**
 * this class represents an arbitrary tuple of class-functions, that may be
 * both boolean functions or linear combinations
 * @author lorenzo capra
 * @param <E> the tuple's elements' type (either BoolFunction or BagFunction)
 */
public abstract class AbstractTuple<E extends ClassFunction, F extends ParametricExpr> implements Transposable, GuardedExpr<F>  {
    //careful: we assume that the tuple's components color-classes are consistent: c1.equals(c2) <-> c1.compareTo(c2) (i.e. different colors must have different names)
    private          SortedMap<ColorClass, List<? extends E>> hom_parts ; // the map between colors and homogenous sub-tuples composing this tuple 
    private          Guard   filter,   guard; //null means true!
    private final    Domain  codomain, domain; // can be different from one another - the codomain is inferred by the tuple form
    //cache-efficiency fields
    private String   str; 
    private List<? extends E>  components;
    private boolean  simplified;
        
    /**
     * base constructor (the only that should be used from outside the library at parsing time):
     * builds a tuple from a list of class-functions; functions are grouped by colour, only the relative
     * order of functions of the same color actually matters, the order among different colours (based non names)
     * just allows for an easier, "standard" (in some sense, canonical) descritpiom;
     * what is important, the tuple's codomain is inferred; either the tuple's domain or the tuple's guard must be specified,
     * otherwise a NullPointerException is raised; if the specified filter (guard) values(s) is (are) null, then
     * a trivial filter (guard) is (are) associated with this tuple
     * projection or_indexes occurring on the tuple must correctly range over the corresponding
     * colour bounds of the domain, otherwise an exception is raised
     * creates an unmodifiable data structures so any attempt to modify the tuple will raise an exception 
     * @param f the specified tuple'fc filter
     * @param g the specified tuple'fc guard
     * @param l the specified list of class-functions 
     * @param dom the tuple'fc domain
     * @param check domain check flag
     * @throws NullPointerException if both the given domain and guard are <tt>null</tt>
     * @throws IllegalDomain if the domain doesn't match the guard, or some variable index
     * is out of the tuple range
     */
    public AbstractTuple (Guard f, List<? extends E> l, Guard g, Domain dom, boolean check) {
        if ( dom == null && g == null )  
            throw new NullPointerException("either the domain or the guard of a tuple must be specified!");
        
        if (dom == null) 
            dom = g.getDomain();
        else if ( g != null && !dom.equals(g.getDomain()) ) 
            throw new IllegalDomain (" guard and tuple domains are incompatible!");
        // tuple's co-domain is derived        
        /*Sorted*/HashMap<ColorClass, Integer> cd = new /*Tree*/HashMap<>(); // the codomain's structure
        SortedMap<ColorClass , List<? extends E>> map = Util.sortedmapFeatureToList(l, ClassFunction::getSort);
        for (Map.Entry<ColorClass, List<? extends E> > entry  : map.entrySet()) {
            List<? extends E>    st = entry.getValue();
            ColorClass cc = entry.getKey();
            if (check) {
                Set<? extends Integer> idxset = ClassFunction.indexSet(st); // the projection or_index set of st
                if ( ! idxset.isEmpty() && Collections.max(idxset)  > dom.mult(cc) )
                    throw new IllegalDomain("failed tuple's building:\nincorrect domain specification: projection index outside the range of color "+cc+
                        "\ntuple components: "+st+", domain: "+dom);
            }
            cd.put(cc, st.size());
        } 
        
        this.codomain = new Domain(cd);
        if (check && f != null && ! this.codomain.equals(f.getDomain())) {
            System.err.println("lista funzioni: "+l+"\nfiltro: "+f.toStringDetailed());
            throw new IllegalDomain (this.codomain+" and "+f.getDomain()+ ": (co)domains are incompatible!");
        }
        
        this.hom_parts = Collections.unmodifiableSortedMap(map);
        this.domain = dom;
        setGuard(g);
        setFilter(f); 
    }
    
    /**
     * efficiently builds a tuple from a map of colors to corresponding class-function lists;
     * it doesn't perform any check and true copy, it provides an unmodifiable view of the passed map
     * @param filter the tuple'fc filter (<code>null</code> means TRUE)
     * @param codomain the tuple'fc codomain (necessary only if filter is <code>null</code>)
     * @param map the specified map
     * @param guard the tuple'fc guard (<code>null</code> means TRUE)
     * @param domain the tuple'fc domain (necessary only if guard is <code>null</code>)
     */
    public AbstractTuple (Guard filter, Domain codomain, SortedMap<ColorClass, List<? extends E>> map, Guard guard, Domain domain) {
        if (filter != null)
            this.codomain  = filter.getDomain();
        else if (codomain != null)
              this.codomain  = codomain ;
        else 
            throw new IllegalDomain("canno built a tuple with null codomain!");
        
        if (guard != null)
            this.domain  = guard.getDomain();
        else if (domain != null)
              this.domain  = domain ;
        else 
            throw new IllegalDomain("canno built a tuple with null domain!");
        
        setFilter(filter);
        setGuard(guard);
        this.hom_parts = map;
    }
    
    
    private void setFilter(Guard filter) {
        if (! (filter instanceof True) )
            this.filter =filter;
    }
    
    private void setGuard(Guard guard) {
        if (! ( guard instanceof True) )
            this.guard = guard;
    }    
    
    /**
     * builder method: build a tuple with the same components as <tt>this</tt>
     * @param filter tuple's  filter
     * @param guard tuple's guard
     * @param domain tuple's domain
     * @return a tuple with the same components as <tt>this</tt> and the specified
     * filter, guard, domain; <tt>this</tt> tuple if the new filters coincide with the
     * current ones
     */
    public abstract AbstractTuple<E,F> build (Guard filter, Guard guard, Domain domain);    
    
    
    /**
     * builds a tuple with the same (co-)domain and components as <code>this</code> tuple
     * it performs no consistency check 
     * @param filter the possibly null tuple'fc filter
     * @param map the tuple'fc components map
     * @param guard the possibly null tuple'fc guard
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    @Override
    public final /*AbstractTuple<E,*/F/*>*/ build(Guard filter, Guard guard) {
    	return build(filter, guard, this.domain).cast();
    }
    
    /**
     * builds a tuple with the same (co-)domain, components, and guards
     * as <code>this</code> tuple
     * it performs no consistency check 
     * @param filter the possibly null tuple'fc filter
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    public final F build(Guard filter) {
    	return build(filter, this.guard);
    }
    
    /**
     * 
     * @param f filter tuple's  filter
     * @param cd tuple's co-domain
     * @param m tuple's color components' map
     * @param g tuple's guard
     * @param d  tuple's domain
     * @return a tuple with specified elements
     */
    public abstract AbstractTuple<E,F> build (Guard f, Domain cd, SortedMap<ColorClass, List<? extends E>> m, Guard g, Domain d) ;
    
    @Override
    public final Domain getDomain() {
        return this.domain;
    }

    @Override
    public final Domain getCodomain() {
        return this.codomain;
    }
    
    /**
     *
     * @return the (possibly null, i.e., trivial) tuple's guard
     */
    @Override
    public final Guard guard() {
        return this.guard;
    }
    
    /**
     *
     * @return the (possibly null, i.e., trivial) tuple's filter
     */
    @Override
    public final Guard filter() {
        return this.filter;
    }
    
    /** 
     * @return <tt>true</tt> if and only if the filter and the guard are
     * the constant true
     */
    public final boolean hasTrivialFilters() {
        return this.guard == null && this.filter == null;
    }
    
    /**
     * @return the unique color class, if the tuple is 1-sorted; <code>null</code> otherwise
     */
    public final ColorClass getSort() {
        return this.hom_parts.size() == 1 ? this.hom_parts.keySet().iterator().next() : null;           
    }
    
    /**
     * @return a view of the color-homogeneous parts of this tuple 
     */
    public final SortedMap<ColorClass, List<? extends E>> getHomSubTuples() {
        return this.hom_parts;
    }
    
    /**
     * @param cc a color class
     * @return (a read-only) sub-list of components of the specified color class;
     * an empty list if there is no such a sub-list
     */
    public final List<? extends E> getHomSubTuple(ColorClass cc) {
        return this.hom_parts.getOrDefault(cc, Collections.emptyList());
    }
   
    /**
     * @return (an unmodifiable view of) the tuple components;
     * the list is ordered w.r.t. colour-classes
     */
    public final List<? extends E> getComponents() {
        if (this.components == null) {
            ColorClass c = getSort();
            if (c != null) 
                this.components = this.hom_parts.get(c);
            else {
                List<E> mycomps = new ArrayList<>();
                this.hom_parts.entrySet().forEach(x -> {mycomps.addAll(x.getValue()); });
                this.components = Collections.unmodifiableList(mycomps);
            }
        }
        
        return this.components;
    }
            
    /**
     @param i a relative position
     @param cc a color class
     @return the i-th component of the homogenous sub-tuple of the specified color
     @throws IndexOutOfBoundsException if the position is out of the correct range
     */
    public final E getComponent(int i, ColorClass cc) {
        return getHomSubTuple(cc).get(i - 1);
    }     
        
    @Override
    public final boolean isConstant() {
        return ClassFunction.indexSet(getComponents()).isEmpty() && (this.guard == null || this.guard.isConstant());
     }
            
    /**
     * applies a filter/guard to <tt>this</tt> tuple, by joining with the existing ones
     * if either of the specified filter/guard is <tt>null</tt> then it is ignored
     * @param f a filter
     * @param g a guard
     * @return a tuple with the new filter/guard;
     * <tt>this</tt> tuple if the new filter coincides with the current one
     */
    public final AbstractTuple<E,F>  embedBetween(Guard f , Guard g) {
        return build(GuardedExpr.join(this.filter, f), GuardedExpr.join(this.guard, g), this.domain) ;
    }
    
    
    /**
     * efficient overriding of the single-argument clone methods
     * @param nd the tuple's new domain
     * @return a copy of <tt>this</tt> tuple with new domain, assumed compliant with ("including")
     * the current domain
     */
    @Override
    public final AbstractTuple<E,F> clone(Domain nd) {
        return nd.equals(getDomain()) ? this : build (filter(), guard() != null ? guard().clone(nd) : null, nd);
    }
    
    @Override
    public final String toString () {
        if (this.str == null) {
            String t = "<";
            t = getComponents().stream().map( x -> x.toString() + ',').reduce(t, String::concat);
            t = t.substring(0,t.length()-1)+'>';
            this.str = (filter == null ? "" : "[" + filter + ']')  + t + (guard == null ? "" : "[" +guard + ']');
        }
        
        return this.str;
    }
    
    @Override
    public final boolean equals(Object o) {
        boolean res = super.equals(o);
        if (! res && o != null && getClass().equals( o.getClass() ) )  {
            AbstractTuple<?,?> t = (AbstractTuple<?,?>)o;
            res =   Objects.equals(t.guard,this.guard) && Objects.equals(t.filter,this.filter) && Objects.equals(t.hom_parts,this.hom_parts);
        }
        
        return res;
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.hom_parts);
        hash = 97 * hash + Objects.hashCode(this.filter);
        hash = 97 * hash + Objects.hashCode(this.guard);
        
        return hash;
    }
    
    @Override
    public final boolean simplified() {
        return this.simplified; 
    }

    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
        
    /**
     * performs the "difference" between guards knowing that eg == null means eg == true
     * p2 is assumed other than true (null)
     * @param p1 a guard
     * @param p2 another guard
     * @return the difference between guards
    */
    public static Guard subtr (Guard p1, Guard p2) {
        return p1 == null ?  Neg.factory(p2) : And.factory(p1, Neg.factory(p2));
    }
    
    /**
     * performs the "or" between guards knowing that eg == null means eg == true
     * @param p1 a guard
     * @param p2 another guard
     * @return the "OR" between guards 
    */
    public static Guard disjoin(Guard p1, Guard p2) {
        return p1 == null || p2 == null ? null : Or.factory(false, p1,p2);
    }
    
    //in alternativa si potrebbe definire come specSimplify
    @Override
    public F specSimplify( ) {
    	boolean changed = false;
        SortedMap<ColorClass, List<E>> tuplecopy = new TreeMap<>(); //si potrebbe ottimizzare
        for (Entry<ColorClass, List<? extends E>> x: getHomSubTuples().entrySet()) {
            ColorClass c = x.getKey();
            ArrayList<E> args_c = new ArrayList<>(x.getValue());
            Expression f_null = args_c.get(0).nullExpr(); //the null expression for tuple components
            for (ListIterator<E> ite = args_c.listIterator() ; ite.hasNext(); ) {
                E e = ite.next(), normal = e.normalize().cast() ;
                if (normal.equals(f_null))
                    return nullExpr().cast();
                
                if ( ! e.equals(normal) ) { 
                    ite.set( normal );
                    changed = true;
                }
            }
            
            if (guard != null)
                for (Equality eq: guard.equalityMap().getOrDefault(c, Collections.emptyMap()).getOrDefault(true, Collections.emptySortedSet()))
                    if (ClassFunction.replace(args_c, eq)) 
                       changed = true;

            tuplecopy.put(c, args_c);
        }

        if (changed) // some  tuple component have been reduced ..
            return build(filter, getCodomain(), Collections.unmodifiableSortedMap(tuplecopy), guard, getDomain()).cast();

        //if (! (Objects.equals(this.filter ,filter()) && Objects.equals(this.guard, guard() )) ) // the filter or the guard have been reduced ..
            //return build(this.filter,this.guard); //optimization (more efficient than previous bild ..)
        return cast();
    }
    
     /** @return the split-delimiters map of <code>this</code> tuple, separately considering
      * (in the order) the filters and the tuple'fc components
     */
    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        Guard g = guard(), f = filter();
        Map<Sort,Integer>  delims = g != null ? g.splitDelimiters() : new HashMap<>();
        if ( delims.isEmpty()) {
            if (f != null && ! f.isConstant() ) 
                delims = f.splitDelimiters();
            if (delims.isEmpty()) { //tuple components are considered
                SortedMap<ColorClass, List<? extends E>> m = getHomSubTuples();
                for ( Map.Entry<ColorClass, List<? extends E>> x : m.entrySet()) 
                    ColorClass.setDelim(delims, x.getKey(), ClassFunction.splitDelim(x.getValue(), x.getKey()));
            }
        }
        //System.out.println("split delimiters di "+this+ " "+delims); //debug
        return delims;
    }
    
    @Override
    public final ParametricExpr clone(Domain newdom, Domain newcd) {
        Guard c_g  =  guard(), c_f = filter();
        if (c_g != null)
            c_g  = (Guard) c_g.clone(newdom, null);
        if (c_f != null)
            c_f  = (Guard) c_f.clone(newcd, null);
        
        TreeMap<ColorClass, List<? extends E> > m = new TreeMap<>();
        //inefficiente,va sistemato!
        getHomSubTuples().entrySet().forEach( x -> {
            ColorClass cc = x.getKey();
            if (newcd.mult(cc) != 0) //color cc is present in newcd
                m.put(cc, x.getValue());
            else {
                ColorClass newcc = (ColorClass)newcd.sort(cc.name());
                m.put(newcc, (List<? extends E>) ClassFunction.copy(x.getValue(),newcc));
            }
        });
        
        return build (c_f, newcd, m, c_g, newdom);
    }   
    
    //in alternativa potrebbe ritornare una tupla senza filtro/guardia
    @Override
    public final F expr() {
        return null;
    }
    
    public final boolean isTuple() {
        return true;
    }
    
    /**
     * factorizes common parts of the composition between tuples
     * assumes that <tt>this</tt> tuple is without filter
     * @param right the tuple to compose with <tt>this</tt>
     * @return the result of common tuple-composition reductions
     */
    protected F tupleCompose (final AbstractTuple<E,F> right)  {
        if (guard == null)
            return cast();
        
        try {
            Constructor<? extends CompositionOp<F>> constructor = tkComp().getConstructor(type(), type());
            return constructor.newInstance(withoutGuard(), right.build(GuardedExpr.join(guard, filter))).cast(); //this tuple's guard moved into the right's filter
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(AbstractTuple.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error();
        }
    }
    
    /**
     * 
     * @return the map of color-homogeneous sub-tuples (of the same domain)
     * composing <tt>this</tt> tuple;
     * an empty map, if the tuple's filter is neither null nor an elementary-and-form
     */
    public HashMap<ColorClass, AbstractTuple<E,F>> subTuples() {
        HashMap<ColorClass, AbstractTuple<E,F>> subtuples = new HashMap<>();
        if (filter == null) {
            getHomSubTuples().entrySet().forEach(tuple -> {
                ColorClass cc = tuple.getKey();
                List<? extends E> comp = tuple.getValue();
                subtuples.put(cc, build(null, new Domain(cc,comp.size()), Util.singleSortedMap(cc, comp), guard, null));
            });
        }
        else if (filter instanceof ElementaryGuard) {
           getHomSubTuples().entrySet().forEach(tuple -> {
                ColorClass cc = tuple.getKey();
                List<? extends E> comp = tuple.getValue();
                Domain dom = new Domain(cc,comp.size());
                subtuples.put(cc, build(cc.equals(((ElementaryGuard)filter).getSort()) ? filter.clone(dom): null, dom, Util.singleSortedMap(cc, comp), guard, null));
            });
        } 
        else if (filter.isElemAndForm()) {
            getHomSubTuples().entrySet().forEach(tuple -> {
                ColorClass cc = tuple.getKey();
                List<? extends E> comp = tuple.getValue();
                Domain dom = new Domain(cc,comp.size());
                Map<Boolean, SortedSet<Equality>> emap = (Map<Boolean, SortedSet<Equality>>)ParametricExpr.cloneMapExpr(filter.equalityMap().get(cc), dom, Equality.class);
                Map<Boolean, HashSet<Membership>> mmap = (Map<Boolean, HashSet<Membership>>)ParametricExpr.cloneMapExpr(filter.membershipMap().get(cc), dom, Membership.class);
                Guard nf = And.factory(cc, emap, mmap);
                subtuples.put(cc, build(nf, dom, Util.singleSortedMap(cc, comp), guard, null));
            });
        }
        return subtuples;
    }
    
    
    public abstract Class<? extends CompositionOp<F>> tkComp();
    
    
}
