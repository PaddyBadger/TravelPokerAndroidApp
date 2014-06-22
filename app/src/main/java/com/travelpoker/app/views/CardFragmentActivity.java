package com.travelpoker.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.travelpoker.app.R;

/**
 * Created by patriciaestridge on 5/21/14.
 */
public class CardFragmentActivity extends FragmentActivity {
    String cardUrl;
    String cardDescription;
    double cardLongitude;
    double cardLatitude;

    TextView mDescription;
    ScrollView mScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_fragment);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String description = intent.getStringExtra("description");

        String longitudeString = intent.getStringExtra("longitude");
        double longitude = Double.parseDouble(longitudeString);

        String latitudeString = intent.getStringExtra("latitude");
        double latitude = Double.parseDouble(latitudeString);

        cardUrl = url;
        cardDescription = description;
        cardLongitude = longitude;
        cardLatitude = latitude;

        mScroll = (ScrollView) findViewById(R.id.cardFragment);
        mDescription = (TextView) findViewById(R.id.cardDescription);
        mDescription.setText(cardDescription);
        GoogleMap mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.cardMap)).getMap();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(cardLatitude, cardLongitude), 16));
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sightseeing))
                .anchor(0.0f, 1.0f)
                .position(new LatLng(cardLatitude, cardLongitude)));

    }
}
