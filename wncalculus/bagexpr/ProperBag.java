package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.ParametricExpr;

/**
 * this class defines a unary operator that extratcs the positive part of a bag
 * @author lorenzo capra
 * @param <E> the bag's elements type
 */
public abstract class ProperBag<E  extends BagExpr, F extends ParametricExpr> extends UnaryBagOp<E,F> {
    
    /**
     * build a proper-bag from a bag-expression
     * @param b a bag-expression
     */
    public ProperBag(E b) {
        super(b);
    }
    
    @Override
    public final boolean isInvolution() {
        return false;
    }

    @Override
    public final String symb() {
        return "proper";
    }
    
    @Override
    public E specSimplify() {
        E arg = getArg();
        if (arg instanceof Bag){
            Bag<E> b = (Bag<E>) arg;
            if (b.isProper())
                return b.cast();
            
            Set<? extends E> supp =  b.properSupport();
            if (supp != null) {
                HashMap<E, Integer> bprop = new HashMap<>();
                supp.forEach(e -> { bprop.put(e, b.mult(e)); });
                
                return b.build(bprop).cast();
            }
        }
        
        return  cast();
    }
    
    @Override
    public final String toString() {
        return toStringOp();
    }
    
}