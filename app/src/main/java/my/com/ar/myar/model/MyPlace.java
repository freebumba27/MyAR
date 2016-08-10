package my.com.ar.myar.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by AJ on 8/10/16.
 */
public class MyPlace {
    private String name;
    private double lat;
    private double lng;
    private float distance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public static ArrayList<MyPlace> toList(String json) {
        return new Gson().fromJson(json, new TypeToken<ArrayList<MyPlace>>() {
        }.getType());
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
