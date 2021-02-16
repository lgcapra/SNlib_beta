package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.*;

/**
 * this class represents the super-type of n-ary operations on bag-expressions
 * @author lorenzo capra
 */
public abstract class BinaryBagOp<E  extends BagExpr> implements TwoArgs<E,E>, BagExpr {
    
    private final E left, right;
    private boolean simplified;
    
    /**
     * base constructor: builds a sum of bag-expressions starting from a given collection
     * @param c a collection of bag-expressions
     * @param check arity-check flag
     * @throws IllegalDomain
     */
    public BinaryBagOp(E left, E right,  boolean check) {
        if (check)
            Expressions.checkArity(left,right);
        this.left = left;
        this.right = right;
    }
    
    @Override
    public final E left() {
        return left;
    }
    
    @Override
    public final E right() {
        return right;
    }
    
    @Override
    public final String toString() {
        return TwoArgs.super.toStringOp();
    }

    @Override
    public final boolean simplified() {
        return this.simplified;
    }

    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
            
    @Override
    public final boolean equals(Object o) {
        return TwoArgs.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.left);
        hash = 53 * hash + Objects.hashCode(this.right);
        return hash;
    }

}
