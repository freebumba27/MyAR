package my.com.ar.myar.ar.base;

/**
 * A general utilities class.
 * 
 */
public abstract class Utilities {

    private Utilities() { }
    
    public static final float getAngle(float center_x, float center_y, float post_x, float post_y) {
        float tmpv_x = post_x - center_x;
        float tmpv_y = post_y - center_y;
        float d = (float) Math.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
        float cos = tmpv_x / d;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        angle = (tmpv_y < 0) ? angle * -1 : angle;

        return angle;
    }
    
    public static final float getAngle(float center_x, float center_y, float post_x, float post_y, int orientation) {
        float tmpv_x = post_x - center_x;
        float tmpv_y = post_y - center_y;
        if (orientation == 1) {
        	// potrait - switch x,y axis
			float temp = tmpv_x;
			tmpv_x = tmpv_y;
			tmpv_y = temp;
        }
        float d = (float) Math.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
        float cos = tmpv_x / d;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        angle = (tmpv_y < 0) ? angle * -1 : angle;

        return angle;
    }
}
