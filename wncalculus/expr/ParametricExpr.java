package wncalculus.expr;

import java.util.*;
import static wncalculus.expr.Expressions.mergeResults;
import static wncalculus.expr.Expressions.printResults;
import wncalculus.util.Pair;
import wncalculus.util.Util;

/**
 * this interface defines the ADT of expressions with (possibly) parameterized domains
 * @author Lorenzo Capra
 */
public interface ParametricExpr extends Expression {

    /**
     * split <tt>this</tt> parametric expression into an equivalent set
     * with more specific constraints;
     * assumes that the split of single parametric color-classes are binary, i.e.,
     * the interval defining the constraint is divided into two sub-intervals
     * @return a (possibly empty, in the case no split is done for any reasons)
     * set of equivalent expressions with more specific constraints
    */
    default Set<ParametricExpr> split() {
        Map<Sort, Integer> delim = splitDelimiters();
        if (!delim.isEmpty()) {
            int n = 0;
            long pow2 = 1;
            // we build a map between sorts and corresponding numerical id (denoting a given bit in the split encoding), throuh 0..n-1
            HashMap<Integer, Pair<Sort, Map<Boolean, Sort>>> split_sort_map = new HashMap<>();
            for (Map.Entry<Sort, Integer> es : delim.entrySet()) {
                Sort sort = es.getKey();
                Map<Boolean, Sort> split_sort = sort.split2(es.getValue());
                if (!split_sort.isEmpty()) {
                    // non split sorts are skipped - we could preliminarily check for this
                    if ((pow2 *= 2) > Integer.MAX_VALUE) {
                        throw new Error("at most " + (Integer.SIZE - 1) + " colors may be split\n");
                    }
                    split_sort_map.put(n++, new Pair<>(sort, split_sort));
                }
            } // pow2 == 2^n
            if (!split_sort_map.isEmpty()) {
                Set<ParametricExpr> res = new HashSet<>();
                for (int i = 0; i < pow2; i++) {
                    //each i encodes one of the possible 2^n splits of term - each of them corresponds to a clone
                    HashMap<Sort, Sort> split_map = new HashMap<>();
                    for (Map.Entry<Integer, Pair<Sort, Map<Boolean, Sort>>> x : split_sort_map.entrySet()) {
                        Pair<Sort, Map<Boolean, Sort>> p = x.getValue();
                        split_map.put(p.getKey(), p.getValue().get(Util.checkBit(i, x.getKey())));
                    }
                    //System.out.println("split_map ->\n"+split_map);
                    res.add( clone(getDomain().setSupport(split_map), getCodomain().setSupport(split_map))); //nuovo clone
                }
                return res;
            }
        }
        return Collections.EMPTY_SET;
    }
    
      /**
     * builds a copy of <tt>this</tt> expression with given new co-domains, which are assumede compliant
     * with the term's co-domains; the terms composing the expression are cloned too
     * optionally performs domain consistency checks
     * @param newdom the new domain
     * @param newcd the new codomain
     * @return  a copy of this term with the new co-domains
     * @throws IllegalDomain if the specified co-domains are not compliant with the current one
     */
      ParametricExpr clone (final Domain newdom, final Domain newcd);
      
      
      default ParametricExpr clone (final Domain newdom) {
          return clone(newdom, getCodomain());
      }
      
     /**
     * clone a given collection of expressions, by assigning them a new domain
     * @param c a given collection of expressions
     * @param dom a new domain
     * @return a collection with clones having the new domain of the same type as the passed collection
     * @throws NullPointerException
     */    
    public static Collection<ParametricExpr> cloneCollExpr ( Collection<? extends ParametricExpr> c , Domain dom) {
        Collection<ParametricExpr> copy;
        if (c instanceof List) 
            copy = new ArrayList<>();
        else if (c instanceof SortedSet)
            copy = new TreeSet<>();
        else 
            copy = new HashSet<>();
        c.forEach(g -> { copy.add(g.clone(dom)); });
        
        return copy;
    }
    
    public static <E extends ParametricExpr> Collection<E> cloneCollExpr ( Collection<? extends E> c , Domain dom, Class<E> type) {
        return Util.cast(ParametricExpr.cloneCollExpr (c , dom), type);
    }
    
    /**
     * clone a given map of collections of expressions, by assigning them a new domain
     * @param m a given map of collections of expressions
     * @param dom a new domain
     * @return a collection with clones having the new domain, of the same type as the passed collection
     * @throws NullPointerException
     */
    public static <K,E extends ParametricExpr> Map<K,? extends Collection<E> > cloneMapExpr ( Map<K,? extends Collection<E>> m , Domain dom, Class<E> type)  {
        Map<K,Collection<E>> new_map = new HashMap<>();
        m.entrySet().forEach(e -> { 
            new_map.put(e.getKey(), ParametricExpr.cloneCollExpr(e.getValue(), dom, type));
        });
        
        return new_map;
    }

      
     /**
      * @return a map of sorts to split-delimiters for <code>this</code> term;
      * sorts not appearing in the map shouldn't be split
      */
     Map<Sort,Integer> splitDelimiters () ;
     
     /**
      * 
      * @return <code>true</code> if and only if any sort of expression's arity is parametric 
      */
     default boolean isParametric() {
         Set<Sort> supp = getDomain().support();
         boolean par = supp.stream().anyMatch(s ->  s.ccSize() == 0 );
         if (!par)
             par = getCodomain().support().stream().anyMatch(s ->  supp.contains(s) && s.ccSize() == 0 );
         
         return par; 
     }
               
    /**
     * the core simplification algorithm for parametric expressions
     * performs a fixed-point algorithm that normalizes and possibly split (if needed) <code>this</code> term;
     * splitting and normalization are done iteratively
     * in this (improved) version terms are first simplified, trying to delay the split
     * syntactically identical normalization results are finally aggregated
     * @param verbose indicates whether resulting terms have to be printed, with some profiling info
     * @return a collection of equivalent normalized terms
     */
    default Set<? extends ParametricExpr> simplify (boolean verbose) {
        long startTime = System.currentTimeMillis();
        Set<ParametricExpr>  res_set    = new LinkedHashSet<>(); // the list of normalization results
        List<ParametricExpr> to_simplify = Util.singletonList(this);
        for (ListIterator<ParametricExpr> ite = to_simplify.listIterator(1); ite.hasPrevious();) {
            ParametricExpr tx = ite.previous();
            //System.out.println("tx: "+tx.toStringDetailed()); // debug
            ite.remove();
            Collection<? extends ParametricExpr> split_terms;
            if (tx.isParametric() && (split_terms = tx.split()). size() > 1) 
                split_terms.forEach( t -> {
                    //System.out.println("tx split: "+t.toStringDetailed()); // debug
                    t.setSimplified(false);
                    ite.add((ParametricExpr) t.normalize()); } );
            else 
                if (tx.simplified())
                    res_set.add(tx); //if it has been already normalized does nothing
                else
                    ite.add((ParametricExpr)tx.normalize());
        } //end for    
        
        mergeResults(res_set); //syntactically identical normalization results are aggregated w.r.t. their constraints
        if (verbose) {
            long endTime = System.currentTimeMillis(), seconds = endTime - startTime;
            System.out.println("normalization time: " + seconds + " ms");
            printResults(res_set);
        }
        
        return res_set;
    }
        
    /**
     * default version of simplify
     * @return a list of equivalent normalized terms "equivalent" (modulo a
     * split of constraint) to <code>this</code>
     */
    default Set<? extends ParametricExpr> simplify ( ) {
        return simplify(true);
    }
  
}
