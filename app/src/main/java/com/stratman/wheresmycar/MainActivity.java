package com.stratman.wheresmycar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stratman.wheresmycar.src.com.tyczj.mapnavigator.Navigator;

import java.sql.Time;
import java.text.BreakIterator;


public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private String maptype;
    private Boolean checkbox;
    LocationClient mLocationClient;
    Location mCurrentLocation;

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    //sets location variables
    private double first_latitude = 0;
    private double first_longitude = 0;
    private String saved_Latitude = "";
    private String saved_Longitude = "";

    Double latitude = 0.0;
    Double longitude = 0.0;

    private int count = 0;
    Marker marker;

    String timer;
    int hour = 0;
    int minute = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.CustomTheme);
        ActionBar actionBar = getActionBar();
        setContentView(R.layout.activity_main);
        //getActionBar().setDisplayShowTitleEnabled(false);
        mLocationClient = new LocationClient(this, this, this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);


    }


    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            about();
            return true;
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_notes) {
            getNotes();
            return true;
        }
        else if  (item.getItemId() == R.id.set_location) {
            setLocation();
            return true;
        }
        else if  (item.getItemId() == R.id.find_car) {
            findCar();
            return true;
        }
        else if(item.getItemId() == R.id.timer){
            timer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    /**
     * Creates an alert dialog that displays information about the application
     */
    public void about(){
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("Dude, Where's My Car? \nDeveloped by Liam Kelly \nVersion 1.4 \n© 2014")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })

                .show();
    }


    /**
     * Creates the note dialog and saves user's note through shared prefs.
     */
    public void createNote(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        final EditText et = new EditText(this);
        layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle("Would you like to make a note?");


        alertDialogBuilder.setCancelable(false);

        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Setting Positive "Yes" Button
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String etStr = et.getText().toString();
                SharedPreferences appPref = getSharedPreferences("My Pref - Make Note", 0);
                SharedPreferences.Editor editor= appPref.edit();
                editor.putString("note",etStr);
                editor.commit();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialogBuilder.show();

        try {
            alertDialog.show();
        } catch (Exception e) {
            // WindowManager$BadTokenException will be caught and the app would
            // not display the 'Force Close' message
            e.printStackTrace();
        }
    }

    /**
     * Retrieves notes from shared prefs and displays them in an alert dialog.
     */
    public void getNotes(){

    SharedPreferences appPref = getSharedPreferences("My Pref - Make Note", 0);
    String retrievedNote = appPref.getString("note", "");



    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

    LinearLayout layout = new LinearLayout(this);
    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setLayoutParams(parms);

    layout.setGravity(Gravity.CLIP_VERTICAL);
    layout.setPadding(2, 2, 2, 2);

    final EditText et = new EditText(this);
    et.setText(retrievedNote);
    layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    alertDialogBuilder.setView(layout);
    alertDialogBuilder.setTitle("Would you like to make a note?");


    alertDialogBuilder.setCancelable(false);

    // Setting Negative "Cancel" Button
    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.cancel();
        }
    });

    // Setting Positive "Yes" Button
    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            final String etStr = et.getText().toString();
            SharedPreferences appPref = getSharedPreferences("My Pref - Make Note", 0);
            SharedPreferences.Editor editor= appPref.edit();
            editor.putString("note",etStr);
            editor.commit();
        }
    });

    AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialogBuilder.show();

    try {
        alertDialog.show();
    } catch (Exception e) {
        // WindowManager$BadTokenException will be caught and the app would
        // not display the 'Force Close' message
        e.printStackTrace();
    }
}


    /**
     * Creates a toast letting the user know their location has been saved
     * Saves location using shared prefs
     */
    public void setLocation() {
        //toast messages alerts user that location has been saved
        Context context = getApplicationContext();
        CharSequence text = "Location saved!";
        int duration = Toast.LENGTH_SHORT;


        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        count = 1;

        if(checkbox == true){
            createNote();
        }

        //saves location
        mCurrentLocation = mLocationClient.getLastLocation();
        first_latitude = mCurrentLocation.getLatitude();
        first_longitude = mCurrentLocation.getLongitude();
        //creates SharedPreferences and saves strings
        SharedPreferences appPref = getSharedPreferences("My Pref - Find Car", 0);
        SharedPreferences.Editor editor= appPref.edit();
        String s_Latitude = first_latitude + "";
        String s_Longitude = first_longitude + "";
        editor.putString("lat", s_Latitude);
        editor.putString("long", s_Longitude);
        editor.commit();





    }

    /**
     * Retrieves location using shared prefs
     * Checks to make sure location had been set before retrieving
     * Updates map and finds directions
     */
    public void findCar() {
        //gets the committed strings
        SharedPreferences appPref = getSharedPreferences("My Pref - Find Car", 0);
        saved_Latitude = appPref.getString("lat", "0");
        saved_Longitude = appPref.getString("long", "0");
        latitude = Double.parseDouble(saved_Latitude);
        longitude = Double.parseDouble(saved_Longitude);
        if(count == 1)
        {
            mMap.clear();
            count = 0;
        }

        if(latitude == 0)
        {
            if(longitude == 0)
            {
                Context context = getApplicationContext();
                CharSequence text = "Please set car location!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else{
                LatLng Car_Location = new LatLng(latitude, longitude);
                marker = mMap.addMarker(new MarkerOptions().position(Car_Location).title("Here's your car!").icon(BitmapDescriptorFactory.fromResource(R.drawable.test)));
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(Car_Location, 20);
                mMap.animateCamera(update);

                mCurrentLocation = mLocationClient.getLastLocation();
                Double user_latitude = mCurrentLocation.getLatitude();
                Double user_longitude = mCurrentLocation.getLongitude();

                LatLng user_latlng = new LatLng(user_latitude, user_longitude);
                Navigator nav = new Navigator(mMap,Car_Location,user_latlng);
                nav.findDirections(true);
            }

        }
        else{
            LatLng Car_Location = new LatLng(latitude, longitude);
            marker = mMap.addMarker(new MarkerOptions().position(Car_Location).title("Here's your car!").icon(BitmapDescriptorFactory.fromResource(R.drawable.test)));
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(Car_Location, 20);
            mMap.animateCamera(update);

            mCurrentLocation = mLocationClient.getLastLocation();
            Double user_latitude = mCurrentLocation.getLatitude();
            Double user_longitude = mCurrentLocation.getLongitude();

            LatLng user_latlng = new LatLng(user_latitude, user_longitude);
            Navigator nav = new Navigator(mMap,Car_Location,user_latlng);
            nav.findDirections(true);

        }

    }

    /**
     * Creates an alert dialog allowing the user to set an alarm
     */
    public void timer()
    {
       /**

        CountDownTimer aCounter = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished)
            {
                timer = "Seconds remaining: " + millisUntilFinished / 1000;
            }

            public void onFinish()
            {
                timer = "Timer is finished";
            }
        };
        aCounter.start();



        // Setting Positive "Yes" Button
        alertDialogBuilder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Set the alarm

                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, timer, duration);
                toast.show();
            }
        });

        **/



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Alarm");

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.timer, null))
                // Add action buttons
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String test = "Hour: " + hour + " Minute: " + minute;

                        Context context = getApplicationContext();
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, test, duration);
                        toast.show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        builder.show();


        try {
            alertDialog.show();
        } catch (Exception e) {
            // WindowManager$BadTokenException will be caught and the app would
            // not display the 'Force Close' message
            e.printStackTrace();
        }

        alertDialog.setContentView(R.layout.timer);
        TimePicker tp = (TimePicker)alertDialog.findViewById(R.id.timePicker1);
        hour = tp.getCurrentHour();
        minute = tp.getCurrentMinute();


    }


    @Override
    public void onPause()
    {

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        checkbox =  prefs.getBoolean("checkboxPref", true);
        maptype = prefs.getString("maptype", "1");

        if(maptype.equals("1"))
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else if(maptype.equals("2"))
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if(maptype.equals("3"))
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

            mMap.setMyLocationEnabled(true);

    }



/**
 * Google Stuff
 */

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */

                        break;
                }

        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        }
        return false;
        }



    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Context context = getApplicationContext();
            CharSequence text = "Please turn on GPS!";
            int duration = Toast.LENGTH_SHORT;


            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }


}
