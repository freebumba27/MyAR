package my.com.ar.myar.ar.uiwidgets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import my.com.ar.myar.ar.base.CameraModel;
import my.com.ar.myar.ar.base.PitchAzimuthCalculator;
import my.com.ar.myar.ar.base.Utilities;
import my.com.ar.myar.ar.base.Vector;
import my.com.ar.myar.ar.data.ARData;
import my.com.ar.myar.ar.data.ScreenPosition;
import my.com.ar.myar.ar.uiobjs.PaintableCircle;
import my.com.ar.myar.ar.uiobjs.PaintableLine;
import my.com.ar.myar.ar.uiobjs.PaintablePosition;
import my.com.ar.myar.ar.uiobjs.PaintableRadarPoints;
import my.com.ar.myar.ar.uiobjs.PaintableText;


/**
 * This class will visually represent a radar screen with a radar radius and blips on the screen in their appropriate
 * locations.
 */
public class Radar {
    public static final float RADIUS = 80;

    private static final int LINE_COLOR = Color.argb(150, 0, 0, 220);
    private static float PAD_X = 10;
    private static float PAD_Y = 20;
    private static final int RADAR_COLOR = Color.parseColor("#9665c881");
    private static final int RADAR_BORDER_COLOR = Color.argb(150, 0, 0, 0);
    private static final int TEXT_COLOR = Color.argb(0, 0, 0, 0);
    private static final int TEXT_SIZE = 16;

    private static ScreenPosition leftRadarLine = null;
    private static ScreenPosition rightRadarLine = null;
    private static PaintablePosition leftLineContainer = null;
    private static PaintablePosition rightLineContainer = null;
    private static PaintablePosition circleContainer = null;
    private static PaintablePosition circleBorderContainer = null;

    private static PaintableRadarPoints radarPoints = null;
    private static PaintablePosition pointsContainer = null;

    private static PaintableText paintableText = null;
    private static PaintablePosition paintedContainer = null;

    private boolean showDirectionText = false;
    private boolean showZoomText = true;
    private boolean rotateRadar = true;

    private static final Vector origin = new Vector(0, 0, 0);
    private static final Vector upVertical = new Vector(0, 1, 0);
    private final Vector originVector = new Vector();
    private final Vector upVector = new Vector();
    float currentAngle = 0;
    protected final float[] originVectorArray = new float[3];
    protected final float[] upVectorArray = new float[3];

    public Radar(Canvas canvas) {
        if (leftRadarLine == null) leftRadarLine = new ScreenPosition();
        if (rightRadarLine == null) rightRadarLine = new ScreenPosition();

        // adjust radar to bottom of screen
        PAD_X = canvas.getWidth() - RADIUS * 2 - 10;
        PAD_Y = (canvas.getHeight() / 2) - RADIUS;
    }

    public void updateAngle() {
        originVector.set(origin);
        originVector.prod(ARData.getRotationMatrix());
        originVector.get(originVectorArray);

        upVector.set(upVertical);
        upVector.prod(ARData.getRotationMatrix());
        upVector.get(upVectorArray);
        currentAngle = Utilities.getAngle(originVectorArray[0], originVectorArray[1], upVectorArray[0], upVectorArray[1]);
        currentAngle -= 90;
        currentAngle *= -1;
        if (!rotateRadar) {
            currentAngle = 0;
        }

    }

    /**
     * Draw the radar on the given Canvas.
     *
     * @param canvas Canvas to draw on.
     * @throws NullPointerException if Canvas is NULL.
     */
    public void draw(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        canvas.rotate(currentAngle, PAD_X + RADIUS, PAD_Y + RADIUS);

        //Update the pitch and bearing using the phone's rotation matrix
        if (!rotateRadar) {
            PitchAzimuthCalculator.calcPitchBearing(ARData.getRotationMatrix());
        } else {
            PitchAzimuthCalculator.calcPitchBearing(ARData.getRotationMatrix(), currentAngle);
        }
        ARData.setAzimuth(PitchAzimuthCalculator.getAzimuth());
        ARData.setPitch(PitchAzimuthCalculator.getPitch());

        //Update the radar graphics and text based upon the new pitch and bearing
        drawRadarCircle(canvas);
        drawRadarCircleBorder(canvas);
        //drawRadarLines(canvas);
        drawRadarArc(canvas);
        drawRadarText(canvas);
        canvas.rotate(-currentAngle, PAD_X + RADIUS, PAD_Y + RADIUS);
        drawRadarPoints(canvas);
    }

    private void drawRadarCircle(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (circleContainer == null) {
            PaintableCircle paintableCircle = new PaintableCircle(RADAR_COLOR, RADIUS, true);
            circleContainer = new PaintablePosition(paintableCircle, PAD_X + RADIUS, PAD_Y + RADIUS, 0, 1);
        }
        circleContainer.paint(canvas);
    }

