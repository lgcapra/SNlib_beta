package wncalculus.logexpr;

import wncalculus.bagexpr.BagExpr;

/**
 * This interface represents expressions (function) representing parametric sets
 * @author lorenzo capra
 */
public interface SetExpr extends LogicalExpr {
    
     
    /** 
     * provides a bag-view of this expression
     * @param <E> the expression's type
     * @return a singleton bag corresponding to <tt>this</tt> expression
     * @throws ClassCastException if the type of e is not compatible with <code>this</code> type
     */
    default BagExpr asBag() {
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * @return the lower-bound of <code>this</code> tuple's cardinality; <code>null</code> if, for any reason, the
     * cardinality cannot be computed 
    */
    default Integer cardLb() {
        return null;
    }    
    
}
