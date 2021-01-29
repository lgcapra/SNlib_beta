package wncalculus.tuple;

import java.util.Collections;
import java.util.Set;
import wncalculus.expr.*;
import wncalculus.logexpr.LogComposition;

/**
 * this class defines the composition between WN (set) function-tuples
 * @author Lorenzo Capra
 */
public final class TupleComposition implements FunctionTuple, LogComposition<FunctionTuple> {
    
    private FunctionTuple left , right;
    private boolean simplified;
    
    /** creates a new composition between function-tuples after having possibly checked that the (co)domains are consistent
     * if the left operand is a Tuple its "reduce guard" flag is set up
     * @param left the left operand
     * @param right the right operand
     * @param check domain-check flag
     */
    public TupleComposition (FunctionTuple left , FunctionTuple right, boolean check) {
        if (/*check && */!left.getDomain().equals(right.getCodomain())) 
            throw new IllegalDomain("the domain of the left function and the codomain of the right one do not correspond!");
        
        setArgs(left,right);
    }
    
    /** creates a new composition between function-tuples assuming that the (co)domains are consistent
     * @param left left function
     * @param right right function
     */
    public TupleComposition (FunctionTuple left , FunctionTuple right)  {
        this(left, right, false);
    }
    
    
    
    @Override
    public TupleComposition buildOp(FunctionTuple left, FunctionTuple right) {
        return new TupleComposition( left, right);
    }
    
    private void setArgs(FunctionTuple left, FunctionTuple right) {
        this.left   = left;
        if (left instanceof Tuple) //new
            ((Tuple)this.left).setReduceGuard(true);
        this.right  = right;
    }
    
    @Override
    public FunctionTuple specSimplify() {
        //System.err.println("***\n"+this);
        FunctionTuple res = this;
        Tuple tleft;
        if (this.left instanceof Tuple && (tleft = (Tuple) this.left).filter() == null  && this.right.isTuple() &&
                ( res = tleft.tupleCompose(this.right.asTuple().cast()) ) instanceof Tuple) 
            ((Tuple)res).setReduceGuard(false); // the composition has been solved: default condition (not needed?)
        //System.err.println("\n-->\n"+res);
        return res;
    }
    
    @Override
    public boolean differentFromZero() {
        return false;
    }

    @Override
    public final FunctionTuple left() {
        return this.left;
    }

    @Override
    public final FunctionTuple right() {
        return this.right;
    }
    
    @Override
    public final boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return optk.equals(TupleProjection.class); 
    }


    @Override
    public boolean equals(Object o) {
        return LogComposition.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.left.hashCode();
        hash = 53 * hash + this.right.hashCode();
        
        return hash;
    }

    @Override
    public final String toString() {
        return LogComposition.super.toStringOp();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
    @Override
    public Set<Class<? extends MultiArgs >> distributiveOps () {
        return Collections.singleton(TupleSum.class);
    }
    
}
