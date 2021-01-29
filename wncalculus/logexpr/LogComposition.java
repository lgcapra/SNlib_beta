package wncalculus.logexpr;

import wncalculus.expr.CompositionOp;

/**
 * the ADT of composition of set-functions
 * @author Lorenzo Capra
 * @param <E> the operands type
 */
public interface LogComposition<E extends LogicalExpr> extends SetExpr, CompositionOp<E> {
    
    @Override
    default E genSimplify () {
        E res = CompositionOp.super.genSimplify(); // super-type method
        if (res instanceof LogComposition) {
            E left, right;
            LogComposition<E> logcomp = (LogComposition) res;
            if ( (left = logcomp.left()).isFalse() || (right = logcomp.right()).isFalse())
                return  getFalse().cast();
            
            if (left.isConstant() && right.differentFromZero() /*&& left.getDomain().equals(right.getDomain())*/)  //optimization could be generalized
                res = left.clone(right.getDomain()).cast(); 
        }
     
        return res;
    }
        
}
