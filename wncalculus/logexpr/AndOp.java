package wncalculus.logexpr;

import wncalculus.expr.N_aryOp;
import wncalculus.expr.NonTerminal;
import wncalculus.tuple.FunctionTuple;
import static wncalculus.logexpr.LogicalExprs.checkComplementary;

/**
 * this interface defines the super-type of boolean operators that can be viewed as "conjunctions"
 * @author lorenzo capra
 * @param <E> the operands type
 */
public interface AndOp<E extends LogicalExpr> extends LogicalExpr, N_aryOp<E> { 
    
    /**
     * provides a sufficient condition for a given "And" expressions to be "false";
     * @return  <code>true</code> if the operator is "false" 
     */
    @Override
    default boolean isFalse() {
        return getArgs().contains(getFalse());
    }
    
    /**
     * provides a sufficient condition for an "And" expressions to be "true";
     * @return  <code>true</code> if the operator is "true"
     */
    @Override
    default boolean isTrue() {
        LogicalExpr True = getTrue();
        
        return getArgs().stream().allMatch( t -> True.equals(t) );
    }
    
  
    @Override
    default E getIde() {
       Class<E> type = type();
       
       return type.cast(getTrue());
    }
    
     /**
     * @return <code>true</code> if and only if <code>this</code> expression is
     * either terminal or an "And" operator
     */
    @Override
    default boolean isAndForm( ) {
        return true ;
    }
    
    /**
     * @return true if and only if <code>this</code> operator is formed solely by terminals
     * other than the boolean constants
     */
    @Override
    default boolean isNormalAndForm( ) {
        return getArgs( ).stream().noneMatch( t -> t instanceof NonTerminal || t.isFalse() || t.isTrue());
    }
    
    
    @Override
    default LogicalExpr getZero() {
        return getFalse();
    }
    
    @Override
    default E genSimplify ( ) {
        //System.out.println("(AndOp) " + this);
        E res = N_aryOp.super.genSimplify(); // super-type method
        //System.out.println("(AndOp) -->\n" + res);
        if (res instanceof AndOp<?> && ! type().equals( FunctionTuple.class) && checkComplementary( ((AndOp<E>) res).getArgs()))  
            return getFalse().cast();
        //System.out.println("(AndOp) ->\n" + res); //debug
        return res;
    }
    
}
