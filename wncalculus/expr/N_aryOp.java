package wncalculus.expr;

import java.util.*;
import wncalculus.util.Util;

/**
 * This interface defines the ADT for n-ary (n greater than 1) associative operator
 * in which the operands and operator'a types coincide..
 * @author Lorenzo Capra
 * @param <E> the type of operands/operator
 */
public interface N_aryOp<E extends ParametricExpr> extends MultiArgs<E,E>  {
    
    //new versions: calls the super-type method first then try to apply associativity
    //and distributivity
     @Override
     default E genSimplify ( ) {
        //System.out.println("NaryOpOp.gensimplify\n"+this.toStringDetailed());//debug*/
        E res = MultiArgs.super.genSimplify();
        if (res != this)
            return res;
        
        Collection<E> argscopy = associate();
        if (argscopy != null)
            return buildOp(argscopy);
        
        Set<Class<? extends MultiArgs>> distrOps = distributiveOps(); 
        if (!distrOps.isEmpty()) 
            for (E f : getArgs()) 
                if (distrOps.contains(f.getClass()))
                    return distribute((N_aryOp<E>) f);
            
        //System.out.println("(NaryOpOp) --->\n"+argscopy+norm);//debug*/
        return cast() ;
    }
    
              
     /**
      * distribute <code>this</code> operator over a nestedop one of a given type
      * (it works also if nestedop is not actually present in the operands)
      * performs just one step, i.e., considers just the first occurrence (if any)
      * of the nestedop operator
      * @param nestedop the (assumed nestedop) operation over which <code>this</code> operation is distributed
      * @return the distribution of  <code>this</code> over a nestedop operation
      * (the returned object has the same type as the nestedop one);
      * or <code>this</code> if no match is found
      * This implementation collections of the same type as the operator(s).
      */
    default E distribute( N_aryOp<E> nestedop ) {
        Collection<E> nestedArgs = nestedop.getArgs(),
                      new_arg_set = nestedArgs instanceof Set<?> ? new HashSet<>() : new ArrayList<>();
        for (E term : nestedArgs ) {
            Collection<E> iset = Util.copy( getArgs() );
            iset.remove(nestedop); // copy of args without nestedop (efficient if an hashset is used)
            iset.add(term);
            new_arg_set.add( buildOp(iset) );
        }

        return nestedop.buildOp(new_arg_set);
    }
    
    /**
     * applies the associative property to <tt>this</tt> n-ary operator, by searching
     * for occurrences of the same type of operator among its operands
     * (it doesn't operate recursively)
     * @return a collection of operands resulting from applying the associative property
     * <tt>null</tt> if no reduction is done
     */
    default Collection<E> associate () { 
        Collection<? extends E> args = getArgs();
        List<N_aryOp<E>> nested = new ArrayList<>();
        args.stream().filter( expr ->  getClass().isInstance( expr ) ). forEachOrdered(expr -> {
            nested.add((N_aryOp) expr);
         });
        
        if ( nested.isEmpty() )
            return null;
        
        Collection<E> argscopy = Util.copy(args);
        argscopy.removeAll(nested);
        nested.forEach( op -> { argscopy.addAll(op.getArgs()); });
        
        return argscopy;
    }
    
    
    @Override
    default Class<? extends E> argsType() {
        return type();
    }
              
}
