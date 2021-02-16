package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.*;


/**
 * This class implements the transposing of a bag of bexpr-tuples
 * @author Lorenzo Capra
 * @param <E> the bag-expression's type
 * @param <F> bag's base-type
 */
public abstract class BagTranspose<E extends BagExpr, F extends ParametricExpr> extends UnaryBagOp<E,F> {

    /**
     * build the transpose of a bag-expression
     * @param b a bag-expression
     * @throws ClassCastException if the arguments' type is not transposable
     */
    public BagTranspose (E b) {
        super(b);
        Object o =(Transposable)b;
    }
    
    @Override
    public E genSimplify() {
        E res = super.genSimplify();
        if ( res != this || ! ( getArg() instanceof Bag<?>) )
            return res;
        
        Bag<E> bag = (Bag<E>) getArg();
        if (bag.isEmpty()) 
            return build().cast(); //should be right!
        
       Map<E,Integer> trmap = new HashMap<>();
       bag.asMap().entrySet().forEach( e -> { trmap.put( ((Transposable)e.getKey()).buildTransp().cast() , e.getValue() ); });
        
       return bag.build(trmap).cast(); 
    }
    
    /*
    the domain and the codomain are "inverted"
    */
    @Override
    public final Domain getDomain() {
        return super.getCodomain();
    }

    @Override
    public final Domain getCodomain() {
        return super.getDomain();
    }
    
    @Override
    public final String symb() {
        return "'";
    }
    
    @Override
    public final String toString() {
        return toStringPost();
    }

    @Override
    public final boolean isInvolution() {
        return true;
    }
    
     /**
     * clone <tt>this</tt> transpose; the method is overridden because the
     * arity of the operand and that of the operator are "inverted"
     * of the operator are the same (to be overridden otherwise)
     * @param newdom the new domain
     * @param newcd the new codomain
     * @param smap the map between old and new split sorts
     * @return a clone of <tt>this</tt> with the specified co-domain
     */
    @Override
    public final E clone (final Domain newdom, final Domain newcd) {
        return buildOp(getArg().clone(newcd, newdom). cast()).cast();
    }

}
