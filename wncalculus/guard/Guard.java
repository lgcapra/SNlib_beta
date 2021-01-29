package wncalculus.guard;

import wncalculus.logexpr.LogicalExpr;
import java.util.*;

import wncalculus.classfunction.SetFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.Expression;
import wncalculus.util.Util;

/**
 * This abstract class represents the root of the hierarchy describing SN guards/filters.
 * The class and its subclasses meet the interpreter pattern.
 * @author Lorenzo Capra
 */
public abstract class Guard implements LogicalExpr {
    
    private boolean simplified;
    private Map<ColorClass , List<? extends SetFunction>> right_tuple; // if other than null, marks this guard as a filter of a tuple
    
    
    @Override
    public final boolean simplified () {
        return this.simplified;
    }
    
    @Override
    public void setSimplified(boolean simplified) {
       this.simplified = simplified;
    }
    
    /**
     * set <tt>this</tt> guard as a filter associated to a given tuple, expressed by
     * a map of colors to corresponding class-function lists
     * @param m a map between colors and corresponding class-function lists (i.e, a tuple)
     */
    public final void setAsFilter(Map<ColorClass, List<? extends SetFunction>> m) {
        this.right_tuple = m;
    }
    
    /**
     *
     * @return the tuple (expressed as a map) which is associated with <tt>this</tt> guard,
     * seen as a filter; <tt>null</tt> if no tuple is associayed with <tt>this</tt> guard
     */
    public Map<ColorClass , List<? extends SetFunction>> getRightTuple() {
        return this.right_tuple;
    }
    
    
    
    @Override
    /**
     * @return the guard's codomain, which is equal (by definition) to the domain
     */
    public final Domain getCodomain() {
         return getDomain(); 
    }
    
    
    /** static version of indexSet working on a collection
     * @param c a given collection of guards
     * @return  the set of projection indices in <tt>c</tt>
     */
    public static Set<Integer> indexSet(Collection<? extends Guard> c) {
        Set<Integer> idxset = new HashSet<>();
        c.forEach( f -> { idxset.addAll(f.indexSet()); });
        
        return idxset;
    } 
    
    /** 
     * @return  the set of indexes of projections occurring on this guard
     */
    public abstract Set<Integer> indexSet();

    
    /** this trivial implementation is the only possible for guards*/
    @Override
    public  boolean differentFromZero() {
       return isTrue() ;
    }
    
    
      @Override
      public final Guard notFactory(LogicalExpr arg) {
          return Neg.factory((Guard)arg);
      }
      
      //the following methods return the passed argument, in the case the collection is a singleton

  
      @Override
      public final Guard andFactory(Collection<? extends LogicalExpr> args)  {
          return And.factory(Util.cast(args, Guard.class), false);
      }
      
      @Override
      public final Guard orFactory(Collection<? extends LogicalExpr> args, boolean disjoined)  {
          return Or.factory(Util.cast(args, Guard.class), disjoined);
      }
      
    /**
     * builds an "OR" guard from a pair of operands, assumed not disjoint
     * @param arg1 the first operand
     * @param arg2 the second operand
     * @return the newly built guard
     */
    public final Guard orFactory(LogicalExpr arg1, LogicalExpr arg2)  {
        return Or.factory(false, (Guard)arg1, (Guard)arg2);
     }
     
    /**
     * @param g a specified guard
     * @return <code>true</code> if and only this guard and  <code>g</code> have
     * the same color domain
     */
    final boolean sameDomainAs(Guard g) {
        return g.getDomain().equals( getDomain() );
     }

    
    /** the two methods below buildOp a constant with the same domain as the current term */
    @Override
    public final True getTrue() {    
        return True.getInstance(getDomain());
    }

    @Override
    public final False getFalse() {
       return False.getInstance(getDomain());
    }
    
    /**
     * given a list of guards, returns its (possibly empty) restriction (also called "projection") to those that refer
     * to projection indexes less than or equal to k; if the list contains any terms, with an empty index-set,
     * an exception is raised
     * @param <E> the type parameter: must be a type of guard
     * @param args the collection of guards
     * @param k the projection index used as restriction bound
     * @return a sub-list of guards, of the same type as the input list, which contains the restriction
     */
    public static <E extends Guard> Set<E> restriction (Collection<? extends E> args, int k) {
        LinkedHashSet<E> res = new LinkedHashSet<>();
        args.stream().filter(g ->  Collections.max( g.indexSet() ) <= k ).forEachOrdered(g -> { res.add(g); });
            
        return res;
    }
    
    //collection of static methods for the manipulation of list of guards (almost always assumed color-homogeneous and ordered)
   
    /**
     * checks whether a collection of guards contains an (in)equality betweeen terms of given indexes
     * @param cg the specified collection
     * @param i the firts index
     * @param j the second index
     * @param op the sign of the (in)equality
     * @return true if and only if cg contains an inequality between f(X_i) and f(X_j) of the appropriate sign 
     */
     public static boolean contains (Collection<? extends Guard > cg, int i, int j, boolean op) {
        return cg.stream().filter(g -> g instanceof Equality).map(g -> (Equality) g).anyMatch(eq -> eq.sign() == op && eq.firstIndex() ==i && eq.secondIndex() == j);
    }
    
    /**
     * @return true if and only if this guard is an "and form" which only contains elementary guards 
     * default implementation
     */
    public boolean isElemAndForm() {
        return false;
    }
    
    @Override
    public final Class<Guard> type() {
        return Guard.class;
    }
    
    //convenience method (to be redefined)
    
    /**
     * @return <tt>true</tt> if and only <tt>this</tt> is an equality of type X^i == X^j
     */
    public boolean isEquality() {
        return false;
    }
    
    /**
     * @return <tt>true</tt> if and only if @code{this} is an equality of type X^i != X^j
     */
    public boolean isInEquality() {
        return false;
    }
    
    /**
     * @return <tt>true</tt> if and only if @code{this} is a membership of type X^i in C_j
     */
    public boolean isMembership() {
        return false;
    }
    
    /**
     * @return  <tt>true</tt> if and only if  @code{this} is a membership of type X^i notin C_j
     */
    public boolean isNotinMembership() {
        return false;
    }
    
     
    @Override
    public abstract Guard clone(Domain nd);
    
    @Override
    public Guard buildGuardedExpr(Guard f, Expression e, Guard g) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * assuming that a given guard is (an elementary) "AND" form, calculates its set
     * of operands (a singleton if the guard is elementary)
     * @param g a guard
     * @return the set of operands of the guard, if it is an elementary "AND" form (a singleton if the guard is elementary);
     * <tt>null</tt> otherwise
     */
    public Set<? extends Guard> getElemArgs() {
        return null;
    }
    
    /**
     * @return the color-memberships-map of <tt>this</tt>  guard, if it is an elementary-"AND"-form;
     * an empty map, otherwise
     */
    public Map<ColorClass, Map<Boolean,HashSet<Membership>>> membershipMap() {
        return Collections.emptyMap();
    }
    
    /**
     * @return the color-equality-map of <tt>this</tt>  guard, if it is an elementary-"AND"-form;
     * an empty map, otherwise
     */
    public Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap() {
        return Collections.emptyMap();
    }
    
}