    private void drawRadarCircleBorder(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (circleBorderContainer == null) {
            PaintableCircle paintableCircle = new PaintableCircle(RADAR_BORDER_COLOR, RADIUS, false);
            circleBorderContainer = new PaintablePosition(paintableCircle, PAD_X + RADIUS, PAD_Y + RADIUS, 0, 1);
        }
        circleBorderContainer.paint(canvas);
    }

    private void drawRadarPoints(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (radarPoints == null) radarPoints = new PaintableRadarPoints();

        if (pointsContainer == null) {
            pointsContainer = new PaintablePosition(radarPoints,
                    PAD_X,
                    PAD_Y,
                    -ARData.getAzimuth(),
                    1);
        } else {
            pointsContainer.set(radarPoints,
                    PAD_X,
                    PAD_Y,
                    -ARData.getAzimuth(),
                    1);
        }

        pointsContainer.paint(canvas);
    }

    @SuppressWarnings("unused")
    private void drawRadarLines(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        //Left line
        if (leftLineContainer == null) {
            leftRadarLine.set(0, -RADIUS);
            leftRadarLine.rotate(-CameraModel.DEFAULT_VIEW_ANGLE / 2);
            leftRadarLine.add(PAD_X + RADIUS, PAD_Y + RADIUS);

            float leftX = leftRadarLine.getX() - (PAD_X + RADIUS);
            float leftY = leftRadarLine.getY() - (PAD_Y + RADIUS);
            PaintableLine leftLine = new PaintableLine(LINE_COLOR, leftX, leftY);
            leftLineContainer = new PaintablePosition(leftLine,
                    PAD_X + RADIUS,
                    PAD_Y + RADIUS,
                    0,
                    1);
        }
        leftLineContainer.paint(canvas);

        //Right line
        if (rightLineContainer == null) {
            rightRadarLine.set(0, -RADIUS);
            rightRadarLine.rotate(CameraModel.DEFAULT_VIEW_ANGLE / 2);
            rightRadarLine.add(PAD_X + RADIUS, PAD_Y + RADIUS);

            float rightX = rightRadarLine.getX() - (PAD_X + RADIUS);
            float rightY = rightRadarLine.getY() - (PAD_Y + RADIUS);
            PaintableLine rightLine = new PaintableLine(LINE_COLOR, rightX, rightY);
            rightLineContainer = new PaintablePosition(rightLine,
                    PAD_X + RADIUS,
                    PAD_Y + RADIUS,
                    0,
                    1);
        }
        rightLineContainer.paint(canvas);
    }

    private void drawRadarArc(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        Paint paint = new Paint();
        final RectF rect = new RectF();
        rect.set(PAD_X, PAD_Y, PAD_X + RADIUS * 2, PAD_Y + RADIUS * 2);
        paint.setColor(Color.parseColor("#6401641d"));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(rect, -114, 47, true, paint);
    }

    private void drawRadarText(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        //Direction text
        if (showDirectionText) {
            int range = (int) (ARData.getAzimuth() / (360f / 16f));
            String dirTxt = "";
            if (range == 15 || range == 0) dirTxt = "N";
            else if (range == 1 || range == 2) dirTxt = "NE";
            else if (range == 3 || range == 4) dirTxt = "E";
            else if (range == 5 || range == 6) dirTxt = "SE";
            else if (range == 7 || range == 8) dirTxt = "S";
            else if (range == 9 || range == 10) dirTxt = "SW";
            else if (range == 11 || range == 12) dirTxt = "W";
            else if (range == 13 || range == 14) dirTxt = "NW";
            int bearing = (int) ARData.getAzimuth();
            radarText(canvas,
                    "" + bearing + ((char) 176) + " " + dirTxt,
                    (PAD_X + RADIUS),
                    (PAD_Y - 5),
                    true
            );
        }

        //Zoom text
        if (showZoomText) {
            radarText(canvas,
                    formatDist(ARData.getRadius() * 1000),
                    (PAD_X + RADIUS),
                    (PAD_Y + RADIUS * 2 - 10),
                    false
            );
        }
    }

    private void radarText(Canvas canvas, String txt, float x, float y, boolean bg) {
        if (canvas == null || txt == null) throw new NullPointerException();

        if (paintableText == null)
            paintableText = new PaintableText(txt, TEXT_COLOR, TEXT_SIZE, bg);
        else paintableText.set(txt, TEXT_COLOR, TEXT_SIZE, bg);

        if (paintedContainer == null)
            paintedContainer = new PaintablePosition(paintableText, x, y, 0, 1);
        else paintedContainer.set(paintableText, x, y, 0, 1);

        paintedContainer.paint(canvas);
    }

    private static String formatDist(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    private static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }
}
