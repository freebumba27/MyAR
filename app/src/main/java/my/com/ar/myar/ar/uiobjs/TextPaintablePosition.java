package my.com.ar.myar.ar.uiobjs;


import android.graphics.Canvas;

/**
 * This class extends PaintableObject and adds the ability to rotate and scale.
 * 
 */
public class TextPaintablePosition extends PaintablePosition {
    
    public TextPaintablePosition(PaintableObject drawObj, float x, float y, float rotation, float scale) {
    	super(drawObj, x, y, rotation, scale);
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void paint(Canvas canvas) {
    	if (canvas==null || obj==null) throw new NullPointerException();
    	
        canvas.save();
        canvas.translate(objX+obj.getWidth()/2, objY);
        canvas.rotate(objRotation);
        canvas.scale(objScale,objScale);
        canvas.translate(-(obj.getWidth()/2), 0);
        obj.paint(canvas);
        canvas.restore();
	}
}
