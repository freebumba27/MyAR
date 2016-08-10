package my.com.ar.myar;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import my.com.ar.myar.model.MyPlace;

public class MainActivity extends AppCompatActivity {
    String stringArrayList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Dummy location data
        addDummyData(1, 3.153472, 101.613117, 2650, "Object 1");
        addDummyData(2, 3.148431, 101.610725, 2350, "Object 2");
        addDummyData(3, 3.141961, 101.607335, 2250, "Object 3");
        addDummyData(4, 3.141961, 101.607335, 2250, "Object 4");
        addDummyData(5, 3.298661, 101.616755, 2250, "Object 5");

        // My current location
        Location location = new Location("");
        location.setLatitude(3.146921d);
        location.setLongitude(101.617004d);

        // Update current location
        LocationHelper.setLocation(location);
    }

    private void addDummyData(int id, double latValue, double lngValue, float distanceValue, String name) {
        MyPlace myPlace = new MyPlace();
        myPlace.setLat(latValue);
        myPlace.setLng(lngValue);
        myPlace.setDistance(distanceValue);
        myPlace.setName(name);

        if (id == 1)
            stringArrayList += "[";

        if (id == 5)
            stringArrayList += myPlace.toString() + "]";
        else
            stringArrayList += myPlace.toString() + ",";
    }

    public void openingArView(View view) {
        Intent i = new Intent(this, MyARActivity.class);
        i.putExtra("data", stringArrayList);
        startActivity(i);
    }
}
