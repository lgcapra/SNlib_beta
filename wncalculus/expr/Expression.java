package wncalculus.expr;

import java.util.*;
import wncalculus.guard.Guard;

/**
 * this interface is the root of the whole hierarchy, and represents the super-type
 * of terms of a language, which can be
 * normalized; these terms have an arity, i.e., a domain and a codomain
 * @author lorenzo capra
 */
public interface Expression {   
    
    /**
     * performs a domain-specific simplification of <code>this</code> term
     * (default version: to be overwritten)
     * @return an equivalent expression; should return <code>this</code> if no simplification is done
     */
    default Expression specSimplify () {
         return this;
    }
     
      /**
      * performs a simplification based on generic rules of <code>this</code> term
      * (default version: to be overwritten)
      * @return an equivalent expression or <code>this</code> if no simplification is done
      */
      default Expression genSimplify() {
          return this;
      }
      
     /**
     * basic generic fixed-point normalization algorithm for <tt>this</tt> Expression;
     * it relies on <code>Expression.specSimplify</code> and <code>Expression.genSimplify()</code>;
     * this version performs generic reductions first, and works in an optimized
     * way, setting any fixed point expression as simplified
     * @return an expression  equivalent to <tt>this</tt>
     */
    default Expression normalize( ) {
        //long startTime = System.currentTimeMillis();
        Expression simpterm = this;
        for (Expression previous = simpterm, temp; ! simpterm.simplified() ; previous = simpterm){
            if ( (temp = simpterm.genSimplify() ) == null) 
                throw new NullPointerException(simpterm+".genSimplify() -> null");
            if ( (simpterm = temp.specSimplify()) == null) 
                throw new NullPointerException(temp+".specSimplify() -> null");
            if (simpterm.equals(previous))
                break;
            //if (System.currentTimeMillis() - startTime > 50000) {
                //System.out.println("normalization time > 50 sec ");
                //System.err.println(simpterm.getClass()+""+simpterm+"\nvs\n"+previous.getClass()+previous);
                 //System.err.println(((wncalculus.bagexpr.BagSum)simpterm).getArgs());
                //}
            //}
        }   
        simpterm.setSimplified(true);
        
        return simpterm;
    }    
         
      /** this (optional) method avoids the same term (shown to be already in normal form)
          to be further simplified
          @return <code>true</code> if the term has been already simplified; the default
          implementation return false
       */
      boolean simplified();
      
      /** this (optional) method allows one to set a given term as already simplified; after its
       *  @param simplified flag denoting whether the term is simplified or not            
       */
      public void setSimplified(boolean simplified);
      
      
      /**
       * check whether this term represents a constant (function)
       * @return <code>true</code> if the term is a constant
       */
      default boolean isConstant() {
          return false; 
      }
      
      /** 
       * @return the domain of <tt>this</tt> term: if the term is an operator then its domain
       *  is inferred from the operands */
     Domain getDomain();
     
     /** 
      * @return the codomain of <tt>this</tt> term: usually, it is inferred from the form of the term
      */
     Domain getCodomain();
         
    
    /**
     * @return the expression's sort, in case it is one-sorted;
     * <code>null</code> otherwise
     */
    default Sort oneSorted() {
        Set<? extends Sort> cdsupp = getCodomain().support();
        
        return cdsupp.size() == 1 ? cdsupp.iterator().next() : null;
    }

     
     /** 
     * @return the @code {List} of sorts appearing in the arity (i.e, domain and codomain)
     * of <tt>this</tt> expression, ordered according to the sort natural ordering
     */
     default ArrayList<Sort> getSorts() {
         Set<Sort> supp = getDomain().support();
         ArrayList<Sort> sortlist = new ArrayList<>(supp);
         getCodomain().support().stream().filter(s -> !supp.contains(s)).forEachOrdered(s -> { sortlist.add(s); });
         Collections.sort(sortlist);
        
         return sortlist;
     }
     
     
     /**
      * print the cardinality (if there is any) of <code>this</code> expression
      * default (stub) implementation - to be redefined if necessary 
      */
     default void printCard() {} 
     
    
    /**
      * @return <code>true</code> if and only if <code>this</code> expression is
      * terminal
    */
     default boolean terminal() {
        return !( this instanceof NonTerminal ) ;
    }
    
     /**
      * @param e an expression
      * @return <code>true</code> if and only if <code>this</code> has the same
      * co-domain as the other
      */
    default boolean sameCoDomain(Expression e) {
        return getCodomain().equals(e.getCodomain());
    }
    
    /**
     * 
     * @param e an expression
     * @return <code>true</code> if and only if <code>this</code> has the same
     * domain as the other
     */
    default boolean sameDomain(Expression e) {
        return getDomain().equals(e.getDomain());
    }
    
    /**
     * 
     * @param e an expression
     * @return <code>true</code> if and only if <code>this</code> has the same
     * domain as the other
     */
    default boolean sameArity(Expression e) {
        return sameDomain(e) && sameCoDomain(e);
    }
        
     /**
      * @param e an expression
      * @return <code>true</code> if and only if <code>this</code> can be left-composed
      * with the other
      */
     default boolean composableTo(Expression e) {
       return getDomain().equals(e.getCodomain());
    }
    
     /**
      * 
      * @param <E> the expression's type
      * @return the expression's type 
      */
     <E extends Expression> Class<E> type();
     
    
    /**
     * performs a cast of <tt>this</tt> expression to a given type
     * @param <E> the type to cast
     * @return <tt>this</tt>
     */
    default <E extends Expression> E cast() {
        Class<E> x = type();
        return x.cast(this);
     }
         
    /**
     *
     * @return a description of <tt>this</tt> expression including the domain
     */
    default String toStringDetailed( ) {
        return this +", "+ getDomain();
    }
    
    /**
     * @return the "null" expression for <tt>this</tt> language, if any;
     * <tt>null</tt> if there is no null expression
     */
    abstract Expression nullExpr () ;
    
    /**
     * could be implemented as default
     * @return <tt>true</tt> if and only if <tt>this</tt> the null expression
     */
    abstract boolean isNull() ;
    
    
    /**
     * optional method: build a guarded expression of the same type as <tt>this</tt>
     * @param f the expression's filter
     * @param e expression
     * @param g expression's guard
     * @return a guarded expression <tt>e</tt>
     * @throws UnsupportedOperationException,IllegalDomain,NullPointerException
     */
     Expression buildGuardedExpr(Guard f, Expression e, Guard g);
    
}
