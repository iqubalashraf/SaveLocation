package info.location.save;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.util.Locale;


import info.location.save.sqldatabase.DatabaseHandler;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {
    private static final String TAG = "MainActivity";

    static MainActivity mInstance;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    double latitude,longitude;

    String addressline;//locality,adminArea,countryName,postalCode;

    public static String[] titleSet ;
    public static String[] addressSet ;
    public static int[] idSet ;
    public static String [] latitudeSet,longitudeSet;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 2000; // 2 sec
    private static int DISPLACEMENT = 10; // 10 meters


    private static RecyclerView mRecyclerView;
    private static CustomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static Context context;

    private static ProgressDialog pDialog;


   // private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 100;

    public static String currentDateTimeString, currentDate,currentTime;

    public static boolean shouldShowDialogBoxAddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // First we need to check availability of play services
        mInstance=this;
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }
        context = MainActivity.this;
        //GPSchecker();
        DatabaseHandler db = new DatabaseHandler(this);
        int databaseLength = db.getLocationsCount();
        Log.d(TAG,"Locations: "+ databaseLength);
        titleSet = new String [databaseLength];
        addressSet = new String[databaseLength];
        idSet = new int[databaseLength];
        latitudeSet = new String[databaseLength];
        longitudeSet = new String[databaseLength];
        db.getAllLocation();
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CustomAdapter(titleSet,addressSet,idSet,latitudeSet,longitudeSet);
        mRecyclerView.setAdapter(mAdapter);
        registerForContextMenu(mRecyclerView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppStatus.getInstance(getApplicationContext()).isOnline()) {
                    if(GPSchecker()){
                        showProgressDialog("Fetching your location",MainActivity.this);
                        shouldShowDialogBoxAddress=true;
                        FetchLocationDetailes fetchLocationDetailes = new FetchLocationDetailes();
                        fetchLocationDetailes.execute("");
                    }else {
                        sendGPSSetting();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * Method to display the location on UI
     * */
    public void getLocation() {
        if((ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            Log.d(TAG,"getLocation called, Location Reveived");
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                Log.d(TAG,"getLocation called, LatLong Set");
            } else {
                Toast.makeText(getApplicationContext(),"Unable to fetch location. Try again",Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please close and restart the app",Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart Called");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.d(TAG, "GoogleApiClient Connected  " +mGoogleApiClient.isConnected());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Called "+mGoogleApiClient.isConnected());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause Called");
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop Called "+mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.d(TAG, "onStop Called "+mGoogleApiClient.isConnected());
        }
    }
    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        Log.d(TAG,"onConnected Called");
        getLocation();
        Log.d(TAG, "onConnected Called "+mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "onConnected Called");
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG,"OnConnectionSuspended Called");
        mGoogleApiClient.connect();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final MenuItem mMenuItem = item;
        DatabaseHandler db = new DatabaseHandler(this);

        //Toast.makeText(context,item.getItemId()+", "+item.getOrder(), Toast.LENGTH_LONG).show();
        switch (item.getOrder()){
            case 1:
                String[] locationLatLong = db.getLocation(item.getItemId());
                Log.d(TAG,locationLatLong[0]+"  "+locationLatLong[1]+", "+item.getItemId()+", "+item.getOrder());
                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", Double.parseDouble(locationLatLong[0]), Double.parseDouble(locationLatLong[1]));
                //String uri1 = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", latitude, longitude, Double.parseDouble(locationLatLong[0]), Double.parseDouble(locationLatLong[1]));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                break;
            case 2:
                String[] locationLatLong1 = db.getLocation(item.getItemId());
                Log.d(TAG,locationLatLong1[0]+"  "+locationLatLong1[1]+", "+item.getItemId()+", "+item.getOrder());
                //String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", Double.parseDouble(locationLatLong[0]), Double.parseDouble(locationLatLong[1]));
                String uri1 = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", latitude, longitude, Double.parseDouble(locationLatLong1[0]), Double.parseDouble(locationLatLong1[1]));
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri1));
                startActivity(intent1);
                break;
            case 3:
                try{
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    //i.putExtra(Intent.EXTRA_SUBJECT, "AudiByte- Bulletin in bits");
                    String[] locationLatLong3 = db.getLocation(item.getItemId());
                    Log.d(TAG,locationLatLong3[0]+"  "+locationLatLong3[1]+", "+item.getItemId()+", "+item.getOrder());
                    String uri3 = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", Double.parseDouble(locationLatLong3[0]), Double.parseDouble(locationLatLong3[1]));
                    //String sAux = "\nHi! Check out AudiByte app. I found it really nice for listening news \n";
                    uri3 = uri3+"\n\nLocation shared by SaveLocation.\nDownload our app on play store";
                    i.putExtra(Intent.EXTRA_TEXT, uri3);
                    startActivity(Intent.createChooser(i, "Share using:"));
                }
                catch(Exception e) {
                    Log.d(TAG,"Share the app"+e.getMessage());
                }
                break;
            case 4:
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                //dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
                dialog.setMessage("Sure to Delete");
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        deleteLocation(mMenuItem.getItemId());
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        return;
                    }
                });
                dialog.show();
                break;
            default:

                break;
        }



        if(item.getOrder()==0){

        }else if(item.getOrder()==1){

        }
        return false;
    }


    private void addLocation(Double latitude,Double longitude, String addresstitle, String address){
        if(addresstitle.equals(null)){
            addresstitle="No Title";
        }
        if(addresstitle.equals("")){
            addresstitle="No Title";
        }
        DatabaseHandler db = new DatabaseHandler(this);
        db.addLocation(latitude,longitude,addresstitle,address);
        int databaseLength = db.getLocationsCount();
        titleSet = new String [databaseLength];
        addressSet = new String[databaseLength];
        idSet = new int[databaseLength];
        latitudeSet = new String[databaseLength];
        longitudeSet = new String[databaseLength];
        db.getAllLocation();
        mAdapter = new CustomAdapter(titleSet,addressSet,idSet,latitudeSet,longitudeSet);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void deleteLocation(int deleteItem){
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteLocation(deleteItem);
        int databaseLength = db.getLocationsCount();
        titleSet = new String [databaseLength];
        addressSet = new String[databaseLength];
        idSet = new int[databaseLength];
        latitudeSet = new String[databaseLength];
        longitudeSet = new String[databaseLength];
        db.getAllLocation();
        mAdapter = new CustomAdapter(titleSet,addressSet,idSet,latitudeSet,longitudeSet);
        mRecyclerView.setAdapter(mAdapter);
    }
    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        Log.d(TAG,"createLocationRequest called successfully" );

    }
    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try{
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
                Log.d(TAG,"startLocationUpdates called succesfully" );
            }catch (Exception e){
                Log.d(TAG,"Error:1 "+e.getMessage());
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please close and restart the app",Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(TAG,"StopLocationUpdates Called");
        }catch (Exception e){
            Log.d(TAG,"Error:2 "+e.getMessage());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        Log.d(TAG,"onLocationChanged Called");
    }
    public void showProgressDialog(String text, Context context){
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(text);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Stop",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shouldShowDialogBoxAddress=false;
                cancelProgressDialog();
            }
        });
        pDialog.show();
    }
    public void cancelProgressDialog(){
        pDialog.dismiss();
    }

    private void sendGPSSetting(){
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            //dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Enable Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
    }
    private boolean GPSchecker(){
        Log.d(TAG, "GPSchecker Called " );
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if(!gps_enabled && !network_enabled){
            return false;
        }else {
            return true;
        }
    }
    public void displayDialogForAddress(){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        currentDate=df.format("dd-MM-yyyy", new java.util.Date()).toString();
        currentTime = df.format("hh:mm a", new java.util.Date()).toString();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.context);
        final EditText input = new EditText(MainActivity.this);
        input.setHint("Enter Location Title");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setTitle("Current Location");
        alertDialog.setMessage(addressline+ "\nLatitude: "+latitude+"\nLongitude: "+longitude);
        alertDialog.setIcon(R.drawable.ic_location_png);
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String title = input.getText().toString();
                        addLocation(latitude,longitude,title,addressline);
                        Toast.makeText(getApplicationContext(),"Location Saved",Toast.LENGTH_SHORT).show();
                    }
                });
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
