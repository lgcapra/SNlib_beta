package wncalculus.bagexpr;

/**
 * this class implements the bag's scalar product
 * @author lorenzo capra
 * @param <E> the bag's domain
 */
public abstract class ScalarProd<E extends BagExpr> extends UnaryBagOp<E,E> {

    private final int k; 
    
    public ScalarProd(E arg, int coeff) {
        super(arg);
        this.k = coeff;
    }
    
    /**
     * @return the scalar value 
     */
    public final int k() {
        return k;
    }

    @Override
    public final String symb() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public final E genSimplify() {
         E res = super.genSimplify();
         if ( res instanceof ScalarProd<?> ) {
             ScalarProd<E> scp = (ScalarProd<E>)res;
             E arg = scp.getArg();
             if (scp.k == 1) 
                return arg;

            if (scp.k == 0)
                return build().cast();

            if (arg instanceof ScalarProd<?>) {
                ScalarProd<E> sparg = (ScalarProd<E>) arg;
                return scalarProdFactory( sparg.getArg(), sparg.k * scp.k).cast();
            }

            if (arg instanceof Bag) {
                Bag<E> b = (Bag<E>) arg;
                return (b.isEmpty() ? b : b.build(Bag.scalarprod(b.asMap(), scp.k))).cast();
            }
        }
        
        return res;
    } 
    
    @Override
    public final boolean isInvolution() {
        return false;
    }
    
    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj) && this.k == ((ScalarProd<?>)obj).k;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        return super.hashCode() + 97 * hash + this.k;
    }
    
    @Override
    public final String toString() {
        return "" +this.k + "*("+getArg()+')';
    }
    
}
