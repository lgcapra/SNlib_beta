package wncalculus.wnbag;

import java.util.*;
import java.util.Map.Entry;
import wncalculus.classfunction.ElementaryFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.And;
import wncalculus.guard.Equality;
import wncalculus.guard.Guard;
import wncalculus.tuple.AbstractTuple;
import wncalculus.util.Util;

/**
 * this class defines generic SN function-tuples, possibly composed of linear combinations
 * of elementary SN class-functions
 * (this implementation is not complete)
 * @author lorenzo capra
 */
public final class WNtuple extends AbstractTuple<LinearComb,BagfunctionTuple> implements BagfunctionTuple {

    /**
     * base constructor: creates a <tt>WNtuple</tt> from a list of class-functions
     * @param f the tuple's infilter
     * @param l the tuple's components (possibly, linear combinations)
     * @param g the tuple's guard
     * @param dom the tuple's domain
     * @param check domain-check flag
     */
    public WNtuple(Guard f, List<? extends LinearComb> l, Guard g, Domain dom, boolean check) {
        super(f, l, g, dom, check);
    }
    
    /**
     * efficiently creates a <tt>WNtuple</tt> from a map of colors to class-functions;
     * no check is done
     * @param f the tuple's infilter
     * @param codom the tuple's codomain
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
     * @param dom  the tuple's domain
     */
    public WNtuple(Guard f, Domain codom, SortedMap<ColorClass, List <? extends LinearComb>> m, Guard g, Domain dom)  {
        super(f, codom, m, g, dom);
    }

    @Override
    public WNtuple build(Guard filter, Guard guard, Domain domain) {
        return  filter() == filter && guard() == guard ? this :
                new WNtuple(filter, getCodomain(), getHomSubTuples(), guard, getDomain());
    }
    
    @Override
    public WNtuple build(Guard f, Domain cd, SortedMap<ColorClass, List<? extends LinearComb>> m, Guard g, Domain d) {
            return new WNtuple(f, cd, m, g, d);
    }

    /**
     * 
     * @return the tuple's expansion as a set of list of entries matching tuple's
     * components containing variables of the same index
     * it builds on <tt>Util.cartesianProd</tt>
     * should be invoked on single-color tuples
     */
    private Set<List<Entry<Integer, Map<ElementaryFunction, Integer>>>> expand( ) {
    	List<Set<Entry<Integer, Map<ElementaryFunction, Integer>>>> l = new ArrayList<>();
    	getComponents().stream().map(cx -> cx.components().entrySet()).forEachOrdered(l::add);
    	
    	return Util.cartesianProd(l);
    }
    
    /**
     * @return a tuple's expansion into a set (i.e., sum) of tuples whose components contain constants
     * or variables with the same index
     * should be invoked on single-color tuples, otherwise, it raises an exception
     * it builds on <tt>expand()</tt>
     * performs a kind of Cartesian product on <tt>this</tt> tuple, resulting in the set of
     * tuples with equal-index components
     * @return the tuple's expansion in a set of tuples whose components contain variables of the same index
     * should be invoked on single-color tuples, otherwise, it raises an exception
     * it build on <tt>expand()</tt>
     */
    public Set<? extends WNtuple> singleIndexComponentsTuples() {
    	HashSet<WNtuple> tset = new HashSet<>();
    	Set<List<Entry<Integer, Map<ElementaryFunction, Integer>>>> expansion = expand();
    	if (expansion.size() == 1)
            return Collections.singleton(this); //optimization
        
        ColorClass cc = getSort(); // the tuple is assumed-single color
    	for (List<Entry<Integer, Map<ElementaryFunction, Integer>>> lx : expansion) {
            List<LinearComb> lc = new ArrayList<>();
            lx.forEach(x -> { lc.add(new LinearComb(x.getValue())); });
            tset.add(new WNtuple (null, getCodomain(), Util.singleSortedMap(cc, lc), guard() /*null*/, getDomain()));
    	}
    	
    	return tset;		
    }
    
