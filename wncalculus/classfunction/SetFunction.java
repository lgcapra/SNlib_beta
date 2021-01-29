package wncalculus.classfunction;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.guard.Equality;
import wncalculus.guard.Guard;
import wncalculus.logexpr.LogicalExpr;
import wncalculus.logexpr.SetExpr;
import wncalculus.util.Pair;
import wncalculus.util.Util;
import wncalculus.wnbag.LinearComb;

/**
 * this abstract class is the super-type of class-functions mapping to sets
 * @author lorenzo capra
 */
public abstract class SetFunction implements ClassFunction, SetExpr {

    private boolean simplified;
    
    protected SetFunction(boolean simp) {
        this.simplified = simp;
    }
    
    protected SetFunction () { }
    
    /**
     * * (default implementation to override)
     * @return an elementary linear-combination composed of <tt>this</tt> term 
     * DA TOGLIERE CON NUOVA IMPLEMENTAZIONE DEI BAG?
     */
    @Override
    public LinearComb asBag() {
    	throw new UnsupportedOperationException();
    }
    
    
    @Override
    public final All getTrue() {
        return All.getInstance(getSort());
    }

     @Override
     public final Empty getFalse() {
         return Empty.getInstance(getSort());
     }
     
      @Override
      public final SetFunction notFactory(LogicalExpr arg) {
          return Complement.factory((SetFunction) arg);
      }
      
      //the following methods return the passed argument, in the case the collection is a singleton

    
     @Override
     public final SetFunction andFactory(Collection<? extends LogicalExpr> args) {
          return Intersection.factory(Util.cast(args, SetFunction.class), false);
      }
      
      
      @Override
      public final SetFunction orFactory(Collection<? extends LogicalExpr> args, boolean disjoined) {
          return Union.factory(Util.cast(args, SetFunction.class), disjoined);
      }
      
  
     @Override
     public final boolean differentFromZero() {
        Interval mycard = card();
        return mycard != null && mycard.lb() > 0;
    }
     
    /**
     *
     * @param ite a collection of class-functions mapping to sets
     * @return <tt>true</tt> if and only if every function is not equivalent to "zero"
     */
    public final static boolean differentFromZero(Iterable<? extends SetFunction> ite) {
        for (SetFunction f : ite) 
            if ( !f.differentFromZero() ) 
                return false;
        
        return true;
    }
        
    /**
     *
     * @return the size of any application of <tt>this</tt> function;
     * <tt>null</tt> if it cannot be computed, for any reasons
     */
    public abstract Interval card();
    
    @Override
    public final Integer cardLb() {
         Interval c = card();
         return c == null ? null : c.lb();
    }
    
    /** 
     * @return <tt>true</tt> if and only if this function has zero cardinality 
     */
    public boolean zeroCard () {
        Interval card = card();
        
        return card != null && card.ub() == 0;
    }
    
    /**
     *
     * @return the difference between the lower bounds of <tt>this</tt> function's color-class cardinality
     * and the size (cardinality) of the function; -1 if, for any reasons, it cannot be computed
     */
    public final int gap () {
        int gap = -1;
        Interval mycard = card() , constr;
        if (mycard != null && mycard.size() == ( constr = getConstraint() ).size()) //  card and constr either unbounded or "congruent" 
              gap = constr.lb() - mycard.lb();
        
        return gap;
    }
    
    /**
     *
     * @return <tt>true</tt> if and only if the size (cardinality) of <tt>this</tt> function
     * is less than or equal to one
     */
    public final boolean cardLeq1 () {
        Interval card = card();
        return card != null && card.singleValue(1) || 
               this instanceof Intersection && Util.find( ((Intersection)this).getArgs(), Projection.class) != null ;
    }
    
    /**
     * claculates the composition between class-function mapping to sets,
     * disregarding the variables' indices
     * @param right the function to compose with <tt>this</tt>
     * @return the composition result; <tt>null</tt> if, for any reasons,
     * the composition outcome cannot be computed
     */
    public SetFunction baseCompose ( SetFunction right) {
        return null;
    }
    
    @Override
    public final Class<SetFunction> type () {
        return SetFunction.class;
    }
    
    /**
     * puts an intersection-form to an equivalent set (i.e., sum) of simple (guarded) class-functions
     * this default version must be redefined if needed; it assumes that basic reductions have been
     * carried out; the arguments encode the basic predicates associated with the function, when it is
     * embedded in a tuple
     * @param ineqlist the inequalities 
     * @param inmap a map describing the membership "in" predicates
     * @param domain the guard domain
     * @param notinmap a map describing the membership "notin" predicates (there may be many, for a given variable)
     * @return if <tt>this</tt> is an "elementary" intersection-form, an equivalent
     * set (sum) of simple (guarded) class-functions, represented by <tt>Pair</tt>s; otherwise the
     * <tt>Pair</tt> (<tt>this</tt>,<tt>null</tt>)
    */ 
    public Set<? extends Pair<? extends SetFunction, ? extends Guard> > toSimpleFunctions (Set<? extends Equality> ineqlist, Map<Projection, Subcl> inmap, Map<Projection, HashSet<Subcl>> notinmap, Domain domain) {
         return Collections.singleton (new Pair<>(this, null));
    }
    
    
    @Override
    public void setSimplified(boolean simp) {
        this.simplified = simp;
    }
    
    @Override
    public final boolean simplified() {
        return this.simplified;
    }
    
}
