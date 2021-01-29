package wncalculus.wnbag;

import java.util.Map;
import wncalculus.guard.Guard;
import wncalculus.expr.*;

/**
 * this class defines arc-function expressions prefixed/suffixed by guards
 * @author lorenzo capra
 */
public final class GuardedArcFunction implements BagfunctionTuple, GuardedExpr<BagfunctionTuple>, NonTerminal {
   
    private final Guard filter,guard;
    private final BagfunctionTuple expr;
    private boolean simplified;
    
    /**
     * build a guarded arc-function expression
     * @param f filter
     * @param e arc-function expression
     * @param g guard
     * @param check arity-check flag
     * @return a guarded arc-function, if either the filter or the guard are not nulle,
     * the embedde expression otherwise
     */
    public GuardedArcFunction (Guard f, BagfunctionTuple e, Guard g, boolean check) {
        if (check)
            GuardedExpr.checkArity(f, e, g);
        this.filter = f;
        this.guard  = g;
        this.expr   = e;
    }
    
    public GuardedArcFunction (Guard f, BagfunctionTuple e, Guard g) {
        this(f,e,g,true/*false*/);
    }

    
    //public static BagfunctionTuple factory (Guard f, BagfunctionTuple e, Guard g) {
        //return f!= null || g!=null ? new GuardedArcFunction (f, e, g) : e;
    //}
    
    @Override
    public ParametricExpr clone(Domain newdom, Domain newcd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public Guard guard() {
        return this.guard;
    }

    @Override
    public Guard filter() {
        return this.filter;
    }

    @Override
    public BagfunctionTuple expr() {
        return this.expr;
    }
    
    
    @Override
    public Class<BagfunctionTuple> type() {
        return BagfunctionTuple.class;
    }
    
    @Override
    public String toString () {
        return GuardedExpr.super.toStringGuardedExpr() ;
    }

    @Override
    public BagfunctionTuple build(Guard f, Guard g) {
        return new GuardedArcFunction(f, this.expr, g, false);
    }

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
