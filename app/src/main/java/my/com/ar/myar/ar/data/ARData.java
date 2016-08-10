package my.com.ar.myar.ar.data;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import my.com.ar.myar.ar.AugmentedReality;
import my.com.ar.myar.ar.base.Matrix;
import my.com.ar.myar.ar.uiwidgets.Marker;


/**
 * Abstract class which should be used to set global data.
 */
public abstract class ARData {
    private static final String TAG = "ARData";
    private static final Object maxZoomDistanceLock = new Object();
    private static float maxZoomDistance = 100; // in km
    private static final Map<String, Marker> markerList = new ConcurrentHashMap<String, Marker>();
    private static final List<Marker> cache = new CopyOnWriteArrayList<Marker>();
    private static final AtomicBoolean dirty = new AtomicBoolean(false);
    private static final float[] locationArray = new float[3];

    /*defaulting to our place*/
    public static final Location hardFix = new Location("ATL");

    static {
        hardFix.setLatitude(3.150073);
        hardFix.setLongitude(101.615782);
        hardFix.setAltitude(1);
    }

    private static final Object radiusLock = new Object();
    private static float radius = new Float(20);
    private static String zoomLevel = new String();
    private static final Object zoomProgressLock = new Object();
    private static int zoomProgress = 0;
    private static Location currentLocation = hardFix;
    private static Matrix rotationMatrix = new Matrix();
    private static Matrix originalRotationMatrix = new Matrix();
    private static final Object azimuthLock = new Object();
    private static float azimuth = 0;
    private static final Object pitchLock = new Object();
    private static float pitch = 0;
    private static final Object rollLock = new Object();
    private static float roll = 0;


    public static void setMaxZoomDistance(float maxZoom) {
        synchronized (ARData.maxZoomDistanceLock) {
            ARData.maxZoomDistance = maxZoom;
        }
    }

    public static float getMaxZoom() {
        synchronized (ARData.maxZoomDistanceLock) {
            return ARData.maxZoomDistance;
        }
    }

    /**
     * Set the zoom level.
     *
     * @param zoomLevel String representing the zoom level.
     */
    public static void setZoomLevel(String zoomLevel) {
        if (zoomLevel == null) throw new NullPointerException();

        synchronized (ARData.zoomLevel) {
            ARData.zoomLevel = zoomLevel;
        }
    }

    /**
     * Get the zoom level.
     *
     * @return String representing the zoom level.
     */
    public static String getZoomLevel() {
        synchronized (ARData.zoomLevel) {
            return ARData.zoomLevel;
        }
    }

    /**
     * Set the zoom progress.
     *
     * @param zoomProgress int representing the zoom progress.
     */
    public static void setZoomProgress(int zoomProgress) {
        synchronized (ARData.zoomProgressLock) {
            if (ARData.zoomProgress != zoomProgress) {
                ARData.zoomProgress = zoomProgress;
                if (dirty.compareAndSet(false, true)) {
                    if (AugmentedReality.DEBUG) Log.v(TAG, "Setting DIRTY flag!");
                    cache.clear();
                }
            }
        }
    }

    /**
     * Get the zoom progress.
     *
     * @return int representing the zoom progress.
     */
    public static int getZoomProgress() {
        synchronized (ARData.zoomProgressLock) {
            return ARData.zoomProgress;
        }
    }

    /**
     * Set the radius of the radar screen.
     *
     * @param radius float representing the radar screen.
     */
    public static void setRadius(float radius) {
        synchronized (ARData.radiusLock) {
            ARData.radius = radius;
        }
    }

    /**
     * Get the radius (in KM) of the radar screen.
     *
     * @return float representing the radar screen.
     */
    public static float getRadius() {
        synchronized (ARData.radiusLock) {
            return ARData.radius;
        }
    }

    /**
     * Set the current location.
     *
     * @param currentLocation Location to set.
     * @throws NullPointerException if Location param is NULL.
     */
    public static void setCurrentLocation(Location currentLocation) {
        if (currentLocation == null) throw new NullPointerException();

        if (AugmentedReality.DEBUG)
            Log.d(TAG, "current location. location=" + currentLocation.toString());
        synchronized (currentLocation) {
            ARData.currentLocation = currentLocation;
        }
        onLocationChanged(currentLocation);
    }

    private static void onLocationChanged(Location location) {
        if (AugmentedReality.DEBUG)
            Log.d(TAG, "New location, updating markers. location=" + location.toString());
        for (Marker ma : markerList.values()) {
            ma.calcRelativePosition(location);
        }

        if (dirty.compareAndSet(false, true)) {
            if (AugmentedReality.DEBUG) Log.v(TAG, "Setting DIRTY flag!");
            cache.clear();
        }
    }

