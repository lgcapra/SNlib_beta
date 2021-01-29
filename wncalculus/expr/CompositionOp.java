package wncalculus.expr;

import java.util.*;
import wncalculus.guard.Guard;

/**
 * this interface defines a composition operator between (sorted) terms
 * the type of composition operator may be different from the operands'type
 * (e.g., a composition of set-type expressions meant as bag)
 * @author Lorenzo Capra
 * @param <E> the operands type
 */
public interface CompositionOp<E extends ParametricExpr> extends TwoArgs<E,E> {
   
    final static String SYMB = /*"\u25CB"*/ " . ";

    
    /**
     * @param optk an operator type
     * @return <code>true</code> if and only if this composition is left-associative
     * w.r.t. the specified operator*/
    boolean isLeftAssociative (Class<? extends SingleArg> optk);
    
    
    @Override
    default E genSimplify () {
        E res = TwoArgs.super.genSimplify(); //the operands have been normalzed
        if (res instanceof CompositionOp) {
            CompositionOp<?> cop = (CompositionOp<?>) res;
            E left = cop.left().cast(), right = cop.right().cast();
            
            UnaryOp<E> uop;
            if (left instanceof UnaryOp<?> && isLeftAssociative((uop = (UnaryOp<E>) left).getClass()) ) 
                return  uop.buildOp( buildOp( uop.getArg(), right).cast());
            
            N_aryOp<E> op;
            Set<Class<? extends MultiArgs>> distribOps = distributiveOps();
            if (left instanceof MultiArgs<?,?> && distribOps.contains(left.getClass()) ) {
                op = (N_aryOp<E>) left;
                Collection<E> args = op.getArgs() instanceof Set<?> ? new HashSet<>() : new ArrayList<>();
                op.getArgs().forEach(e -> { args.add( buildOp(e, right).cast()); });
                
                return  op.buildOp(args);
            }

            if (right instanceof MultiArgs && distribOps.contains(right.getClass()) ) {
                op =( N_aryOp<E>) right;
                Collection<E> args = op.getArgs() instanceof Set<?> ? new HashSet<>() : new ArrayList<>();
                op.getArgs().forEach(e -> { args.add(buildOp(left, e).cast()); });
                
                return op.buildOp(args);
            }
            // if the operands are guarded expressions the possible outer filter/guards are put outside 
            Guard g;
            GuardedExpr<E> g_expr;
            if (left instanceof GuardedExpr<?> ) {
                g_expr = (GuardedExpr<E>) left;
                if ( (g = g_expr.filter() ) !=null)
                    return buildGuardedExpr(g, buildOp(g_expr.withoutFilter(), right), null).cast();
            } 
            if (right instanceof GuardedExpr<?> && ! right.terminal() ) { //we put the guard outside only if right is not a terminal
                g_expr = (GuardedExpr<E>) right;
                if ( (g = g_expr.guard() ) != null)
                    return buildGuardedExpr(null, buildOp(left, g_expr.withoutGuard()), g). cast();
            }
        }    
    
        return res;
    }
    
    
    @Override
    default Domain getDomain() {
        return right().getDomain();
    }
    
    @Override
    default Domain getCodomain() {
        return left().getCodomain();
    }
    
    /**
     * overrides the ancestor method, taking into account the fact that the
     * left operands' domain has to match the right operand's codomain
     * @param newdom the composition's new domain
     * @param newcd the composition's new co-domain
     * @param smap the map between orginal and split sorts
     * @return a clone of <tt>this</tt> composition with the new co-domains
     */
    @Override
    default E clone (final Domain newdom, final Domain newcd) {
        E left = left(), right = right();
        Domain left_dom = left.getDomain(); // the left operands' domain = right operand's codom
        if (left.getCodomain().equals(left_dom)) //optimization
            left_dom = newcd;
        else if (left_dom.equals(right.getDomain()))
            left_dom = newdom;
        else { //we re-build the left's domain
            HashMap<Sort,Integer > copy = new HashMap<>();
            left_dom.asMap().entrySet().forEach (e -> {
                Sort s = e.getKey(), ns;
                boolean not_found = newdom.mult(s) == 0 && newcd.mult(s) == 0; // the old sort of the left's domain is not present in the new (co-)domain
                if (not_found && ( (ns = newdom.sort(s.name() )) != null || (ns = newcd.sort(s.name() )) != null) ) // in the new (co-)domain there is a sort with that name
                    s = ns;
                copy.put(s, e.getValue());
            });

            left_dom  =  new Domain(copy);
        }
        
        return buildOp(left().clone(left_dom, newcd). cast(), right().clone(newdom, left_dom). cast());
    }
    
    @Override
    default String symb() {
        return SYMB;
    }
    
    @Override
    default Class<E> argsType() {
        return left().type();
    }
       
 }
