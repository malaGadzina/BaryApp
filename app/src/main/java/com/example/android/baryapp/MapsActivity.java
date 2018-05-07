package com.example.android.baryapp;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final String MIEJSCE_DANE = "com.example.android.baryapp.MIEJSCE_DANE";
    public static final String CZY_SZYBKA = "com.example.android.baryapp.CZY_SZYBKA";
    private boolean permission = false;
    private boolean locationOn = false;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private Button infoButton2;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoButtonListener2;
    MapWrapperLayout mapWrapperLayout;

    private GoogleMap mMap;
    private ArrayList<Place> places = new ArrayList<Place>();
    private HashMap<Marker,Place> markers = new HashMap<Marker,Place>();
    ArrayAdapter<String> adapter;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    AutoCompleteTextView searchBar;
    Button locationBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchBar = (AutoCompleteTextView) findViewById(R.id.search);
        locationBtn = (Button) findViewById(R.id.gps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.btn_info_window, null);
        this.infoTitle = (TextView)infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button)infoWindow.findViewById(R.id.button);
        this.infoButton2 = (Button)infoWindow.findViewById(R.id.button2);

        addPlacesFromServer();
        searchBar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String nazwa = adapterView.getItemAtPosition(i).toString();
                locateBar(nazwa);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.ustawienia_menu:
                Toast.makeText(getApplicationContext(),"Ustawienia", Toast.LENGTH_SHORT).show();
                break;

            case R.id.login_menu:
                Toast.makeText(getApplicationContext(),"Login", Toast.LENGTH_SHORT).show();
                break;

            case R.id.info_menu:
                Toast.makeText(getApplicationContext(),"Informacje", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getDeviceLocation();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onMapReady: permissions are not granted");
                return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);



        for (int i=0; i<places.size();i++) {
           Marker m = mMap.addMarker(new MarkerOptions().
                    position(places.get(i).getPos()).
                    title(places.get(i).getName()).
                    snippet(places.get(i).getAddress()));
           markers.put(m, places.get(i));

        }

        initSearch();
    }

    private void initPerm() {
        locationOn = turnOnLocation();
        if(!locationOn){
            DialogFragment dialog = new LocationDialogFragment();
            dialog.show(getFragmentManager(),"GPS");
            if(!locationOn){
                Toast.makeText(getApplicationContext(),"Brak dostępu do lokalizacji, aplikacja ma ograniczoną funkcjonalność",Toast.LENGTH_SHORT).show();
            }
        }
        if (!permission){
            Toast.makeText(this,"Brak uprawnień do lokalizacyjnych. Część funkcji niedostępna",Toast.LENGTH_LONG).show();
        }

    }

    private void initSearch(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));


        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.d(TAG, "EditTextListener: begin");
                if(i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    //execute our method for searching
                    String nazwaBaru = searchBar.getText().toString();
                    boolean find = locateBar(nazwaBaru);
                    if(!find){
                        Toast.makeText(getApplicationContext(),"Nie udało się znaleźć baru",Toast.LENGTH_SHORT).show();
                    }
                }

                return false;
            }
        });

        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton)
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Intent i = new Intent(getApplicationContext(), ReservationActivity.class);
                Place data = markers.get(marker);
                i.putExtra(MIEJSCE_DANE, data);
                i.putExtra(CZY_SZYBKA, false);
                startActivity(i);
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        this.infoButtonListener2 = new OnInfoWindowElemTouchListener(infoButton2)
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Intent i = new Intent(getApplicationContext(), ReservationActivity.class);
                Place data = markers.get(marker);
                i.putExtra(MIEJSCE_DANE, data);
                i.putExtra(CZY_SZYBKA, true);
                startActivity(i);
            }
        };
        this.infoButton2.setOnTouchListener(infoButtonListener2);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                infoButtonListener.setMarker(marker);
                infoButtonListener2.setMarker(marker);
                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private boolean locateBar(String nazwaBaru) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Log.d(TAG, "locateBar:begin=EditText: action");

        Log.d(TAG,"Nazwa Baru: "+nazwaBaru);

        for (int i=0;i<places.size();i++){
            Place znanyBar = places.get(i);
            Log.d(TAG,znanyBar.getName());
            if (nazwaBaru.equalsIgnoreCase(znanyBar.getName())) {
                moveCamera(znanyBar.getPos(), 15f);
                return true;
            }
        }
        return false;

    }

    private void addPlacesFromServer() {
        Log.d(TAG, "addPlacesFromServer: begin");
        places.add(new Place(52.2,21,"Bar nr1","Puławska 22/7"));
        places.add(new Place(52.25, 20.99,"Bar nr2", "Królowej Wiktorii 1/1"));
        String[] Nazwy = new String[places.size()];
        for (int i=0;i<places.size();i++)
             Nazwy[i]= places.get(i).getName();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Nazwy);
        searchBar.setAdapter(adapter);
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: begin");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(permission){
                Log.d(TAG, "getDeviceLocation: get location");
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Log.d(TAG, "getDeviceLocation: location taken");
                        if(task.isSuccessful() && task.getResult()!=null){
                            Log.d(TAG, "getDeviceLocation: location - succesful");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    15f);
                        }else{
                            Log.d(TAG, "getDeviceLocation: location - unable to get");
                            Toast.makeText(getApplicationContext(), "Znalezienie lokalizacji niemożliwe. Część funckji niedostępna", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("MAPS", "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: begin");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);    }

    public void findMeFcn(View view) {
        getDeviceLocation();

    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private boolean turnOnLocation() {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gpsEnabled || !networkEnabled) {
            return false;
        }
        return true;
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: begin");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "getLocationPermission: permissions already granted");
                permission = true;
                initPerm();
            }
            else{
                Log.d(TAG, "getLocationPermission: course location - request");
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            Log.d(TAG, "getLocationPermission: fine location - request");
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission = false;
        Log.d(TAG, "onRequestPermissionResult: begin");
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    Log.d(TAG, "onRequestPermissionResult: check if agreed");
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            permission = false;
                            Log.d(TAG, "onRequestPermissionResult: permission denied");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionResult: permission granted");
                    permission = true;
                    initPerm();
                }
            }
        }
    }
}
