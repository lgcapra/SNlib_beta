package wncalculus.wnbag;

import wncalculus.classfunction.ClassFunction;
import wncalculus.expr.ParametricExpr;
import wncalculus.tuple.AbstractTuple;

/**
 * this interface represents any SN arc function built of function-tuples
 * tuple components may set- or bag-functions, dependending on the type parameter
 * @author lorenzo capra
 */
public interface SNfunctionTuple<E extends ClassFunction> extends ParametricExpr  {
    
    /**
     * @return <tt>true</tt> if and only if <tt>this</tt> expression is or may be seen as a tuple
     * (e.g. a <tt>AllTuple</tt> 
     */
    default boolean isTuple() {
        return false;
    }
    
    /**
     * 
     * @return <tt>true</tt> if and only if <tt>this</tt> expression is (may be seen) either a tuple or a base sum of tuples 
     */
    default boolean isElementarySum() {
        return false; 
    }
    
    /**
     * @return the tuple view of <tt>this</tt> function; <tt>null</tt> if
     * <tt>this</tt> cannot be seen as a tuple 
     */
    default <F extends SNfunctionTuple<E>> AbstractTuple<E,F> asTuple() {
        return null;
    }
    
}
