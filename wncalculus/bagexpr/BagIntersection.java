package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.*;

/**
 * this type represents the intersection among bag-expressions
 * @author lorenzo capra
 * @param <E> the bag's type
 */
public abstract class BagIntersection <E  extends BagExpr> extends BinaryBagOp<E> {
    
    /**
     * base constructor: builds a sum of bag-expressions starting from a given collection
     * @param c a collection of bag-expressions
     * @param check arity-check flag
     * @throws IllegalDomain
     */
    public BagIntersection(E left, E right, boolean check) {
        super(left, right, check);
    }
   
    
    @Override
    public E genSimplify() {
      E res = super.genSimplify();
      if (res instanceof BagIntersection<?>) {
          BagIntersection<?> binter = (BagIntersection<?>) res;
          if ( binter.left().isNull() || binter.right().isNull()   ) 
              return build().cast();

          if (binter.left() instanceof Bag<?> && binter.right() instanceof Bag<?>) {
              Map<E,Integer> m = new HashMap<>(((Bag<E>) left()).asMap()); 
              Bag.intersection(m, ((Bag<E>) right()).asMap());
          }
      }

      
      return res;
    } 

    @Override
    public final String symb() {
        return " * ";
    }
    
    /**
     * there is no identity as for the bag-intersection
     * @return <tt>null</tt>
     */
    @Override
    public final E getIde() {
        return null;
    }
    
}
