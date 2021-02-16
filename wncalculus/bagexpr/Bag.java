package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.*;

/**
 * @author Lorenzo Capra
 * the abstract data type for a "generalised" Bag, inter.e., a set whose elements have an associated
 * (signed) integer multiplicity
 * @param <E> the domain of the elements of the bag 
 */
public interface Bag<E extends ParametricExpr> extends BagExpr, NonTerminal {
    
    static final String EMPTY = "<null>";
    
    /** 
     * @return a map-view of this bag
     */
    Map<? extends E,Integer> asMap ();
   
     
    /**
     * @param e a given element
     * @return the multiplicity of the element in the multiset; 0 if it is not present
     */
    default int mult(E e) {
        Integer x = asMap().get(e);
        return x != null ? x : 0;
    }
    
    /** 
     * @return the support of the multi-set, inter.e., the set of its elements with multiplicity
        different from zero; the set is backed by the bag
    */
    default Set<? extends E> support() {
        return asMap().keySet();
    }
    
    @Override
    default boolean isConstant() {
        return support().stream().allMatch( x -> x.isConstant() );
    }
    
    /**
     * @return the bag's support size
     */
    default int size() {
        return asMap().size();
    }
    
    /**
     * @return <code>true</code> if and only if the multi-set is build 
     */
    default boolean isEmpty() {
        return asMap().isEmpty();
    }
    
    @Override
    default boolean isNull() {
        return isEmpty();
    }
             
    /**
     * @return the elements of this bag with coefficients greater than zero:
     * <tt>null</tt> if, for any reasons, they cannot be computed
     */
    default Set<? extends E> properSupport() {
        HashSet<E> pset = new HashSet<>();
        asMap().entrySet().stream().filter(e -> ( e.getValue() > 0 )).forEachOrdered(e -> { pset.add(e.getKey()); });
            
        return pset;
    }
    
    /**
     * 
     * @return <code>true</code> if and only if <code>this</code> is a true bag 
     */
    default boolean isProper () {
        return isEmpty() || Collections.min(asMap().values()) > 0;
    }
    
    /**
     * 
     * @return the bag's cardinality, meant as the cardinality of the application of the
     * corresponding bag-function to any argument resulting in a non-empty bag;
     * <code>null</code> if the cardinality cannot be computed (inter.e., the bag-function is not constant-size)
     */
    Integer card();
    
    @Override
    default Bag<E> clone(Domain newdom, Domain newcd) {
         if (isEmpty())
            return build(newdom, newcd) ;
         
         HashMap<E,Integer> mapcopy = new HashMap<>();
         Class<E> type = bagType();
         asMap().entrySet().forEach(e -> { mapcopy.put(type.cast(e.getKey().clone(newdom, newcd)), e.getValue()); });
         
         return (Bag<E>) build(mapcopy);
    }
    
    /**
     * 
     * @return a simplified, bag obtained by normalizing bag's elements and "withdrawing"
     * null bag's elements
     */
    @Override
    default Bag<E> genSimplify () {
       if ( isEmpty() ) 
           return this;
       
       HashMap<E,Integer> smap = new HashMap<>();
       Class<E> type = bagType();
       Expression nullExpr = asMap().keySet().iterator().next().nullExpr();
       asMap().entrySet().forEach( s -> {
           E key = s.getKey(), e = type.cast(key.normalize());
           if (! e.equals(nullExpr ) ) 
               smap.put( e, s.getValue());
       });
       
       return  smap.isEmpty() ? build() : build(smap).cast();
    }
    
    //base operations on bags (maps)
    
    /**
     * (destructively) does the intersection between maps (i.e., bags) by putting the result in the first one,
     * which is returned; it builds on <tt>pairwiseinter</tt>
     * @param <E> the map's key type
     * @param b 1st map
     * @param m 2nd map
     */
    public static <E> void intersection(Map<E, Integer> b, Map<? extends E, Integer> m) {
        if (b.isEmpty())
            return;
        
        if (m.isEmpty()) {
            b.clear();
            return;
        }
        Map<E, Integer> i = pairwiseinter(b, m);
        b.clear();
        b.putAll(i);
    }
    /**
     * efficiently computes the pairwise-intersection between maps (bags), by summing the multiplicities
     * of map (bag) elements
     * @param <E> the map's key type
     * @param b 1st map
     * @param m 2nd map
     * @return the pair-wise intersection between the maps
     */
    static <E> Map<E, Integer> pairwiseinter(Map<? extends E, Integer> b, Map<? extends E, Integer> m) {
        Map<? extends E, Integer> small = b, big = m;
        if (b.size() > m.size()) {
            small = m;
            big = b;
        }
        Map<E, Integer> inter = new HashMap<>();
        for (Map.Entry<? extends E, Integer> x : small.entrySet()) 
            inter.put(x.getKey(), Math.min(big.getOrDefault(x.getKey(), 0), x.getValue()));
        //System.out.println("pairwisesum: "+inter); //debug
        return inter;
    }
    
    /**
     * computes the product between a map (bag) and a scalar
     * @param <E> the map's key type
     * @param b the map
     * @param k the scalar
     * @return the product <tt>k</tt> by <tt>b</tt>
     */
    public static <E> Map<E, Integer> scalarprod(Map<? extends E, Integer> b, int k) {
          if (k == 0 || b.isEmpty())
             return Collections.emptyMap();

        if (k == 1)
            return Collections.unmodifiableMap(b);
        
        HashMap<E,Integer> m = new HashMap<>();
        b.entrySet().forEach(e -> { m.put(e.getKey(), k * e.getValue()); });
        return m;
    }
    
    /**
     * updates a map of occurrences (i.e., a bag) by summing up another map
     * the values of the 2nd are multiplied by a coefficient
     * @param <E> map key's type
     * @param sum the map to update
     * @param m the map to sum up
     * @param k multiplicative coefficient
     */
    public static <E> void addAll(Map<E, Integer> sum, Map<? extends E, Integer> m, int k) {
        m.entrySet().forEach(x -> { add(sum, x.getKey(), x.getValue() * k) ; });
    }

    /**
     * updates an entry of map of occurrences (i.e., a bag)
     * @param <E> map key's type
     * @param sum the map to update
     * @param x the key to update
     * @param n the value to sum up
     */
    public static <E> void add(Map<E, Integer> sum, E x, int n) {
        sum.put(x, sum.getOrDefault(x, 0) + n);
    }
    
    @Override
    default String symb() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     * @param b a bag
     * @param k a weight
     * @return the weighted sum between <tt>this</tt> and the specified bag
     */
    default Bag<E> sum(Bag<E> b, int k) {
        if (isEmpty())
            return b;
        
        if (b.isEmpty())
            return this;
        
        HashMap<E,Integer> sum = new HashMap<>(asMap());
        Bag.addAll(sum, b.asMap(), k);
        
        return (Bag<E>) build(sum);
    }
    
    /**
     * @param b a bag
     * @return the sum between <tt>this</tt> and the specified bag
     */
    default Bag<E> sum(Bag<E> b) {
        return sum(b,1);
    }
    
    /**
     * @param b a bag
     * @return @return the difference between <tt>this</tt> and the specified bag
     */
    default Bag<E> diff(Bag<E> b) {
        return sum(b,-1);
    }
           
    /**
     * @return the bag's elements type 
     */
    <E extends ParametricExpr> Class<E> bagType();
    
}
