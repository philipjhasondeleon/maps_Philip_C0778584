package com.example.maps_philip_c0778584;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;

    Polyline line;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markers = new ArrayList();
    int count = 1;
    Marker dragMarker;

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                for (int i=0; i<POLYGON_SIDES; i++) {
                    markers.get(i).remove();
                }
                shape.remove();
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarker(latLng);



            }



            private void setMarker(LatLng latLng) {

                Location l = new Location("");
                l.setLatitude(latLng.latitude);
                l.setLongitude(latLng.longitude);
                MarkerOptions options = new MarkerOptions().position(latLng)
                        .title(getAddress(l)).snippet(getCity(l)).draggable(true);

//                if (destMarker != null) clearMap();



//                destMarker = mMap.addMarker(options);
//
//                drawLine();

//                 check if there are already the same number of markers, we clear the map.
                if (markers.size() == POLYGON_SIDES)
                    clearMap();

                markers.add(mMap.addMarker(options));

                if (markers.size() == POLYGON_SIDES)
                    drawShape();

                if(count == 1){
                    homeMarker = mMap.addMarker(options);
                }if(count == 2){
                    destMarker = mMap.addMarker(options);
                    drawLine();
                }if(count == 3 || count == 4){
                    homeMarker = destMarker;
                    destMarker = mMap.addMarker(options);
                    drawLine();
                }

                count += 1;


            }

            private void drawShape() {


                PolygonOptions options = new PolygonOptions()
                        .fillColor(0x337CFC00)
                        .strokeColor(Color.RED)
                        .strokeWidth(5).clickable(true);

                for (int i=0; i<POLYGON_SIDES; i++) {
                    options.add(markers.get(i).getPosition());
                }

                shape = mMap.addPolygon(options);




            }


            private void clearMap() {

//                if (destMarker != null) {
//                    destMarker.remove();
//                    destMarker = null;
//                }
//
//                line.remove();

                for (Marker marker: markers)
                    marker.remove();

                markers.clear();
                shape.remove();
                shape = null;
            }

            private void drawLine() {



                PolylineOptions options = new PolylineOptions()
                        .color(Color.RED)
                        .width(20)
                        .add(homeMarker.getPosition(), destMarker.getPosition()).clickable(true);
                line = mMap.addPolyline(options);
            }
        });

        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }


    private String getAddress(Location location){

        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> locationAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(locationAddress != null && locationAddress.size() > 0){

                if(locationAddress.get(0).getSubThoroughfare() != null)
                    address += locationAddress.get(0).getSubThoroughfare() + " ";
                if(locationAddress.get(0).getThoroughfare() != null)
                    address += locationAddress.get(0).getThoroughfare() + " ";
                if(locationAddress.get(0).getPostalCode() != null)
                    address += locationAddress.get(0).getLocality();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        return address;

    }



    private String getCity(Location location){

        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> locationAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(locationAddress != null && locationAddress.size() > 0){

                if(locationAddress.get(0).getAdminArea() != null)
                    address += locationAddress.get(0).getAddressLine(0) + " ";
                if(locationAddress.get(0).getThoroughfare() != null)
                    address += locationAddress.get(0).getAddressLine(1) + " ";

            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        return address;

    }


    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        /*Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setHomeMarker(lastKnownLocation);*/
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        mMap.addMarker(options);
//        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        List points = polygon.getPoints();

        Location loc1 = new Location("");
        LatLng l1 = (LatLng) points.get(0);

        loc1.setLatitude(l1.latitude);
        loc1.setLongitude(l1.longitude);

        Location loc2 = new Location("");
        LatLng l2 = (LatLng) points.get(1);

        loc2.setLatitude(l2.latitude);
        loc2.setLongitude(l2.longitude);

        Location loc3 = new Location("");
        LatLng l3 = (LatLng) points.get(2);
        loc2.setLatitude(l3.latitude);
        loc2.setLongitude(l3.longitude);

        Location loc4 = new Location("");
        LatLng l4 = (LatLng) points.get(3);
        loc2.setLatitude(l4.latitude);
        loc2.setLongitude(l4.longitude);



        float distance = loc1.distanceTo(loc2) + loc2.distanceTo(loc3) + loc3.distanceTo(loc4) + loc4.distanceTo(loc1);

        System.out.println("..............polygon." + distance);

        Toast.makeText(this, "Total Distance= " + distance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

        dragMarker = marker;

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        //        markers.add(count,marker);

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        List points = polyline.getPoints();

        Location loc1 = new Location("");
        LatLng l1 = (LatLng) points.get(0);

        loc1.setLatitude(l1.latitude);
        loc1.setLongitude(l1.longitude);

        Location loc2 = new Location("");
        LatLng l2 = (LatLng) points.get(1);

        loc2.setLatitude(l2.latitude);
        loc2.setLongitude(l2.longitude);

        float distance = loc1.distanceTo(loc2);
        System.out.println("...............polyline" + distance);

        System.out.println("gggggggggggggggg///////////////////////////////");


        Toast.makeText(this, "Distance= " + distance, Toast.LENGTH_SHORT).show();

    }
}