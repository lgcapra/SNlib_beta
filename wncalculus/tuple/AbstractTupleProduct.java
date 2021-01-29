package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.ClassFunction;
import wncalculus.expr.*;
import wncalculus.color.ColorClass;
import wncalculus.guard.And;
import wncalculus.guard.Guard;
import wncalculus.util.Util;
import wncalculus.wnbag.SNfunctionTuple;

/**
 * This class defines a kind of tuple juxtaposition operator.
 * Tuple-arguments must all have the same domain, single-coloured and pairwise-disjoint codomains.
 * @author Lorenzo Capra
 */
public abstract class AbstractTupleProduct<F extends ClassFunction, E extends SNfunctionTuple<F>> implements N_aryOp<E>  {
    
    private final List<E>  tuples;
    private final Domain   codom;
    private boolean simplified;
    
    /*
    base constructor
    */
    public AbstractTupleProduct (List<? extends E> tuples, boolean check)  {
        //if (check)
            Expressions.checkDomain(tuples);
        this.codom  = buildCodomain( tuples );
        this.tuples =  Collections.unmodifiableList(tuples);
    }
     
    @Override
    public abstract E buildOp(Collection<? extends E > args) ;
    
    //@Override
    //public abstract E cast();
    
    /** 
     *infers the juxtaposition co-domain from its argument list
     */
    private Domain buildCodomain( List<? extends E> tuples) {
        HashMap<Sort,Integer> d = new HashMap<>();
        tuples.forEach( ft -> { 
            Map<? extends Sort, Integer> cd = ft.getCodomain().asMap();
            if (cd.size() == 1) {
                Map.Entry<? extends Sort, Integer> e = cd.entrySet().iterator().next();
                if (d.putIfAbsent(e.getKey(), e.getValue()) != null) 
                    throw new IllegalDomain("tuple product: many tuples of the same colour are present:\n"+tuples);
            }
            else
                 throw new IllegalDomain("tuple product: many-coloured tuples are present:\n"+tuples);
        });
        
        return new Domain(d);
    }
    
    @Override
    public final List<E> getArgs() {
        return this.tuples;
    }
    

    @Override
    public final Domain getCodomain() {
        return this.codom;
    }
    
    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        return ColorClass.mergeSplitDelimiters(getArgs());
    }

    
    /**
     * overrides the super-type method because the operands codomains are restrictions
     * of <tt>this</tt> term's codomain
     * @param newdom the new domain
     * @param newcd  the new codomain
     * @param smap the color split-map
     * @return a clone of <tt>this</tt> with the specified co-domains
     */
    @Override
    public final E clone (final Domain newdom, final Domain newcd) {
        List<E> cloned_tuples = new ArrayList<>();
        this.tuples.forEach(tuple -> {
            Domain old_cd = tuple.getCodomain();
            Sort cc = old_cd.support().iterator().next(); //the sub-tuple is mono-coloured
            int m = newcd.mult(cc); // m is 0 if cc is not present in newcd
            cloned_tuples.add(tuple.clone(newdom,  m != 0 ? old_cd : new Domain(newcd.sort(cc.name()), m)).cast() );
        });
        
        return buildOp(cloned_tuples);
    }

    @Override
    final public E specSimplify() { 
       if (Util.checkAny(this.tuples, p -> p.isNull()))
           return nullExpr().cast(); 
       
       if (Util.checkAll(this.tuples , SNfunctionTuple::isTuple) ) { // the are only tuples (non extended) ..
           Domain cd = getCodomain(), d = getDomain();
           SortedMap<ColorClass, List<? extends F> > m = new TreeMap<>();
           Set<Guard> g = new HashSet<>(), f = new HashSet<>();
           AbstractTuple<F,E> t = null;
           for (E x : this.tuples ) {
               t = x.asTuple();
               m.put(t.getSort(), t.getComponents());
               if (t.filter() != null)  //optimization
                   f.add(t.filter().clone(cd));  // the domains of the inner filters are different from the terms's codomain...
               if (t.guard() != null) 
                   g.add(t.guard());
           }
           
           return t.build(And.buildAndForm(f), cd, m, And.buildAndForm(g, d), d).cast();
       }
       
       int i = Util.indexOf( this.tuples, p -> p.isElementarySum() );
       return i < 0 ? cast() : expand (Util.indexOf(this.tuples, p -> p.isElementarySum())) ;
    }
    
    /**
     * expand a product with respect to an inner "elementary sum"
     * @param inner_sum the inner sum's index
     * @return the corresponding expanded product if the index is geq 0,
     * <tt>this</tt> otherwise
     */
    protected abstract E expand(int inner_sum );

    @Override
    public final boolean isConstant() {
        return this.tuples.stream().noneMatch(t -> !t.isConstant());
    }

    @Override
    public final String symb() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
     @Override
    public final boolean equals(Object o) {
        return N_aryOp.super.isEqual(o);
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.tuples.hashCode();
        return hash;
    }
    
    @Override
    public final String toString () {
        String res = "<";
        res = this.tuples.stream().map(x -> x.toString() + ';').reduce(res, String::concat);
        
        return res.substring(0,res.length()-1)+'>';
    }

    @Override
    public final E getIde() {
        return null;
    }

    @Override
    public final boolean simplified() {
        return this.simplified;
    }

    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
}
