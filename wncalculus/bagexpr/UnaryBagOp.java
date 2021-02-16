package wncalculus.bagexpr;

import java.util.Map;
import java.util.Objects;
import wncalculus.expr.*;

/**
 * this abstract class represent any unary bag operator
 * @author lorenzo capra
 * @param <E> the bag's elements type
 */
public abstract class UnaryBagOp<E  extends BagExpr, F extends ParametricExpr> implements UnaryOp<E>, BagExpr {
    
    private final E arg; // the function to be transposed
    private boolean simplified;
    
    /**
     * buils a unary bag-operator
     * @param a the bag-expression operand
     */
    public UnaryBagOp (E a) {
        this.arg = a;
    }

    @Override
    public final E getArg() {
        return this.arg;
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
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.arg);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) 
            return true;
        
        if (obj == null || getClass() != obj.getClass()) 
            return false;
        
        return Objects.equals(this.arg, ((UnaryBagOp<?,?>) obj).arg);
    }
    
    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        return getArg().splitDelimiters();
    }

}
