package wncalculus.expr;

import java.util.*;
import wncalculus.color.ColorClass;

/**
 *
 * @author lorenzo capra
 */
public interface TwoArgs<E extends ParametricExpr, F extends ParametricExpr> extends MultiArgs<E,F> /*NonTerminal*/ {
    /**
     * @return the "left" operand */
    E left();
    
    /**
     * @return the "right" operand */
    E right();
    
    
    /**
     * @return <tt>true</tt> if and only <tt>this</tt> operator is commutative 
     */
    default boolean isCommutative() {
        return false;
    }
    
    @Override
    default E getIde() {
        return null;
    }
    
    
    /**
     * @return either a set or a list, depending on whether the operator is commutative
     * important because @see {isEqual} builds on it
     * 
     */
    @Override
    default List</*? extends*/ E> getArgs() {
        return Arrays.asList(left(),right());
    }
    
    @Override
    default boolean isEqual (Object other) {
          if (this == other)
              return true;
          
          if (other != null && getClass().equals(other.getClass())) {
              TwoArgs<?,?> op = (TwoArgs<?,?>) other;
              return isCommutative() ? new HashSet<>(getArgs()).equals(new HashSet<>(op.getArgs())) :
                      Objects.equals(left(), op.left()) && Objects.equals(right(), op.right());
          }
          
          return false;
     }
    
    /**
     * generically simplifies a binary operator
     * @return an equivalent simplified term
     */
    @Override
    default F genSimplify () {
        Class<E> type = left().type();
        E left  = type.cast(left().normalize( )), right = type.cast(right().normalize( )),
                  ide = getIde();
        if (ide != null) {
            if (left.equals(ide))
                return right.cast();
            
            if (right.equals(ide))
                return left.cast();
        }
        //aggiungere distributività
        return left.equals(left()) && right.equals(right()) ? cast() : buildOp(left, right);
    }
    
    /**
      * checks the equality between <code>this</code>) and another binary operator
      * (assumed not <code>null</code>), based on their operands considered in order
      * @param other a binary op
      * @return <code>true</code> if and only if the operators are equal
      */
     /*default boolean equals (TwoArgs<?,?> other) {
          return other.getClass().equals(getClass())  && ( Objects.equals(left(),  other.left()  ) &&
                   Objects.equals(right(), other.right()) || isCommutative() &&  Objects.equals(left(),  other.right()  ) &&
                   Objects.equals(right(), other.left() ) );
     }*/
     
     /**
     * gives a textual description for a binary operator
     * @return its corresponding String
     */
    @Override
    default String toStringOp () {
        Object left = left(), right = right();
        String sleft = left.toString(), sright = right.toString();
        if ( left instanceof NonTerminal ) 
            sleft = "(" + sleft + ')';
        if (right instanceof NonTerminal) 
            sright = "(" + sright + ')';
        
        return sleft + " " + symb() + " " + sright;
    }
    
    /**
     * builder method
     * @param left the first operand
     * @param right the second operand
     * @return a binary operator of the same type as <code>this</code>
     */
    F buildOp (E left, E right);
    
    
    @Override
    default F buildOp(Collection<? extends E> args) {
        if (args.size() != 2)
            throw new IllegalArgumentException("a binary op requires two arguments");
        
        Iterator<? extends E> x = args.iterator();
        return buildOp(x.next(),x.next());
    }
    
     /**
     * clone <tt>this</tt> operator assuming that the co-domains of the operands and
     * of the operator are the same (to be overridden otherwise)
     * @param newdom the new domain
     * @param newcd the new codomain
     * @param smap the map between old and new split sorts
     * @return a clone of <tt>this</tt> with the specified co-domain
     */
    @Override
    default F clone (final Domain newdom, final Domain newcd) {
        return buildOp(left().clone(newdom, newcd).cast(), right().clone(newdom, newcd). cast());
    }
    
    @Override
    default Map<Sort, Integer> splitDelimiters() {
        ParametricExpr [] args = {left(), right()};
        
        return  ColorClass.mergeSplitDelimiters(Arrays.asList(args));
    }
}
