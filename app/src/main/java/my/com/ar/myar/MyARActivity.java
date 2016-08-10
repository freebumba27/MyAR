package my.com.ar.myar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;

import my.com.ar.myar.ar.AugmentedReality;
import my.com.ar.myar.ar.data.ARData;
import my.com.ar.myar.ar.uiwidgets.IconMarker;
import my.com.ar.myar.ar.uiwidgets.Marker;
import my.com.ar.myar.model.MyPlace;

public class MyARActivity extends AugmentedReality {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<MyPlace> itemList = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("data")) {
            itemList = MyPlace.toList(extras.getString("data"));
        }


        ArrayList<Marker> markers = new ArrayList<>();
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.moster);
        float distance = 100;
        for (MyPlace item : itemList) {
            if (markers.size() == 20) break; // Only show max 20 markers
            Marker marker = new IconMarker(item.getName(), item.getLat(), item.getLng(), 0, Color.parseColor("#27de5b"), icon);
            marker.setTag(item);
            markers.add(marker);
            if (item.getDistance() / 1000 > distance) {
                distance = item.getDistance() / 1000;
            }
        }
        ARData.setMaxZoomDistance(distance + 5); // buffer 5km for display further item
        ARData.clearMarkers();
        ARData.addMarkers(markers);
        updateDataOnZoom();
        camScreen.invalidate();
    }

    @Override
    protected void markerTouched(Marker marker) {
        if (marker.getTag() != null) {
            MyPlace place = (MyPlace) marker.getTag();
            Intent i = new Intent(this, MyPlaceDetailActivity.class);
            i.putExtra("data", place.toString());
            startActivity(i);
        }
    }
}
