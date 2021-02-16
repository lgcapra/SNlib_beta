package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.*;

/**
 * the super-type of the composition of bags of (linear) functions
 * @author lorenzo capra
 * @param <E> the operands type (bag_comp-expressions)
 * @param <F> bag_comp's elements base-type
 */
public abstract class BagComp<E extends BagExpr/*Composable<E> */> extends BinaryBagOp<E> implements  CompositionOp<E> {

     
     /**
     * build a composition between (expressions over) bag_comp-of-functions
     * @param left the bleft-hand bag_comp-expression
     * @param right the bright-hand bag_comp-expression
     * @throws UnsupportedOperationException if the functions are not "linear"
     * @throws IllegalDomain if the operands cannot be composed due their (co-)domains
     */
    public BagComp(E left, E right) {
        super(left,right,false);
        if (! left.composableTo(right) )
            throw new IllegalDomain();
     }
    
    
    @Override
    public boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return ScalarProd.class.isAssignableFrom(optk); // alternative: false
    }
        
    @Override
    public E genSimplify () {
        E res = CompositionOp.super.genSimplify();
        if (res instanceof BagComp<?>) {
            BagComp<E> bag_comp = (BagComp<E>) res;
            if (bag_comp.left().isNull() || bag_comp.right().isNull())
                return build().cast();
         
            if (bag_comp.left() instanceof Bag<?>) { //either operand is a bag_comp
                Bag<E> lb = (Bag<E>) bag_comp.left();
                if ( lb.isConstant() ) { // composition between a constant and a constant-size bag_comp
                    Integer c = bag_comp.right().card();        
                    if (c != null)
                        return  scalarProdFactory( lb.clone(bag_comp.right().getDomain()).cast(), c).cast();
                }
                HashMap<E,Integer> resmap = new HashMap<>(); //result's map
                lb.asMap(). entrySet().forEach(x -> { 
                    Bag.add(resmap, buildOp( x.getKey(),bag_comp.right() ),x.getValue()); });
                res = lb.build(resmap).cast();
            }
            else if ( bag_comp.right() instanceof Bag) {
                Bag<E> rb = (Bag<E>) bag_comp.right();
                HashMap<E,Integer> resmap = new HashMap<>(); //result's map
                rb.asMap(). entrySet().forEach(y -> {
                    Bag.add(resmap, buildOp(bag_comp.left(),y.getKey()), y.getValue()); });
                res = rb.build(resmap).cast();
            }   
        }
 
        return res;
    }
    
}
