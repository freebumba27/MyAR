package my.com.ar.myar.ar.data;

import java.util.List;

import my.com.ar.myar.ar.uiwidgets.Marker;


/**
 * This abstract class should be extended for new data sources.
 * 
 */
public abstract class DataSource {
    public abstract List<Marker> getMarkers();
}
