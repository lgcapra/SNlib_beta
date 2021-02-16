package wncalculus.expr;

import java.util.*;

/**
 * This interface represents any operator with more than one operands, of the same type.
 * The operator and operandd's type may be different (e.g, the operands sets and the operator bag)
 * @author lorenzo capra
 * @param <E> the type of operands
 * @param <F> the operator's type
 */
public interface MultiArgs<E extends ParametricExpr, F extends ParametricExpr> extends NonTerminal {

    /**
     * normalizes a list of expressions (preserving the order) operating in a destructive way:
     * the normalized expressions type is forced to the specified one
     * clears the list, if any normalized term is equal to the null expression
     * @param <E> the expressions domain
     * @param arglist the list of expressions
     * @return  <code>true</code> if and only if the list has been changed
     * @throws NoSuchElementException if the list is empty
     */
    @Override
    default F genSimplify() {
        Collection<E> args = getArgs();
        E ide = getIde();
        Expression nullexpr = nullExpr(); 
        boolean changed = false, ide_is_nullexpr = ide != null && ide.equals(nullexpr);
        Collection<E> copy = args instanceof Set<?> ? new HashSet<>() : new ArrayList<>();
        for (E e : args) {
            E normal = e.normalize().cast();
            if (normal.equals(nullexpr) && !ide_is_nullexpr) 
                return nullexpr.cast();
            
            if (!e.equals(normal)) 
                changed = true;
            if (! normal.equals(ide) ) //particular case: ide null
                copy.add(normal);
        }
        if (copy.isEmpty()) //only possibile if all args equal to ide (non-null)
             return ide.cast();  // should be safe
        
        return changed || copy.size() != args.size() ? buildOp(copy) : cast();
    }
    
    /**
     * @return the (unmodifiable) collection of arguments of this operator
     * @throws UnmodifiableCollectionException
     */
     Collection</*? extends*/ E> getArgs();
     
     /**
      * @return the identity for this operator; <code>null</code> if it has no identity
      */
     E getIde();
     
     /**
      * 
      * @return the type of operands 
      */
     Class<? extends E> argsType();
     
    /**
     * @return the set of operators' types over which if <tt>this</tt> operator
     * can be distibuted
     */
     default Set<Class<? extends MultiArgs >> distributiveOps () {
         return Collections.emptySet();
     }
     
    @Override
    default F clone (final Domain newdom, final Domain newcd) {
         return buildOp(clone(getArgs(), newdom, newcd, type()));
    }
    
    /**
     * creates a new list of expressions of an arbitrary type setting a new sort support
     * @param <E> the expressions domain
     * @param arglist the list of expressions to be cloned
     * @param s_map a map between old and new sorts
     * @param type the expressions'  type
     * @param dom the expressions' new domain
     * @param cd the expressions' new codomain
     * @return a copy of the passed list of expressions with the specified new sorts and co-domains
     */
    public static <E extends ParametricExpr> Collection<E> clone(Collection<? extends E> arglist, Domain dom, Domain cd, Class<E> type) {
        Collection<E> res = arglist instanceof List ? new ArrayList<>() : new HashSet<>();
        arglist.forEach(f -> { res.add(type.cast(f.clone(dom, cd))); });
        
        return res;
    }
    
     /**
      * builds an operator from a given collection of operands
      * @param args the operand list
      * @return an operator built from the operand list
      * @throws ClassCastException if the type of any operands is not the right 
      * @throws NoSuchElementException if the collection is empty
     */
     F buildOp(Collection<? extends E> args);
     
     /**
      * builds an operator from a given collection of operands
      * @param args the operand list
      * @return an operator built from the operand list
      * @throws ClassCastException if the type of any operands is not the right one
     */
     default F buildOp(E ... args) {
         return buildOp(Arrays.asList(args));
     }
     
      /**
      * checks the equality between <code>thsi</code> n-ary operator and
      * another (assumed not <code>null</code>), based on the particular collection
      * implementing the operands
      * @param other another operator
      * @return <code>true</code> if and only if two terms represent the same
      * n-ary operator
      */
     default boolean isEqual (Object other) {
          return this == other || other != null && getClass().equals(other.getClass()) && getArgs().equals(((MultiArgs)other).getArgs() ) ;   
     }
     
     
     /**
     * gives a textual description for a n-ary operator
     * @return the corresponding String
     */
     default String toStringOp()  {
        String opsymb = symb();
        StringBuilder res = new StringBuilder("(");
        getArgs().forEach(x -> { res.append(x).append(opsymb); });
        res.setCharAt(res.length() - 1, ')');
        int length = res.length();
        res.replace(length - opsymb.length(), length, ")");
        
        return res.toString();
    }
     
     @Override
     default Domain getDomain(){
         return getArgs().iterator().next().getDomain();
     }
     
     @Override
     default Domain getCodomain(){
         return getArgs().iterator().next().getCodomain();
     }
}
