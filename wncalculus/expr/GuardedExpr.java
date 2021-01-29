package wncalculus.expr;

import java.util.Objects;
import wncalculus.guard.And;
import wncalculus.guard.Guard;
import wncalculus.guard.True;

/**
 * This interface defines expressions prefixed/suffixed by a guard
 * (the prefix is called filter, the suffix guard)
 * @param the expression's type
 * @author lorenzo capra
 */
public interface GuardedExpr<E extends Expression> extends Expression {

    /**performs the "and" between guards knowing that eg == null means eg == true
     * @param p1 a guard
     * @param p2 another guard
     * @return the "AND" between guards
     */
    public static Guard join(Guard p1, Guard p2) {
        if (p1 == null || p1 == p2) {
            return p2;
        }
        if (p2 == null) {
            return p1;
        }
        return And.factory(p1, p2);
    }
    
    /**
     * 
     * @return the expression's suffix guard; <tt>null</tt> if there is no guard 
     */
    Guard guard(); 
    
    /**
     * 
     * @return the expression's prefix guard; <tt>null</tt> if there is no filter 
     */
    Guard filter();
    
    /**
     * "optional" implementation
     * @return the guarded expression; <tt>null</tt> if, for any reasons, it cannot
     * be aceessed
     */
    E expr();
    
    @Override
    default E genSimplify() {
        Guard g = guard(), f = filter();
        if (g != null) {
            g = (Guard) g.normalize();
            if (g.isFalse())
                return nullExpr().cast();
            
            if (g instanceof True)
                g = null;
        }
        if ( f != null) {
            f = (Guard) f.normalize();
            if (f.isFalse())
                return nullExpr().cast();
            
            if (f instanceof True)
                f = null;
        }
        E e = expr();
        if ( e != null) {
            e = e.normalize().cast();
            if ( e.equals(nullExpr()) )
                return nullExpr().cast();
        }
        //the filter and the guard are trivial and the expression is not null
        if ( f == null  && g == null  && e != null)
            return e;
        
        if (f == filter() && g == guard() && e == expr()) { //no simplication done
            if (e instanceof GuardedExpr<?> ) { // the embedded expr is a guarded expr
               GuardedExpr<E> g_expr = (GuardedExpr<E> ) e;
               Guard jf = join(f, g_expr.filter()) , jg = join(g, g_expr.guard()); 

               return g_expr.expr() != null ? buildGuardedExpr(jf, g_expr.expr(), jg). cast() : g_expr.build(jf, jg);
            }
            
            return cast(); //return this term
        }
        
        return e != null ? buildGuardedExpr(f,e,g).cast() : build(f,g);
    }
    
    public static <E extends Expression> void checkArity(Guard filter, E expr, Guard guard) {
         if (filter != null && expr != null && !expr.getCodomain().equals(filter.getDomain() ) ) 
            throw new IllegalDomain("filter's domain must be the same as function's co-domain");    
        
        if (guard != null &&  expr != null &&! expr.getDomain().equals(guard.getDomain() ) ) 
            throw new IllegalDomain("guards's domain must be the same as function'co-domain"); 
    }
    
    /**
     * 
     * @return the expression's domain
     * @throws NullPointerException of both the guard and the guarded expr are null
     */
    @Override
    default Domain getDomain() {
        return guard() != null ? guard().getDomain() : expr().getDomain();
    }

    /**
     * 
     * @return the expression's codomain
     * @throws NullPointerException of both the filter and the guarded expr are null
     */
    @Override
    default Domain getCodomain() {
        return filter() != null ? filter().getDomain() : expr().getCodomain();
    }
    
    default boolean equalGuardedExpr(Object o ) {
       GuardedExpr<E> ge;
       return this == o || getClass().equals(o.getClass()) && Objects.equals(filter(), (ge = (GuardedExpr<E>) o).filter())
               && Objects.equals(guard(), ge.guard())  && Objects.equals(expr(), ge.expr());
    }
    
    /**
     * 
     * @return the textual representation of a guarded expression
     */
    default String toStringGuardedExpr() {
        return (filter() == null ? "" : "[" + filter() + ']')  + expr() + (guard() == null ? "" : "[" +guard() + ']');
    }
    
    
    /**
     * @return a copy of <code>this</code> with a trivial guard
     * <code>this</code> if the current guard is trivial the guarded expression is <tt>null</tt>
     */
    default E withoutGuard() {
        if (guard() == null )
            return cast();
        
        return (expr() != null ? buildGuardedExpr(filter(), expr(), null) : build(filter(),null)). cast();
    }
    
    /**
     * @return a copy of <code>this</code> with a trivial filter
     * <code>this</code> if the current filter is trivial or the guarded expression is <tt>null</tt>
     */
    default E withoutFilter() {
        if (filter() == null )
            return cast();
        
        return (expr() != null ? buildGuardedExpr(null, expr(), guard()) : build(null, guard())). cast();
    }
    
        
    /**
     * 
     * @param f a filter
     * @param g a guard
     * @return an expression like that embedded in <tt>this</tt> with the given filter/guard
     */
    E build(Guard f, Guard g);
     
}
