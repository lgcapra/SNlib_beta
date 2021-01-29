package wncalculus.expr;

import java.util.ArrayList;
import java.util.Collection;

/**
 * this interface is the super-type for unary (sorted) operators
 * in which the operandand operator's types coincide
 * @author Lorenzo Capra
 * @param <E> the operand's type
 */
public interface UnaryOp<E extends ParametricExpr> extends SingleArg<E,E> {
         
    @Override
    default E genSimplify() {
        E res = SingleArg.super.genSimplify();
        if (res == this) {
            E arg = getArg();
            if (isInvolution() && getClass().equals(arg.getClass())  )
                res = ((SingleArg<E,E>)arg).getArg() ;
            else {
                MultiArgs<E, E> op;
                if (arg instanceof MultiArgs && isDistributive( (op= ((MultiArgs)arg )).getClass() ) ) {
                    Collection<E> nargs = new ArrayList<>();
                    op.getArgs().forEach( e -> { nargs.add( buildOp(e).cast() );  });//casts Expression to E
                    res =  op.buildOp(nargs);
                }
            }
        }
        
        return res;
    }
    
}
