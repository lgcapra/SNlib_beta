package wncalculus.classfunction;

import java.util.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.Interval;

/**
 * this class defines the empty-set constant function
 * @author lorenzo capra
 */
public final class Empty extends ConstantFunction  {
    
    private static final Map<ColorClass,Empty> VALUES = new HashMap<>();
    
    private final static String SYMBOL ="0";
    
    /**
     * build an <tt>Empty</tt> class-function
     * @param cc the function's color-class
     * @return an <tt>Empty</tt> class-function of the specified color
     */
    public static final Empty getInstance(ColorClass cc) {
        Empty f;
        if ((f = VALUES.get(cc)) == null) 
            VALUES.put(cc, f = new Empty(cc)) ;
        
        return f;
    }
    
    /**
     * creates an empty class function with an associated default color constraint
     */
    private Empty(ColorClass cc) {
        super(cc);
    }
    
    /**
     * @return the constraint [0,0]
     */
    @Override
    public Interval card() {
        return new Interval(0,0);
    }
    
   
    @Override
    public SetFunction baseCompose(SetFunction right) {
        return this;
    }
    
    @Override
    public String toString() {
        return SYMBOL + '_'+ getSort().name();
    }

 
    @Override
    public boolean isFalse() {
        return true;
    }
    
    @Override
    public SetFunction copy(ColorClass newcc) {
       return getInstance(newcc); 
    }
    
}
