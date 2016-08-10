package my.com.ar.myar.ar.uiwidgets;


import android.graphics.Bitmap;
import android.graphics.Canvas;

import my.com.ar.myar.ar.base.Utilities;
import my.com.ar.myar.ar.uiobjs.MarkerPaintablePosition;
import my.com.ar.myar.ar.uiobjs.PaintableIcon;


/**
 * This class extends Marker and draws an icon instead of a circle for it's visual representation.
 * 
 */
public class IconMarker extends Marker {
    private Bitmap bitmap = null;

    public IconMarker(String name, double latitude, double longitude, double altitude, int color, Bitmap bitmap) {
        super(name, latitude, longitude, altitude, color);
        this.bitmap = bitmap;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public void drawIcon(Canvas canvas) {
    	if (canvas==null || bitmap==null) throw new NullPointerException();

        if (gpsSymbol==null) gpsSymbol = new PaintableIcon(bitmap,bitmap.getWidth(),bitmap.getHeight());

        textXyzRelativeToCameraView.get(textArray);
        symbolXyzRelativeToCameraView.get(symbolArray);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = currentAngle + 90;
        
        if (symbolContainer==null) symbolContainer = new MarkerPaintablePosition(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);
        else ((MarkerPaintablePosition)symbolContainer).set(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);
        
        symbolContainer.paint(canvas);
    }
}
