package wncalculus.tuple;

import java.util.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.*;

/**
 * this abstract class defines function-tuples prefixed by a filter
 * @author Lorenzo Capra
 */
public final class GuardedTuple implements FunctionTuple , GuardedExpr<FunctionTuple>, NonTerminal {
    
    private final FunctionTuple  expr;
    private final Guard  filter, guard; //null means true!
    private boolean simplified;
    
    /**
     * creates a <tt>FilteredTuple</tt>
     * @param f a filter
     * @param g a guard
     * @param expr a function-tuple
     * @param check arity-check flag
     */
    public GuardedTuple (Guard f, FunctionTuple e, Guard g, boolean check) {
        if (check)
            GuardedExpr.checkArity(f, e, g);
        this.filter = f;
        this.expr   = e;
        this.guard  = g;
    }
    
    public GuardedTuple (Guard f, FunctionTuple e, Guard g) {
        this(f, e, g, true/*false*/);
    }
    
    /*public static FunctionTuple factory(Guard f, FunctionTuple expr, Guard g) {
        //the following reductions are done at supertype level (could be introduced for efficiency)
        if (f == null || f.isTrue() && g== null || g.isTrue())
            return expr;
        if (expr instanceof Tuple) {
            Tuple t = (Tuple) expr;
            return t.build(AbstractTuple.join(f,t.filter()), AbstractTuple.join(g,t.guard()), t.getDomain());
        }
        
        return new GuardedTuple(f, expr, g);
    }*/
    
    /**
     * creates a <tt>GuardedTuple</tt> with a given filter (and a default guard)
     * @param f a filter
     * @param expr a function-tuple
     */
     public GuardedTuple (Guard f, FunctionTuple expr) {
        this(f, expr, null);
    }
    
    @Override
    public FunctionTuple specSimplify() {
        if (this.expr instanceof AllTuple) {
            Domain codom = getCodomain();
            return new Tuple(this.filter, codom, AllTuple.toMap( codom), this.guard, getDomain());
        }
        //i due casi seguenti sono gestiti nel supertipo
        /*if (this.expr instanceof Tuple) 
            return ((Tuple) this.expr).embedBetween(this.filter, this.guard).cast();
                    
        if (this.expr instanceof GuardedTuple ) { 
           GuardedTuple g_expr = (GuardedTuple ) this.expr;
           
           return new GuardedTuple(AbstractTuple.join(this.filter, g_expr.filter), g_expr.expr, AbstractTuple.join(this.guard, g_expr.guard));
        }*/
        
        if (this.expr instanceof TupleSum ) {
            TupleSum orexpr = (TupleSum ) this.expr;
            Collection<GuardedTuple> cgf = new ArrayList<>();
            orexpr.getArgs().forEach(fx -> { cgf.add(new GuardedTuple (this.filter, fx, this.guard)); });

            return TupleSum.factory(cgf, orexpr.disjoined());
        }

        
        return this;     
    }
    
    @Override
    public GuardedTuple clone(Domain newdom, Domain newcd) {
        Guard nf = null, ng = null;
        if (this.filter!=null)
            nf = this.filter.clone(newcd);
        if (this.guard!=null)
            ng = this.guard.clone(newdom);
        
        return new GuardedTuple (nf, (FunctionTuple) this.expr.clone(newdom,newcd), ng);
    }

    
    @Override
    public  boolean isFalse() {
        return this.filter != null && this.filter.isFalse() || this.expr.isFalse();
    }

    @Override
    public boolean differentFromZero() {
        return (this.filter == null || this.filter.differentFromZero()) && this.expr.differentFromZero();
    }

    @Override
    public boolean isTrue() {
        return this.filter == null && this.expr.isTrue();
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        Map<Sort,Integer>  delims  =  new HashMap<>();
        if (this.filter != null)
            delims.putAll(this.filter.splitDelimiters());
        if (this.guard != null)
            delims.putAll(this.guard.splitDelimiters());
        
        ColorClass.joinDelims(delims, this.expr.splitDelimiters());
        
        return delims;
    }

    @Override
    public boolean isConstant() {
        return  this.expr.isConstant() && this.guard == null;
    }
    
    @Override
    public boolean equals(Object other) {
        return GuardedExpr.super.equalGuardedExpr(other);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.expr);
        hash = 29 * hash + Objects.hashCode(this.filter);
        hash = 29 * hash + Objects.hashCode(this.guard);
        return hash;
    }
    

    @Override
    public String toString () {
        return GuardedExpr.super.toStringGuardedExpr() ;
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
    public FunctionTuple expr() {
        return this.expr;
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
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public FunctionTuple build(Guard f, Guard g) {
        return new GuardedTuple (f, this.expr, g);
    }
    
}
