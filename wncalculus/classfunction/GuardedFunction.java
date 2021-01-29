package wncalculus.classfunction;

import java.util.Objects;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.guard.Guard;
import wncalculus.expr.GuardedExpr;
import wncalculus.expr.NonTerminal;

/**
 * this class defines guarded class-functions
 * @author lorenzo
 */
public final class GuardedFunction implements GuardedExpr<ClassFunction>, ClassFunction, NonTerminal {
    private final Guard guard;
    private final ClassFunction function;
    boolean simplified;
    
    public GuardedFunction(ClassFunction f, Guard g) {
         this.function = f;
         this.guard = g;
    } 
    
    
    @Override
    public Guard guard() {
       return this.guard;
    }

    @Override
    public ClassFunction expr() {
        return this.function;
    }

   
    @Override
    public GuardedFunction clone(Domain newdom, Domain newcd) {
        return new GuardedFunction(this.function.clone(newdom).cast(), this.guard.clone(newdom));
    }

    @Override
    public int splitDelim() {
        return Math.max(this.function.splitDelim(), this.guard.splitDelimiters().get( getSort() ));
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
    public ClassFunction nullExpr() {
        return this.function.nullExpr().cast();
    }

    @Override
    public ColorClass getSort() {
        return this.function.getSort();
    }

    @Override
    public GuardedFunction copy(ColorClass newcc) {
        return newcc == getSort() ? this : new GuardedFunction(this.function.copy(newcc),this.guard.clone(new Domain(newcc,1))); 
    }

    @Override
    public GuardedFunction setDefaultIndex() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Guard filter() {
       return null;
    }

    @Override
    public Class<ClassFunction> type() {
        return ClassFunction.class;
    }
    
    @Override
    public boolean equals (Object o) {
        return GuardedExpr.super.equalGuardedExpr(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.guard);
        hash = 97 * hash + Objects.hashCode(this.function);
        return hash;
    }

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String toString() {
        return GuardedExpr.super.toStringGuardedExpr();
    }

    @Override
    public ClassFunction build(Guard f, Guard g) {
        return new GuardedFunction(this.function ,g);
    }

    @Override
    public boolean isNull() {
        return false;
    }
    
}
