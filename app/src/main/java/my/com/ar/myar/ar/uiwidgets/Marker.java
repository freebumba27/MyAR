package my.com.ar.myar.ar.uiwidgets;


import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import java.text.DecimalFormat;

import my.com.ar.myar.ar.AugmentedReality;
import my.com.ar.myar.ar.base.CameraModel;
import my.com.ar.myar.ar.base.Utilities;
import my.com.ar.myar.ar.base.Vector;
import my.com.ar.myar.ar.data.ARData;
import my.com.ar.myar.ar.data.PhysicalLocation;
import my.com.ar.myar.ar.uiobjs.PaintableBox;
import my.com.ar.myar.ar.uiobjs.PaintableBoxedText;
import my.com.ar.myar.ar.uiobjs.PaintableGps;
import my.com.ar.myar.ar.uiobjs.PaintableObject;
import my.com.ar.myar.ar.uiobjs.PaintablePosition;
import my.com.ar.myar.ar.uiobjs.TextPaintablePosition;


/**
 * This class will represent a physical location and will calculate it's visibility and draw it's text and
 * visual representation accordingly. This should be extended if you want to change the way a Marker is viewed.
 */
public class Marker implements Comparable<Marker> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");

    private static final Vector origin = new Vector(0, 0, 0);
    private static final Vector upVertical = new Vector(0, 1, 0);

    private final Vector screenPositionVector = new Vector();
    private final Vector tmpSymbolVector = new Vector();
    private final Vector tmpVector = new Vector();
    private final Vector tmpTextVector = new Vector();
    private final float[] distanceArray = new float[1];
    private final float[] locationArray = new float[3];
    private final float[] screenPositionArray = new float[3];

    private float initialY = 0.0f;

    private volatile static CameraModel cam = null;

    private volatile PaintableBoxedText textBox = null;
    private volatile PaintablePosition textContainer = null;

    protected final float[] symbolArray = new float[3];
    protected final float[] textArray = new float[3];

    //Container for the circle or icon symbol
    protected volatile PaintableObject gpsSymbol = null;
    protected volatile PaintablePosition symbolContainer = null;
    //Unique identifier of Marker
    protected String name = null;
    //Marker's physical location (Lat, Lon, Alt)
    protected volatile PhysicalLocation physicalLocation = new PhysicalLocation();
    //Distance from camera to PhysicalLocation in meters
    protected volatile double distance = 0.0;
    //Is within the radar
    protected volatile boolean isOnRadar = false;
    //Is in the camera's view
    protected volatile boolean isInView = false;
    //Symbol's (circle in this case) X, Y, Z position relative to the camera's view
    //X is left/right, Y is up/down, Z is In/Out (unused)
    protected final Vector symbolXyzRelativeToCameraView = new Vector();
    //Text box's X, Y, Z position relative to the camera's view
    //X is left/right, Y is up/down, Z is In/Out (unused)
    protected final Vector textXyzRelativeToCameraView = new Vector();
    //Physical location's X, Y, Z relative to the camera's location
    protected final Vector locationXyzRelativeToPhysicalLocation = new Vector();  //relative position vector from user position to POI location
    //Marker's default color
    protected int color = Color.WHITE;

    //Used to debug the touching mechanism
    private static boolean debugTouchZone = false;
    private static PaintableBox touchBox = null;
    private static PaintablePosition touchPosition = null;

    //Used to debug the collision mechanism
    private static boolean debugCollisionZone = false;
    private static PaintableBox collisionBox = null;
    private static PaintablePosition collisionPosition = null;

    private Object object = null;

    public Marker(String name, double latitude, double longitude, double altitude, int color) {
        set(name, latitude, longitude, altitude, color);
    }

    public void setTag(Object object) {
        this.object = object;
    }

    public Object getTag() {
        return object;
    }

    /**
     * Set the objects parameters. This should be used instead of creating new objects.
     *
     * @param name      String representing the Marker.
     * @param latitude  Latitude of the Marker in decimal format (example 39.931269).
     * @param longitude Longitude of the Marker in decimal format (example -75.051261).
     * @param altitude  Altitude of the Marker in meters (>0 is above sea level).
     * @param color     Color of the Marker.
     */
    public synchronized void set(String name, double latitude, double longitude, double altitude, int color) {
        if (name == null) throw new NullPointerException();

        this.name = name;
        this.physicalLocation.set(latitude, longitude, altitude);
        this.color = color;
        this.isOnRadar = false;
        this.isInView = false;
        this.symbolXyzRelativeToCameraView.set(0, 0, 0);
        this.textXyzRelativeToCameraView.set(0, 0, 0);
        this.locationXyzRelativeToPhysicalLocation.set(0, 0, 0);
        this.initialY = 0.0f;
    }

    /**
     * Get the name of the Marker.
     *
     * @return String representing the new of the Marker.
     */
    public synchronized String getName() {
        return this.name;
    }

    /**
     * Get the color of this Marker.
     *
     * @return int representing the Color of this Marker.
     */
    public synchronized int getColor() {
        return this.color;
    }

    /**
     * Get the distance of this Marker from the current GPS position.
     *
     * @return double representing the distance of this Marker from the current GPS position.
     */
    public synchronized double getDistance() {
        return this.distance;
    }

    /**
     * Get the initial Y coordinate of this Marker. Used to reset after collision detection.
     *
     * @return float representing the initial Y coordinate of this Marker.
     */
    public synchronized float getInitialY() {
        return this.initialY;
    }

    /**
     * Get the whether the Marker is inside the range (relative to slider on view)
     *
     * @return True if Marker is inside the range.
     */
    public synchronized boolean isOnRadar() {
        return this.isOnRadar;
    }

    /**
     * Get the whether the Marker is inside the camera's view
     *
     * @return True if Marker is inside the camera's view.
     */
    public synchronized boolean isInView() {
        return this.isInView;
    }

    /**
     * Get the position of the Marker in XYZ.
     *
     * @return Vector representing the position of the Marker.
     */
    public synchronized Vector getScreenPosition() {
        symbolXyzRelativeToCameraView.get(symbolArray);
        textXyzRelativeToCameraView.get(textArray);
        //float x = (symbolArray[0] + textArray[0])/2;
        //float y = (symbolArray[1] + textArray[1])/2;
        //float z = (symbolArray[2] + textArray[2])/2;
        float x = symbolArray[0];
        float y = symbolArray[1];
        float z = symbolArray[2];

        // If the marker has been visible, use the text box to offset the position.
        //if (textBox!=null) y += (textBox.getHeight()/2);

        // offset for symbol and textbox height mismatch
        if (textBox != null)
            y -= ((gpsSymbol.getHeight() - textBox.getHeight()) / 2);

        screenPositionVector.set(x, y, z);
        return screenPositionVector;
    }

    /**
     * Get the the location of the Marker in XYZ.
     *
     * @return Vector representing the location of the Marker.
     */
    public synchronized Vector getLocation() {
        return this.locationXyzRelativeToPhysicalLocation;
    }

    public synchronized float getHeight() {
        if (symbolContainer == null || textContainer == null) return 0f;
        return symbolContainer.getHeight() + textContainer.getHeight();
    }

    public synchronized float getWidth() {
        if (symbolContainer == null || textContainer == null) return 0f;
        float w1 = textContainer.getWidth();
        float w2 = symbolContainer.getWidth();
        return (w1 > w2) ? w1 : w2;
    }

    /**
     * Update the matrices and visibility of the Marker.
     *
     * @param canvas Canvas to use in the CameraModel.
     * @param addX   Adder to the X position.
     * @param addY   Adder to the Y position.
     */
    public synchronized void update(Canvas canvas, float addX, float addY) {
        if (canvas == null) throw new NullPointerException();

        if (cam == null) cam = new CameraModel(canvas.getWidth(), canvas.getHeight(), true);
        cam.set(canvas.getWidth(), canvas.getHeight(), false);
        cam.setViewAngle(CameraModel.DEFAULT_VIEW_ANGLE);
        populateMatrices(cam, addX, addY);
        updateRadar();
        updateView();
    }

    private synchronized void populateMatrices(CameraModel cam, float addX, float addY) {
        if (cam == null) throw new NullPointerException();

        // Find symbol position given the rotation matrix
        tmpSymbolVector.set(origin);
        tmpSymbolVector.add(locationXyzRelativeToPhysicalLocation);
        tmpSymbolVector.prod(ARData.getRotationMatrix());

        // Find the text position given the rotation matrix
        tmpTextVector.set(upVertical);
        tmpTextVector.add(locationXyzRelativeToPhysicalLocation);
        tmpTextVector.prod(ARData.getRotationMatrix());

        cam.projectPoint(tmpSymbolVector, tmpVector, addX, addY);
        symbolXyzRelativeToCameraView.set(tmpVector);
        cam.projectPoint(tmpTextVector, tmpVector, addX, addY);
        textXyzRelativeToCameraView.set(tmpVector);
    }

    private synchronized void updateRadar() {
        isOnRadar = false;

        float range = ARData.getRadius() * 1000;
        float scale = range / Radar.RADIUS;
        locationXyzRelativeToPhysicalLocation.get(locationArray);
        float x = locationArray[0] / scale;
        float y = locationArray[2] / scale; // z==y Switched on purpose 
        symbolXyzRelativeToCameraView.get(symbolArray);
        if ((symbolArray[2] < -1f) && ((x * x + y * y) < (Radar.RADIUS * Radar.RADIUS))) {
            isOnRadar = true;
        }
    }

    private synchronized void updateView() {
        isInView = false;

        symbolXyzRelativeToCameraView.get(symbolArray);
        float x1 = symbolArray[0] + (getWidth() / 2);
        float y1 = symbolArray[1] + (getHeight() / 2);
        float x2 = symbolArray[0] - (getWidth() / 2);
        float y2 = symbolArray[1] - (getHeight() / 2);
        if (x1 >= -1 && x2 <= (cam.getWidth())
                &&
                y1 >= -1 && y2 <= (cam.getHeight())
                ) {
            isInView = true;
        }
    }

    /**
     * Calculate the relative position of this Marker from the given Location.
     *
     * @param location Location to use in the relative position.
     * @throws NullPointerException if Location is NULL.
     */
    public synchronized void calcRelativePosition(Location location) {
        if (location == null) throw new NullPointerException();

        // Update the markers distance based on the new location.
        updateDistance(location);

        // An elevation of 0.0 probably means that the elevation of the
        // POI is not known and should be set to the users GPS height
        if (physicalLocation.getAltitude() == 0.0)
            physicalLocation.setAltitude(location.getAltitude());

        // Compute the relative position vector from user position to POI location
        PhysicalLocation.convLocationToVector(location, physicalLocation, locationXyzRelativeToPhysicalLocation);
        this.initialY = locationXyzRelativeToPhysicalLocation.getY();
        updateRadar();
    }

    private synchronized void updateDistance(Location location) {
        if (location == null) throw new NullPointerException();

        Location.distanceBetween(physicalLocation.getLatitude(), physicalLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distanceArray);
        distance = distanceArray[0];
    }

    /**
     * Tell if the x/y position is on this marker (if the marker is visible)
     *
     * @param x float x value.
     * @param y float y value.
     * @return True if Marker is visible and x/y is on the marker.
     */
    public synchronized boolean handleClick(float x, float y) {
        if (!isOnRadar || !isInView) return false;
        return isPointOnMarker(x, y, this);
    }

    /**
     * Determines if the marker is on this Marker.
     *
     * @param marker Marker to test for overlap.
     * @return True if the marker is on Marker.
     */
    public synchronized boolean isMarkerOnMarker(Marker marker) {
        return isMarkerOnMarker(marker, true);
    }

    /**
     * Determines if the marker is on this Marker.
     *
     * @param marker  Marker to test for overlap.
     * @param reflect if True the Marker will call it's self recursively with the opposite arguments.
     * @return True if the marker is on Marker.
     */
    private synchronized boolean isMarkerOnMarker(Marker marker, boolean reflect) {
        marker.getScreenPosition().get(screenPositionArray);
        float x = screenPositionArray[0];
        float y = screenPositionArray[1];
        boolean middleOfMarker = isPointOnMarker(x, y, this);
        if (middleOfMarker) return true;

        /*
        float halfWidth = marker.getWidth()/2;
        float halfHeight = marker.getHeight()/2;

        float x1 = x - halfWidth;
        float y1 = y - halfHeight;
        boolean upperLeftOfMarker = isPointOnMarker(x1,y1,this);
        if (upperLeftOfMarker) return true;

        float x2 = x + halfWidth;
        float y2 = y1;
        boolean upperRightOfMarker = isPointOnMarker(x2,y2,this);
        if (upperRightOfMarker) return true;

        float x3 = x1;
        float y3 = y + halfHeight;
        boolean lowerLeftOfMarker = isPointOnMarker(x3,y3,this);
        if (lowerLeftOfMarker) return true;

        float x4 = x2;
        float y4 = y3;
        boolean lowerRightOfMarker = isPointOnMarker(x4,y4,this);
        if (lowerRightOfMarker) return true;
		*/
        //If reflect is True then reverse the arguments and see if this Marker is on the marker.
        return (reflect) ? marker.isMarkerOnMarker(this, false) : false;
    }

    /**
     * Determines if the point is on this Marker.
     *
     * @param x      X point.
     * @param y      Y point.
     * @param marker Marker to determine if the point is on.
     * @return True if the point is on Marker.
     */
    private synchronized boolean isPointOnMarker(float x, float y, Marker marker) {
        marker.getScreenPosition().get(screenPositionArray);
        float myX = screenPositionArray[0];
        float myY = screenPositionArray[1];
        float adjWidth = marker.getWidth() / 2;
        float adjHeight = marker.getHeight() / 2;

        float x1 = myX - adjWidth;
        float y1 = myY - adjHeight;
        float x2 = myX + adjWidth;
        float y2 = myY + adjHeight;

        if (x >= x1 && x <= x2 && y >= y1 && y <= y2) return true;

        return false;
    }

    /**
     * Draw this Marker on the Canvas
     *
     * @param canvas Canvas to draw on.
     * @throws NullPointerException if the Canvas is NULL.
     */
    public synchronized void draw(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        //If not visible then do nothing
        if (!isOnRadar || !isInView) return;

        //Draw the Icon and Text
        if (debugTouchZone) drawTouchZone(canvas);
        if (debugCollisionZone) drawCollisionZone(canvas);
        drawIcon(canvas);
        drawText(canvas);
    }

    protected synchronized void drawCollisionZone(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        getScreenPosition().get(screenPositionArray);
        float x = screenPositionArray[0];
        float y = screenPositionArray[1];

        float width = getWidth();
        float height = getHeight();
        float halfWidth = width / 2;
        float halfHeight = height / 2;

        float x1 = x - halfWidth;
        float y1 = y - halfHeight;

        float x2 = x + halfWidth;
        float y2 = y1;

        float x3 = x1;
        float y3 = y + halfHeight;

        float x4 = x2;
        float y4 = y3;

        if (AugmentedReality.DEBUG) Log.w("collisionBox", "ul (x=" + x1 + " y=" + y1 + ")");
        if (AugmentedReality.DEBUG) Log.w("collisionBox", "ur (x=" + x2 + " y=" + y2 + ")");
        if (AugmentedReality.DEBUG) Log.w("collisionBox", "ll (x=" + x3 + " y=" + y3 + ")");
        if (AugmentedReality.DEBUG) Log.w("collisionBox", "lr (x=" + x4 + " y=" + y4 + ")");

        if (collisionBox == null)
            collisionBox = new PaintableBox(width, height, Color.WHITE, Color.RED);
        else collisionBox.set(width, height);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]) + 90;

        if (collisionPosition == null)
            collisionPosition = new PaintablePosition(collisionBox, x1, y1, currentAngle, 1);
        else collisionPosition.set(collisionBox, x1, y1, currentAngle, 1);
        collisionPosition.paint(canvas);
    }

    protected synchronized void drawTouchZone(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (gpsSymbol == null) return;

        symbolXyzRelativeToCameraView.get(symbolArray);
        textXyzRelativeToCameraView.get(textArray);
        float x1 = symbolArray[0];
        float y1 = symbolArray[1];
        float x2 = textArray[0];
        float y2 = textArray[1];
        float width = getWidth();
        float height = getHeight();
        float adjX = (x1 + x2) / 2;
        float adjY = (y1 + y2) / 2;
        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]) + 90;
        adjX -= (width / 2);
        adjY -= (gpsSymbol.getHeight() / 2);

        if (AugmentedReality.DEBUG) Log.w("touchBox", "ul (x=" + (adjX) + " y=" + (adjY) + ")");
        if (AugmentedReality.DEBUG)
            Log.w("touchBox", "ur (x=" + (adjX + width) + " y=" + (adjY) + ")");
        if (AugmentedReality.DEBUG)
            Log.w("touchBox", "ll (x=" + (adjX) + " y=" + (adjY + height) + ")");
        if (AugmentedReality.DEBUG)
            Log.w("touchBox", "lr (x=" + (adjX + width) + " y=" + (adjY + height) + ")");

        if (touchBox == null) touchBox = new PaintableBox(width, height, Color.WHITE, Color.GREEN);
        else touchBox.set(width, height);

        if (touchPosition == null)
            touchPosition = new PaintablePosition(touchBox, adjX, adjY, currentAngle, 1);
        else touchPosition.set(touchBox, adjX, adjY, currentAngle, 1);
        touchPosition.paint(canvas);
    }

    protected synchronized void drawIcon(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (gpsSymbol == null) gpsSymbol = new PaintableGps(36, 36, true, getColor());

        textXyzRelativeToCameraView.get(textArray);
        symbolXyzRelativeToCameraView.get(symbolArray);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = currentAngle + 90;

        if (symbolContainer == null)
            symbolContainer = new PaintablePosition(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);
        else symbolContainer.set(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);

        symbolContainer.paint(canvas);
    }

    protected synchronized void drawText(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        String textStr = null;
        if (distance < 1000.0) {
            textStr = name + " (" + DECIMAL_FORMAT.format(distance) + "m)";
        } else {
            double d = distance / 1000.0;
            textStr = name + " (" + DECIMAL_FORMAT.format(d) + "km)";
        }

        textXyzRelativeToCameraView.get(textArray);
        symbolXyzRelativeToCameraView.get(symbolArray);
//        float maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, MyApplication.getInstance().getResources().getDisplayMetrics());
        float maxHeight = Math.round(canvas.getHeight() / 10f) + 1;
        if (textBox == null)
            textBox = new PaintableBoxedText(textStr, Math.round(maxHeight / 2f) + 1, 300);
        else textBox.set(textStr, Math.round(maxHeight / 2f) + 1, 300); //// TODO: 8/25/15

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = currentAngle + 90;

        float x = textArray[0] - (textBox.getWidth() / 2);
        float y = textArray[1];

        if (textContainer == null)
            textContainer = new TextPaintablePosition(textBox, x, y, angle, 1);
        else ((TextPaintablePosition) textContainer).set(textBox, x, y, angle, 1);
        textContainer.paint(canvas);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int compareTo(Marker another) {
        if (another == null) throw new NullPointerException();

        return name.compareTo(another.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean equals(Object marker) {
        if (marker == null || name == null) throw new NullPointerException();

        return name.equals(((Marker) marker).getName());
    }
}
