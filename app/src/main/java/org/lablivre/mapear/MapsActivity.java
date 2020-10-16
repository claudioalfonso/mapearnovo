package org.lablivre.mapear;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lablivre.mapear.Ferramentas.PermissionUtils;
import org.lablivre.mapear.Ferramentas.Tools;

public class MapsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    private static final String TAG = "MapsActivity";
    protected LocationManager locationManager;
    Tools tools = new Tools();
    double lat = 0, lng = 0;
    int m67dp, m13dp;

    LocationManager mLocationManager;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                enableMyCamera();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        m67dp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 67, getResources().getDisplayMetrics());
        m13dp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 13, getResources().getDisplayMetrics());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        enableMyLocation();

        mMap.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        try {
                            tools.loadWebPage(marker.getSnippet(), MapsActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //  Take some action here
                        FloatingActionButton fab = findViewById(R.id.fab);
                        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
                        marginParams.setMargins(m13dp, m13dp, m13dp, m67dp);

                        return false;
                    }

                }
        );

        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                //hide or do something you want
                FloatingActionButton fab = findViewById(R.id.fab);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
                marginParams.setMargins(m13dp, m13dp, m13dp, m13dp);
            }
        });

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

                double distancia = tools.gps2m(lat, lng, location.getLatitude(), location.getLongitude());

                if (distancia > 5) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    LatLng newCenter = new LatLng(lat, lng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newCenter, 13));
                    carregarPontos(lat, lng);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        String locationProvider1 = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(locationProvider1, 0, 0, locationListener);

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            boolean isOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (isOn) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    LatLng lastCenter = new LatLng(location.getLatitude(), location.getLongitude());
                                    lat = location.getLatitude();
                                    lng = location.getLongitude();
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCenter, 13));
                                    carregarPontos(lat, lng);
                                } else {
                                    Toast.makeText(mFusedLocationClient.getApplicationContext(), "Local não encontrado, verifique se você habilitou a localização Wi-Fi ou o GPS nas configurações.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

            } else {
                Toast.makeText(this, "Ative a localização", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void enableMyCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access CAMERA is missing.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Access to CAMERA has been granted to the app.
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setDataAndType(
                    Uri.parse("http://lablivre.org/educar/requestapp.php?lat=-1.4&lng=-48.5&raio=999999999999"),
                    "application/mixare-json");
            startActivity(i);

        }
    }

    protected void carregarPontos(double lat, double lng) {
        try {
            if (Tools.verificaConexao(this) && lat != 0 && lng != 0) {
                final ProgressDialog dialog = ProgressDialog.show(this, "", "Carregando Mapa...", true);
                Ion.with(this)
                        .load("http://lablivre.org/educar/requestapp.php")
                        .setBodyParameter("lat", lat + "")
                        .setBodyParameter("lng", lng + "")
                        .setBodyParameter("raio", "9999999999999").asString().withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> request) {
                                String result = request.getResult();
                                try {
                                    JSONObject jo = new JSONObject(result);
                                    if (result != "") {
                                        dialog.dismiss();
                                        JSONArray ja;
                                        ja = jo.getJSONArray("results");
                                        for (int i = 0; i < ja.length(); i++) {
                                            LatLng mk = new LatLng(ja.getJSONObject(i).getDouble("lat"), ja.getJSONObject(i).getDouble("lng"));
                                            mMap.addMarker(new MarkerOptions()
                                                    .title(ja.getJSONObject(i).getString("title"))
                                                    .snippet(ja.getJSONObject(i).getString("webpage"))
                                                    .position(mk));
                                        }
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                enableMyLocation();
            } else {
                // Display the missing permission error dialog when the fragments resume.
                mPermissionDenied = true;
            }
            // return;
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permissão a camera concedida", Toast.LENGTH_LONG).show();
                enableMyCamera();
            } else {
                Toast.makeText(this, "Permissão a camera necessária", Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

}
