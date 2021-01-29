
package wncalculus.bagexpr;

import java.util.Map;
import wncalculus.expr.*;

/**
 * the super-type of expressions on bags (of expressions) defined over a given domain;
 * notice that a BagExpr may not be a Bag
 * NOTE currently bags are not parametric expression, even if in perspective they could become
 * @author Lorenzo Capra
 * @param <E> the bag's domain
 */
public interface BagExpr extends ParametricExpr, Transposable {
    
    /**
     * @return the bag's elements type 
     */
    <E extends ParametricExpr> Class<E> bagType();
    
    /** bag-builder method
     * @param m a map
     * @return a bag of the given arity
     */
    
    Bag<? extends ParametricExpr> build(Map<? extends ParametricExpr, Integer> m);
     
    /** bag-builder method
     * @param d bag's domain
     * @param cd bag's codomain
     * @return a bag of the given arity
     */
    <E extends ParametricExpr> Bag<E> build(Domain d, Domain cd) ;
    
    
    default <E extends ParametricExpr> Bag<E> build()  {
        return build (getDomain(), getCodomain());
    }
    
    
    @Override
    default  BagExpr nullExpr() {
    	return build();
    }
    
    @Override
    default boolean isNull() {
        return false;
    }
    
    /**
     * 
     * @return the cardinality of a bag-expression; <tt>null</tt> if it cannot be computed 
     */
    default Integer card() {
        return null;
    }
    
    
    BagExpr scalarProdFactory(BagExpr arg, int k);

    
}
