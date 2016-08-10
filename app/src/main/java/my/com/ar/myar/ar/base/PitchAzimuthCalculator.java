package my.com.ar.myar.ar.base;



/**
 * A state class used to calculate bearing and pitch given a Matrix.
 * 
 */
public class PitchAzimuthCalculator {
    private static final Vector looking = new Vector();
    private static final float[] lookingArray = new float[3];

    private static volatile float azimuth = 0;

    private static volatile float pitch = 0;

    private PitchAzimuthCalculator() {};

    public static synchronized float getAzimuth() {
        return PitchAzimuthCalculator.azimuth;
    }
    public static synchronized float getPitch() {
        return PitchAzimuthCalculator.pitch;
    }

    // Note: this function only working in landscape mode
    public static synchronized void calcPitchBearing(Matrix rotationM) {
        if (rotationM==null) return;

        looking.set(0, 0, 0);
        rotationM.transpose();
        looking.set(1, 0, 0);
        looking.prod(rotationM);
        looking.get(lookingArray);
        PitchAzimuthCalculator.azimuth = ((Utilities.getAngle(0, 0, lookingArray[0], lookingArray[2])  + 360 ) % 360);

        rotationM.transpose();
        looking.set(0, 1, 0);
        looking.prod(rotationM);
        looking.get(lookingArray);
        PitchAzimuthCalculator.pitch = -Utilities.getAngle(0, 0, lookingArray[1], lookingArray[2]);
    }
    
    /* enhance version of calc base on screen angle */
    public static synchronized void calcPitchBearing(Matrix rotationM, float screenAngle) {
        if (rotationM==null) return;

        //Counter-clockwise rotation at -90 degrees around ...
        double angleRotation = Math.toRadians(screenAngle);
        Matrix axisRotation = new Matrix();

        /*
        // around x-axis
        axisRotation.set( 
            	1f, 	0f, 	0f, 
            	0f, 	(float)Math.cos(angleRotation), 	(float)-Math.sin(angleRotation), 
                0f,  	(float)Math.sin(angleRotation),		(float)Math.cos(angleRotation));
                */
        

        // around y-axis
        axisRotation.set( 
            	(float) Math.cos(angleRotation), 	0f, 	(float) Math.sin(angleRotation),
            	0f, 								1f, 	0f, 
                (float)-Math.sin(angleRotation),  	0f,		(float) Math.cos(angleRotation));

        
        /*
        // around z-axis
        axisRotation.set( 
        	(float)Math.cos(angleRotation), 	-(float) Math.sin(angleRotation), 	0f, 
        	(float)Math.sin(angleRotation), 	(float) Math.cos(angleRotation), 	0f, 
            0f,  								0f,								 	1f);
            */

        
        looking.set(0, 0, 0);
        rotationM.transpose();
        rotationM.prod(axisRotation);
        looking.set(1, 0, 0);
        looking.prod(rotationM);
        looking.get(lookingArray);
        PitchAzimuthCalculator.azimuth = ((Utilities.getAngle(0, 0, lookingArray[0], lookingArray[2])  + 360 ) % 360);

        rotationM.transpose();
        looking.set(0, 1, 0);
        looking.prod(rotationM);
        looking.get(lookingArray);
        PitchAzimuthCalculator.pitch = -Utilities.getAngle(0, 0, lookingArray[1], lookingArray[2]);
        
        // workadround hack to fix mismatch azimuth in the sections
        if(screenAngle > 140 && screenAngle < 270) {
        	PitchAzimuthCalculator.azimuth += 180;
        }

    }
}

