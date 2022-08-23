package com.ejosy.cisasapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import GPS_Utilities.GPSTracker;

import static java.time.LocalDate.now;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {
    //database helper object
    private DatabaseHelper db;

    //this is the JSON Data URL
    //make sure you are using the correct ip else it will not work
    private static final String URL_CLIENTS = "https://cbeas.ejosytechconsult.com/api/subscribe.php";
//
    // GPSTracker
    private GPSTracker gpsTracker;
    String selected_item="";
    int selected_item_position = 0;
    public static String Extract_signal = "";
    public static String lat_str = "";
    public static String long_str = "";
    public static String statusSTATE="sms";
    //
    private static final int CODE_POST_REQUEST = 1025;
    private static final int CODE_GET_REQUEST = 1024;
    //
    public static String srcphonenumber="";// Alamist Phone number to be extracted from caller phone set.
    public static String destphonenumber="+2348124731194"; //Server Number where information is routed via
    //
    //RETREIVE PHONE NUMBER SETTINGS BEGINS
    String TAG = "MainActivityTAG";
    Activity activity = MainActivity.this;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    //RETREIVE PHONE NUMBER SETTINGS END
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        db = new DatabaseHelper(this);
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        LoadClients();
        //
        //RETREIVE PHONE NUMBER PERMISSION SETTINGS BEGIN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(wantPermission)) {
                requestPermission(wantPermission);
            } else {
                //
                if(TextUtils.isEmpty(getPhone()))
                {
                    // String is empty or null
                    srcphonenumber = "NA";
                }
                else
                {
                    // string has value
                    srcphonenumber = getPhone();
                }
            }
        }
        //RETREIVE PHONE NUMBER PERMISSION SETTINGS END
        Log.i(TAG, "srcphonenumber: " + srcphonenumber);

        //PERMISSION ......Begin
        final int PERMISSION_REQUEST_CODE = 1;
        // ---- Location ------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to ACCESS_FINE_LOCATION - requesting it");
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }
        // ---- SMS ------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }

        //PERMISSION ......End

        //SIGNAL STRENGHT EXTRACT
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {

            @Override
            public void onSignalStrengthsChanged(SignalStrength strength) {
                super.onSignalStrengthsChanged(strength);

                if (strength.isGsm()) {
                    String[] parts = strength.toString().split(" ");
                    String signalStrength = "";
                    int currentStrength = strength.getGsmSignalStrength();
                    if (currentStrength <= 0) {
                        if (currentStrength == 0) {
                            signalStrength = String.valueOf(Integer.parseInt(parts[3]));
                        } else {
                            signalStrength = String.valueOf(Integer.parseInt(parts[1]));
                        }
                        signalStrength += " dBm";
                    } else {
                        if (currentStrength != 99) {
                            signalStrength = String.valueOf(((2 * currentStrength) - 113));
                            signalStrength += " dBm";
                            Extract_signal = signalStrength;
                        }
                    }
                    //signal = (2 * signal) - 113;
                    System.out.println("Signal strength is : " + signalStrength);
                    System.out.println("Extract_signal : " + Extract_signal);
                    Extract_signal = signalStrength;
                } else {
                    Extract_signal= "Not GSM Signal";
                }
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //

        // Spinner: emergency_type element
        Spinner spinner_emergency_type = (Spinner) findViewById(R.id.spinner_emergency_type);
        // Spinner:emergency_type click listener
        spinner_emergency_type.setOnItemSelectedListener(this);
        // Spinner:emergency_type  Drop down elements
        List<String> emergency_type = new ArrayList<String>();
        emergency_type.add("Theft/Robbery");
        emergency_type.add("Health Issues");
        emergency_type.add("Fire Incidence");

        spinner_emergency_type.setPrompt("Select an item");
        // Creating adapter for spinner:emergency_type
        ArrayAdapter<String> dataAdapter_emergency_type = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emergency_type);
        // Drop down layout style - list view with radio button
        dataAdapter_emergency_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner:emergency_type
        spinner_emergency_type.setAdapter(dataAdapter_emergency_type);
        //

        //

         String smsMessage = "";

        //Toggle Button

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleBtn_sms_alarm);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled

                    statusSTATE = "siren";

                } else {
                    // The toggle is disabled
                    statusSTATE = "sms";


                }
            }
        });



        final Button btn_alarm = findViewById(R.id.btn_alarm);
        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, statusSTATE, Toast.LENGTH_SHORT).show();

                // GPS Management ...Begin
                gpsTracker = new GPSTracker(MainActivity.this);
                Location location = gpsTracker.getLocation();

                // If location information can be obtained, it is displayed on the screen
                double latitude=0.0;
                double longitude=0.0;

                if (location != null)
                {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    //Debug
                    Log.d("mLat", "main_lat: " + latitude);
                    Log.d( "mLong", "main_long: "+ longitude);

                    //Toast.makeText(getApplicationContext(), "Your location: \nLatitude " + latitude + "\nLongitude " + longitude, Toast.LENGTH_LONG).show();
                }
                else
                {
                    // Show message box if location information is not available
                    gpsTracker.showSettingsAlert();
                }
                DecimalFormat df = new DecimalFormat("#.##########");
                lat_str = Double.toString(Double.parseDouble(df.format(latitude)));
                long_str = Double.toString(Double.parseDouble(df.format(longitude)));

                //
                lat_str = lat_str.replace(".","");
                long_str = long_str.replace(".","");
                // GPS Management ...End

                // Collect Data for logging
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                // get current date time with Date()
                Date date = new Date();
                String TodayDate = dateFormat.format(date).toString();
                String TodayTime = timeFormat.format(date).toString();
                //
                String hour_str ="00";
                String minute_str = "00";
                String dayx_str = "00";

                // Extract Hour and Minutes for Data Validity
                Calendar instance = Calendar.getInstance();

                int hour = instance.get(Calendar.HOUR_OF_DAY);
                int minute = instance.get(Calendar.MINUTE);
                int dayx = instance.get(Calendar.DAY_OF_MONTH);
                if (hour<10)
                { hour_str = "0" + String.valueOf(hour);  }
                else
                { hour_str = String.valueOf(hour); }
                //
                if (minute<10)
                { minute_str = "0" + String.valueOf(minute); }
                else
                { minute_str = String.valueOf(minute); }
                //
                if (dayx<10)
                { dayx_str = "0" + String.valueOf(dayx); }
                else
                { dayx_str = String.valueOf(dayx); }
                //


                String  smsMessage ="";
                String logMsg = "";


                //

                if ( statusSTATE.equals("siren") )
                {
                    //SIREN
                    Toast.makeText(getApplicationContext(),"SIREN ALERT" , Toast.LENGTH_SHORT).show(); // display the current state of toggle button's
                    //ZCddhhmmMTtttttttttNnnnnnnnnn
                    //01234567890123456789012345678
                    //Example: ZC1415071T89253365N7535276
                    smsMessage =  "ZC"+  dayx_str  + hour_str + minute_str  +  String.valueOf(selected_item_position) + "T" + lat_str + "N"+ long_str;   // Set Alarm that rings and Tx SMS
                    logMsg = "CMD00A"+ selected_item;
                    //
                    Toast.makeText(getApplicationContext(), smsMessage,Toast.LENGTH_LONG).show();
                    sendSMS(destphonenumber, smsMessage);
                } else {
                    //SMS
                    // The toggle is disabled

                    Toast.makeText(getApplicationContext(), "hour:"+ hour_str , Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "minute:"+ minute_str , Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "day:"+ dayx_str , Toast.LENGTH_SHORT).show();

                    //
                    //ZDddhhmmMTtttttttttNnnnnnnnnn
                    //01234567890123456789012345678
                    //Example: ZD1415071T89253365N75352766
                    smsMessage = "ZD" + dayx_str  + hour_str + minute_str + String.valueOf(selected_item_position) + "T" + lat_str + "N"+ long_str;   // Set Alarm that does not rings and Tx SMS
                    logMsg = "CMD00S"+ selected_item;
                    //
                    Toast.makeText(getApplicationContext(), smsMessage,Toast.LENGTH_LONG).show();
                    sendSMS(destphonenumber, smsMessage);
                }

                //LOGGING
                logdata(TodayDate, TodayTime, Extract_signal, srcphonenumber, destphonenumber,logMsg, lat_str, long_str);


            }
        });


        Button btn_off_alarm = (Button) findViewById(R.id.btn_off_alarm);
        btn_off_alarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String toPhoneNumber = destphonenumber;
                String smsMessage = "ZF"; // Put off Alarm
                //
                sendSMS(toPhoneNumber, smsMessage);
                // Collect Data for logging
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                // get current date time with Date()
                Date date = new Date();
                String TodayDate = dateFormat.format(date).toString();
                String TodayTime = timeFormat.format(date).toString();

                //LOGGING
                logdata(TodayDate, TodayTime, Extract_signal, srcphonenumber, destphonenumber,"ZF", lat_str, long_str);
            }
        });

        Switch switchEnableBtnAlarm= findViewById(R.id.switch_enable_btn_alarm);
        switchEnableBtnAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_alarm.setEnabled(true);
                } else {
                    btn_alarm.setEnabled(false);
                }
            }
        });


    }
   // REFRESH CLIENTS
   private void LoadClients() {

       /*
        * Creating a String Request
        * The request type is GET defined by first parameter
        * The URL is defined in the second parameter
        * Then we have a Response Listener and a Error Listener
        * In response listener we will get the JSON response as a String
        * */
       StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CLIENTS,
               new Response.Listener<String>() {
                   @Override
                   public void onResponse(String response) {
                       try {
                           //converting the string to json array object
                           JSONArray array = new JSONArray(response);
                           //Clean up SQLite
                           db.delete();
                           //traversing through all the object
                           for (int i = 0; i < array.length(); i++) {

                               //getting product object from json array
                               JSONObject clients = array.getJSONObject(i);

                               //Fill SQLite DB with extraxted version from portal
                               db.addClient(clients.getInt("id"), clients.getString("timestamp"),clients .getString("name"),clients .getString("phone"),clients.getString("avenue"),clients.getString("street") );

                           }


                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }
               },
               new Response.ErrorListener() {
                   @Override
                   public void onErrorResponse(VolleyError error) {

                   }
               });

       //adding our stringrequest to queue
       Volley.newRequestQueue(this).add(stringRequest);
   }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item

        int rpt = parent.getCount();
        selected_item_position = position;
        selected_item  =  parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected item position " + position, Toast.LENGTH_LONG).show();
        Toast.makeText(parent.getContext(), "Selected: " + selected_item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    //--sends an SMS message to another device---
    public void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        //PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,  new Intent(SENT), 0);
        // PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        //When the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        //SmsManager sms = SmsManager.getDefault();
        //sms.sendTextMessage(phoneNumber, null, message, null, null);
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",Toast.LENGTH_LONG).show();
            //
           } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    // Log Capture Data
       public void logdata(String logdate, String logtime, String logsignalv, String logsrcphoneno, String logdestphoneno, String logmsg, String logloclat, String logloclong)
       {
       // API link: https://android.intelligize-agro.com/v2/logapi.php?apicall=storelog
            //Log the following Data:
            // 1. Date : log_date
            // 2. Time : log_time
            // 3. Signal-Strenght: log_signalv
            // 4. Source Phone Number: logsrcphoneno
           // 5. Destination Phone Number: logdestphoneno
           //  6. Message: log_msg
            // 7. Location: log_loc_lat,log_loc_long

           HashMap<String, String> params = new HashMap<>();
           params.put("logdate", logdate);
           params.put("logtime", logtime);
           params.put("logsignalv", logsignalv);
           params.put("logsrcphoneno", logsrcphoneno);
           params.put("logdestphoneno", logdestphoneno);
           params.put("logmsg", logmsg);
           params.put("logloclat", logloclat);
           params.put("logloclong", logloclong);

           PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_STORE_LOG, params, CODE_POST_REQUEST);
           request.execute();

    }

    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {
        String url;
        HashMap<String, String> params;
        int requestCode;

        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          //  progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //progressBar.setVisibility(GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                   // refreshHeroList(object.getJSONArray("heroes"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);


            if (requestCode == CODE_GET_REQUEST)
               return requestHandler.sendGetRequest(url);

            return null;
        }
    }
    //PHONE NUMBER RETREIVER HELPER FUNCTION BEGIN
    private String getPhone() {

        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        String getSimSerialNumber = phoneMgr.getSimSerialNumber();
       // return phoneMgr.getLine1Number();
       return phoneMgr.getNetworkOperator() + "-" + phoneMgr.getSimSerialNumber(); //Approach adopted to address challenges of obtaining value from phoneMgr.getLine1Number()
    }

    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Phone number: " + getPhone());
                } else {
                    Toast.makeText(activity,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    //PHONE NUMBER RETREIVER HELPER FUNCTION END



    public void shutdown(View view) {
        //finish();
        finishAffinity();
        System.exit(0);
    }

}