    /**
     * Get the current Location.
     *
     * @return Location representing the current location.
     */
    public static Location getCurrentLocation() {
        synchronized (ARData.currentLocation) {
            return ARData.currentLocation;
        }
    }

    /**
     * Set the rotation matrix - before remap
     *
     * @param rotationMatrix Matrix to use for rotation.
     */
    public static void setOriginalRotationMatrix(Matrix rotationMatrix) {
        synchronized (ARData.originalRotationMatrix) {
            ARData.originalRotationMatrix = rotationMatrix;
        }
    }

    /**
     * Get the rotation matrix - before remap
     *
     * @return Matrix representing the rotation matrix.
     */
    public static Matrix getOriginalRotationMatrix() {
        synchronized (ARData.originalRotationMatrix) {
            return originalRotationMatrix;
        }
    }

    /**
     * Set the rotation matrix.
     *
     * @param rotationMatrix Matrix to use for rotation.
     */
    public static void setRotationMatrix(Matrix rotationMatrix) {
        synchronized (ARData.rotationMatrix) {
            ARData.rotationMatrix = rotationMatrix;
        }
    }

    /**
     * Get the rotation matrix.
     *
     * @return Matrix representing the rotation matrix.
     */
    public static Matrix getRotationMatrix() {
        synchronized (ARData.rotationMatrix) {
            return rotationMatrix;
        }
    }

    /**
     * Add a List of Markers to our Collection.
     *
     * @param markers List of Markers to add.
     */
    public static void addMarkers(Collection<Marker> markers) {
        if (markers == null) throw new NullPointerException();

        if (markers.size() <= 0) return;

        if (AugmentedReality.DEBUG)
            Log.d(TAG, "New markers, updating markers. new markers=" + markers.toString());
        for (Marker marker : markers) {
            if (!markerList.containsKey(marker.getName())) {
                marker.calcRelativePosition(ARData.getCurrentLocation());
                markerList.put(marker.getName(), marker);
            }
        }

        if (dirty.compareAndSet(false, true)) {
            if (AugmentedReality.DEBUG) Log.v(TAG, "Setting DIRTY flag!");
            cache.clear();
        }
    }

    public static void clearMarkers() {
        markerList.clear();
    }

    /**
     * Get the Markers collection.
     *
     * @return Collection of Markers.
     */
    public static List<Marker> getMarkers() {
        //If markers we added, zero out the altitude to recompute the collision detection
        if (dirty.compareAndSet(true, false)) {
            if (AugmentedReality.DEBUG)
                Log.v(TAG, "DIRTY flag found, resetting all marker heights to zero.");
            for (Marker ma : markerList.values()) {
                ma.getLocation().get(locationArray);
                locationArray[1] = ma.getInitialY();
                ma.getLocation().set(locationArray);
            }

            if (AugmentedReality.DEBUG) Log.v(TAG, "Populating the cache.");
            List<Marker> copy = new ArrayList<Marker>();
            copy.addAll(markerList.values());
            Collections.sort(copy, comparator);
            //The cache should be sorted from closest to farthest marker.
            cache.clear();
            cache.addAll(copy);
        }
        return Collections.unmodifiableList(cache);
    }

    private static final Comparator<Marker> comparator = new Comparator<Marker>() {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(Marker arg0, Marker arg1) {
            return Double.compare(arg0.getDistance(), arg1.getDistance());
        }
    };

    /**
     * Set the current Azimuth.
     *
     * @param azimuth float representing the azimuth.
     */
    public static void setAzimuth(float azimuth) {
        synchronized (azimuthLock) {
            ARData.azimuth = azimuth;
        }
    }

    /**
     * Get the current Azimuth.
     *
     * @return azimuth float representing the azimuth.
     */
    public static float getAzimuth() {
        synchronized (azimuthLock) {
            return ARData.azimuth;
        }
    }

    /**
     * Set the current Pitch.
     *
     * @param pitch float representing the pitch.
     */
    public static void setPitch(float pitch) {
        synchronized (pitchLock) {
            ARData.pitch = pitch;
        }
    }

    /**
     * Get the current Pitch.
     *
     * @return pitch float representing the pitch.
     */
    public static float getPitch() {
        synchronized (pitchLock) {
            return ARData.pitch;
        }
    }

    /**
     * Set the current Roll.
     *
     * @param roll float representing the roll.
     */
    public static void setRoll(float roll) {
        synchronized (rollLock) {
            ARData.roll = roll;
        }
    }

    /**
     * Get the current Roll.
     *
     * @return roll float representing the roll.
     */
    public static float getRoll() {
        synchronized (rollLock) {
            return ARData.roll;
        }
    }
}
