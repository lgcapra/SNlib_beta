package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.SetFunction;
import wncalculus.expr.*;
import wncalculus.guard.Guard;
import wncalculus.logexpr.LogicalExpr;
import wncalculus.logexpr.SetExpr;
import wncalculus.util.Pair;
import wncalculus.util.Util;
import wncalculus.wnbag.SNfunctionTuple;

/**
 * @author Lorenzo Capra
 this class defines the super-type for any WN function-tuple mapping to sets
 */
public interface FunctionTuple extends SetExpr, SNfunctionTuple<SetFunction>, Transposable {
    
    /** 
     * just invokes a factory method (maybe redundant)
     * @param subtr the subtrahend
     */
    @Override
    default FunctionTuple diff (LogicalExpr subtr)  {
        return TupleDiff.factory(this, (FunctionTuple) subtr);
    }
    
    @Override
    default TupleTranspose buildTransp() {
        return new TupleTranspose(this);
    }    
        
      @Override
      default FunctionTuple notFactory(LogicalExpr arg) {
          FunctionTuple targ = (FunctionTuple)arg;
          return  TupleDiff.factory(targ.getTrue() , targ);
     }
      
      //the following methods return the passed argument, in the case the collection is a singleton

      @Override
      default FunctionTuple andFactory(Collection<? extends LogicalExpr> args) {
          return TupleIntersection.factory(Util.cast(args, FunctionTuple.class), false);
      }
      
      
      @Override
      default FunctionTuple orFactory(Collection<? extends LogicalExpr> args, boolean disjoined) {
          return TupleSum.factory(Util.cast(args, FunctionTuple.class), disjoined);
      }
       
         
     @Override
     default AllTuple getTrue() {
          return AllTuple.getInstance(getCodomain(), getDomain());
     }

     @Override
     default EmptyTuple getFalse() {
         return EmptyTuple.getInstance(getCodomain(), getDomain());
     }
   
     
    /**
     * computes the difference between <tt>this</tt> function T and a sum T1 + T2 + ..TN
     * as (T - T1) - (T2 + .. TN)
     * @param other the sum of function-tuples to be subtracted from <tt>this</tt> function-tuple
     * @return the difference between this and other sum
     * @throws IllegalDomain if the domains of involved tuples are different
     */
    default FunctionTuple subtract (TupleSum other) {
        Collection<FunctionTuple> args_copy = new HashSet<>(other.getArgs());
        Iterator<FunctionTuple> ite = args_copy.iterator();
        FunctionTuple first_diff = TupleDiff.factory(this, ite.next());
        ite.remove();
        FunctionTuple res = TupleDiff.factory(first_diff, TupleSum.factory(args_copy, other.disjoined()));
        //System.out.println(res);
        return res;
    }
    
    /** tries to merge tuples in the specified list, until possible (expensive);
     *  operates in a destructive way
     *  @param l a list of tuples
     */
     public static void fold (List<Tuple> l) {
        boolean merged;
        do {
            merged = false;
            for (ListIterator<Tuple> ite =  l.listIterator() ; ite.hasNext(); ) {
                Tuple t= ite.next(),  m;
                int j;
                for (j = ite.nextIndex() ; j  < l.size() ;  j++ ) 
                    if ( ( m =  l.get(j) .merge(t) ) != null ) { //merge succeeded!
                        ite.set(m);
                        merged = true;
                        break; //exits the innermost loop
                    }
                if ( merged ) {
                    l.remove(j);
                    break; //exits the outer for loop: this way is safe!
                }   
            }
        }
        while (merged) ;
    }
     
    /**
     * @return the codomain's size (i.e., number of elements) of <code>this</code> function
     */
    default int size() {
        return getCodomain().asMap().values().stream().map(v -> v).reduce(0, Integer::sum);
   }
    
   // methods for the computation of SODE
    
    /**
     * applies a given filter to <tt>this</tt> expression and calculates the resulting
     * (lower bound of the) cardinality on the corresponding normalized expression
     * CAREFUL: the filter is assumed to be (normalized and) in a disjoint form
     * should apply to previously normalized functions (Tuple or TupleSum ...), this is not
     * a requirement
     * @param f a function-tuple
     * @return a <tt>Pair</tt> witj the cardinality of <code>this</code> function-tuple with the applied filter,
     * and the resulting normalized expression
     * @throws NullPointerException if (for any reasons) the cardnality of <code>this</code> function-tuple,
     * with the applied filter, cannot be computed
     */
    default Pair<Integer,FunctionTuple> applyFilter(Guard f) {
        FunctionTuple fx = (FunctionTuple) new GuardedTuple (f, this).normalize(true);
        Integer c = fx.cardLb();
        if (c != null)
            return new Pair<>(c,fx);
        
        throw new NullPointerException("cannot compute the card of:"+ /*'('+f+')'+ this + "->\n" +*/ fx.toStringDetailed());   
    }
    /**
     * applies a given set of filters to <code>this</code> expression and results in
     * a map of pairs containing the (lower bounds of the) cardinalities and the expressions coming from
     * filters application; a <code>null</code> value means that the
     * cardinality of the corresponding expression (for any reasons) cannot be computed
     * @see applyFilter
     * @param filters a set of filters
     * @return the cardinalities of the (possible) refinements of <code>this</code> function-tuple
     * due to the application of filters
     * @throws NullPointerException if (for any reasons) the cardnality of <code>this</code> function-tuple,
     * with the applied filters, cannot be computed
     */
    default Map<Guard, Pair<Integer,FunctionTuple>> applyFilters(Set<? extends Guard> filters) {
         Map<Guard, Pair<Integer,FunctionTuple>> m = new HashMap<>();
         filters.forEach( f -> { m.put(f, applyFilter(f)); });
         
         return m;
    }
    
    /**
     * optional method
     * @return a compact representation used in SODE computation 
     * @throws UnsupportedOperationException
     */
    default  String toStringAbstract () {
        throw new UnsupportedOperationException("not implemented for "+ getClass());
    }
    
    
    @Override
    default FunctionTuple buildGuardedExpr(Guard f, Expression e, Guard g) {
        return new GuardedTuple (f, (FunctionTuple) e, g);
    }
    
    @Override
    default Class<? extends Expression> type() {
        return FunctionTuple.class;
    }
}
