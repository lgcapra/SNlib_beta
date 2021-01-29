package wncalculus.expr;

import java.util.*;
import wncalculus.util.Util;

/**
 * this class is a collector of generic algorithms for the
 * manipulation of  expressions
 * @author Lorenzo Capra
 */
public class Expressions {
    
    /**
     * provides a detailed textual description for a collection of logical terms
     * @param c the terms' collection
     * @return the terms's description, one for each row
     */
    public static String toStringDetailed(Collection<? extends Expression> c) {
        String out = "";
        out = c.stream().map(f -> f.toStringDetailed() + '\n').reduce(out, String::concat);
        
        return out;
    }
    
    /**
     * print a collection of expressions, where each  term may be followed by
     * information about its cardinality, depending on the implementation of <code>printCard</code>
     * @param result the collection to be printed
    */
    public static void printResults (Collection<? extends Expression> result) {
        System.out.println("normalization result:");
        int i = 1;
        for (Expression e : result) {
            System.out.printf("%-2d) %s",i++, e.toStringDetailed()+'\n');
        }
    }

     /**
     * merges syntactically identical expressions in the given collection whose color
     * constraints can be folded, operating in a destructive way
     * @param <E> the type of expressions
     * @param terms a list of terms
     */
    public static <E extends ParametricExpr> void mergeResults (Set<E> terms) {
        if (terms.size() > 1) { 
            HashMap<String, Collection<E>> map = new HashMap<>(); //map between strings and corresponding terms
            terms.forEach(t -> { Util.addElem( t.toString(),t, map, List.class); });
            for (Map.Entry<String, Collection<E>> e : map.entrySet()) {
                List<E> l =  (List<E>) e.getValue(); //list of "identical" terms
                if (l.size() > 1) {
                   HashMap<E,List<Sort>> sorts_map = new HashMap<>(); //map between terms and their sorts (the list are ordered)
                   l.forEach( t -> { sorts_map.put(t, t.getSorts()); });
                   Comparator<E> tcomp = (t1, t2) -> { //comparator for ordered list of sorts (of the same size)
                       int cmp = 0;
                       List<Sort> l1 = sorts_map.get(t1), l2 = sorts_map.get(t2); //l1 and l2 should have the same size
                       for (int i = 0; i < l1.size() && (cmp = l1.get(i).compareTo(l2.get(i))) == 0 ; ++i) {}
                       
                       return cmp;
                   };
                   Collections.sort(l, tcomp); // "identical" terms have been ordered w.r.t. their sorts
                   ListIterator<E> ite = l.listIterator();
                   for (E p = ite.next() , c; ite.hasNext() ; p = l.get(ite.previousIndex())) {
                       Map<Sort,Sort> glued = mergeConstraint(sorts_map.get(p), sorts_map.get(c = ite.next()));
                       if ( glued != null ) {
                           E cp = p.clone(p.getDomain().setSupport(glued), p.getCodomain().setSupport(glued)). cast();
                           ite.set(cp);
                           sorts_map.put(cp, cp.getSorts());
                           terms.add(cp);
                           terms.remove(p);
                           terms.remove(c);
                       }
                   }
                }
            }
        }
    }
    
    /** try to "merge" two constraints (i.e., two ordered lists of corresponding sorts)
     *  of two terms of the same arity 
     *  @return a map between the old sort of the first list and the sort resulting from merge, if any;
     *  <tt>null</tt> if no merge is done
    */
    private static  Map<Sort,Sort>  mergeConstraint(List<? extends Sort> c1, List<? extends Sort> c2) {
        Map<Sort,Sort> old_to_merge = null;
        Sort merge_sort = null, sort1, sort2;
        for (int i = 0; i < c1.size() ; i++) 
            if ( ! (sort1 = c1.get(i)). equals(sort2 = c2.get(i)) ) { 
                if (merge_sort != null || (merge_sort = sort1.merge(sort2)) == null)  // either the sorts are not "foldable" or two "foldable" sorts have been already found
                  return null;
                
                old_to_merge = Collections.singletonMap(sort1, merge_sort);
            }
        
        return old_to_merge;
    }

    
    /**
     * checks the domains of a collection of expressions
     * @param terms a collection of expressions
     * @throws IllegalDomain if the domains of the expressions are not congruent
     */
    public static void checkDomain(Collection<? extends Expression> terms) {
        Util.checkProperty(terms, Expression::sameDomain, e-> "dom: "+e.getDomain(), IllegalDomain.class);
    }
    
   /**
    * checks the co-domains of a collection of expressions; in the case the flag <code>size</code> is set,
    * checks also the co-domains
    * @param terms a collection of expressions
    * @throws IllegalDomain if the co-domains  of the expressions are not congruent
    */
    public static void checkArity(Collection<? extends Expression> terms) {
        Util.checkProperty(terms, Expression::sameArity, e-> "dom: "+e.getDomain()+" codom: "+e.getCodomain(), IllegalDomain.class);
    }
    
    public static void checkArity(Expression ... terms) {
        checkArity(Arrays.asList(terms));
    }
     
}   