    /**
     * separates <tt>this</tt> tuple into its independent sub-tuples, given a partition of variable indices
     * modeling the independent parts of the associated guard (if any)
     * should be invoked on (single-color) tuples whose components hold at most one (projection) index 
     * (even if it works more generally)
     * @param connected the (possibly empty) pre-calculated set of connected components of tuple's guard
     * @return a map whose keys are sets of independent variable indices and whose values are corresponding
     * sub-tuples of <tt>this</tt> tuple, computed according the method's parameter;
     * each independent sub-tuple is expressed in turn as a map between tuple's positions (starting from 0) and components
     */
    public Map<Set<Integer>, LinkedHashMap<Integer,LinearComb> > independentComponents (Set<? extends Set<Integer>> connected) {
    	List<? extends LinearComb> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashMap<Integer, LinearComb>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashMap<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			LinearComb l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashMap<Integer, LinearComb> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashMap<Integer, LinearComb>> entry :  imap.entrySet() ) 
					if (entry.getKey().containsAll( indexSet ) ) { // indexset should be a singleton or empty
		    			 m = entry.getValue();
		    			 break;
		    		}
			if ( m ==  null ) 
				imap.put(l.indexSet(), m = new LinkedHashMap<>());
			m.put(i, l);
		}
			
    	return imap;
    }
    
    /**
     * overloaded version of <tt>independentComponents</tt>, which assumes <tt>this</tt> tuple single-color,
     * and with and and-type guard
     * @return
     */
    public Map<Set<Integer>, LinkedHashMap<Integer,LinearComb> > independentComponents () {
    	Guard g = guard();
    	Set<HashSet<Integer>> ind_g_indices = g instanceof And ? ((And)g).independentSets() : Collections.emptySet();
    	
    	
    	return independentComponents(ind_g_indices);
    }
    
    /*
     * 2nd version of independentComponents, which only returns the positions of independent subtuple elements
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 (Set<? extends Set<Integer>> connected) {
    	List<? extends LinearComb> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashSet<Integer>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashSet<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			LinearComb l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashSet<Integer> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashSet<Integer>> entry :  imap.entrySet() ) 
					if (entry.getKey().containsAll( indexSet ) ) { // indexset should be a singleton or empty
		    			 m = entry.getValue();
		    			 break;
		    		}
			if ( m ==  null ) 
				imap.put(l.indexSet(), m = new LinkedHashSet<>());
			
			m.add(i);
		}
			
    	return imap;
    }
    
    /**
     * overloaded version of <tt>independentComponentsV2</tt>, which assumes <tt>this</tt> tuple single-color,
     * and with and and-type guard
     * @return
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 () {
    	Guard g = guard();
        Set<HashSet<Integer>> ind_g_indices = g instanceof And ? ((And)g).independentSets() : Collections.emptySet();
    	
    	return independentComponentsV2(ind_g_indices);
    }

    @Override
    public TupleBag nullExpr() {
        return new TupleBag( getDomain(), getCodomain());
    }

    
    //implementazione parziale (non viene considerato il filtro)
    @Override
    public Integer card() {
        if (this.filter() != null)
            return null; //temporaneo!
        
        int card = 0;
        for (LinearComb x : getComponents()) {
            Integer xcard = x.card();
            if (xcard == null)
                return null;
            
            card += xcard;
        }
        return card;
    }
    
    @Override
    public Class<BagfunctionTuple> type() {
        return BagfunctionTuple.class;
    }
    
    /** 
     * performs the composition between legacy WN tuples, assuming that the infilter
     * of <tt>this<tt> is trivial 
     * @param right the tuple to tupleCompose with <tt>this<tt>
     * @return the composition result, <tt>null</tt> if for any reasons the composition cannot be done
     */
    public BagfunctionTuple tupleCompose (final WNtuple right)  {
        BagfunctionTuple res =  super.tupleCompose(right);
        if (res != this)
            return res;
        //this tuple has no guard
        final Guard infilter = right.filter(); 
        SortedMap<ColorClass, List<? extends LinearComb>> my_parts = getHomSubTuples();
        int left_parts = my_parts.size(); // the number of parts of this
        if (infilter == null) { // base case: there is no inner filter
             if (left_parts < 2) 
                 return onesortedTupleCompose(right); //may return null 

             List<BagfunctionTuple> compositions  = new ArrayList<>();
             my_parts.entrySet().forEach( h_part -> {
                 ColorClass c = h_part.getKey();
                 List<? extends LinearComb> list = h_part.getValue();
                 compositions.add(new TupleBagComp( new WNtuple (null, new Domain(c , list.size()), Util.singleSortedMap(c,list), null, getDomain()),  right));
            });
             
            return TupleBagProduct.factory(compositions);
        }   
        // the inner infilter is not trivial (!= null)
        WNtuple right_nof = (WNtuple) right.withoutFilter();
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> hom_filters = infilter.equalityMap(); //we assume that in the infilter there are just (in)equalities!!
        List<BagfunctionTuple> compositions = new LinkedList<>();
        hom_filters.entrySet().forEach(entry -> { 
            //compositions.add(new TupleBagComp(reduceGuard(entry.getKey(), entry.getValue()), right_nof) );
        });
        // residual sub-tuples not having any associated infilter ....
        my_parts.keySet().forEach(col -> {
            if (hom_filters.get(col) == null) {
                List<? extends LinearComb> st = my_parts.get(col); // the sub-tuple of cc_low_case cc_name
                compositions.add(new TupleBagComp(new WNtuple (null, new Domain(col,st.size()),  Util.singleSortedMap(col, st) , null, getDomain()), right_nof));
            }
        });
        //System.out.println("basecompose ->\n"+res); //debug
        return TupleBagProduct.factory(compositions);
    }
    

    private BagfunctionTuple onesortedTupleCompose(WNtuple right) {
        return null;
    }
    
     @Override
    public WNtuple asTuple() {
        return this;
    }

    @Override
    public Class<TupleBagComp> tkComp() {
        return TupleBagComp.class; 
    }
    
}
