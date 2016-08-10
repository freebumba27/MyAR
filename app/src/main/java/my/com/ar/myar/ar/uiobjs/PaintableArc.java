package my.com.ar.myar.ar.uiobjs;

import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * This class extends PaintableObject to draw a circle with a given radius.
 * 
 */
public class PaintableArc extends PaintableObject {
    private int color = 0;
    private boolean fill = false;
    private float left = 0;
    private float top = 0;
    private float right = 0;
    private float bottom = 0;
    private RectF oval = null;
    
    public PaintableArc(int color, float left, float top, float right, float bottom, boolean fill) {
    	set(color, left, top, right, bottom, fill);
    }
    
    public void set(int color, float left, float top, float right, float bottom, boolean fill) {
        this.color = color;
        this.fill = fill;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        
        oval = new RectF(left, top, right, bottom);
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public void paint(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	
        setFill(fill);
        setColor(color);
        paintArc(canvas, oval, 0, 360, true);
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public float getWidth() {
        return left-right;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public float getHeight() {
        return top-bottom;
    }
}
