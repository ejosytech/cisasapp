package com.ejosy.cisasapp;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.telephony.SmsManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import GPS_Utilities.GPSTracker;


public class Alert_Activity extends AppCompatActivity {
    String Main_msg;
    // GPSTracker nesnesi
    private GPSTracker gpsTracker;
    private String lat_strxv = "";
    private String long_strxv = "";
    //
    public static String srcphonenumber="";                 // Alamist Phone number to be extracted from caller phone set.
    public static String destphonenumber="+2348124731194";  //Server Number where information is routed via
    //
    private static final int CODE_POST_REQUEST = 1025;
    private static final int CODE_GET_REQUEST = 1024;
    //
    //RETREIVE PHONE NUMBER SETTINGS BEGINS
    String TAG = "AlertActivityTAG";
    Activity activity = Alert_Activity.this;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    //RETREIVE PHONE NUMBER SETTINGS END
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        //
        //RETREIVE PHONE NUMBER PERMISSION SETTINGS BEGIN
        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            Log.d(TAG, "Phone number: " + getPhone());
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



        //RETREIVE PHONE NUMBER PERMISSION SETTINGS END
        // GPS Management ...Begin
        gpsTracker = new GPSTracker(Alert_Activity.this);
        Location location = gpsTracker.getLocation();

        // If location information can be obtained, it is displayed on the screen
        double latitude=0.0;
        double longitude=0.0;

        if (location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Toast.makeText(getApplicationContext(), "receiver's location: \nLatitude " + latitude + "\nLongitude " + longitude, Toast.LENGTH_LONG).show();
        }
        else
        {
            // Show message box if location information is not available
            gpsTracker.showSettingsAlert();
        }
        // GPS Management ...End

        // TX SMS to Activate Sound and Text Alarm
        DecimalFormat df = new DecimalFormat("#.##########");
        lat_strxv = Double.toString(Double.parseDouble(df.format(latitude)));
        long_strxv = Double.toString(Double.parseDouble(df.format(longitude)));
        //
        //
        lat_strxv = lat_strxv.replace(".","");
        long_strxv = long_strxv.replace(".","");
        // GPS Management ...End

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }


        Button btn_alarm_stop = (Button) findViewById(R.id.btn_stop_alarm);
        btn_alarm_stop.setOnClickListener(new View.OnClickListener()
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
                Log.d("TodayDate", "onReceive: " + TodayDate);
                Log.d( "TodayTime", "onReceive: "+ TodayTime);
                Log.d("signal", "onReceive: "+ MainActivity.Extract_signal);
                Log.d("Extracted_Sender_number", "onReceive: "+ srcphonenumber);
                Log.d("strmsg", "onReceive: "+ smsMessage);
                Log.d("lat_strxv", "onReceive: "+ lat_strxv);
                Log.d("long_strxv", "onReceive: " + long_strxv);

                logdata(TodayDate, TodayTime, MainActivity.Extract_signal, srcphonenumber, destphonenumber, smsMessage,  lat_strxv, long_strxv);
                //smsIntent.putExtra("MessageNumber", straddr);
          }
        });

        Button btn_home = (Button) findViewById(R.id.btn_main_home);
        btn_home.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent homeIntent =new Intent(getApplicationContext(),MainActivity.class);
                startActivity(homeIntent);

            }


        });

    }

       //PHONE NUMBER RETREIVER HELPER FUNCTION BEGIN
       private String getPhone() {
           TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
           if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
               return "";
           }
           //return phoneMgr.getLine1Number();
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
       //--sends an SMS message to another device---
       public void sendSMS(String phoneNumber, String message)
       {
           String SENT = "SMS_SENT";
           String DELIVERED = "SMS_DELIVERED";
           PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,  new Intent(SENT), 0);
           PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
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
        // 4. Phone Number: log_phoneno
        //  5. Message: log_msg
        // 6. Location: log_loc_lat,log_loc_long

        HashMap<String, String> params = new HashMap<>();
        params.put("logdate", logdate);
        params.put("logtime", logtime);
        params.put("logsignalv", logsignalv);
        params.put("logsrcphoneno", logsrcphoneno);
        params.put("logdestphoneno", logdestphoneno);
        params.put("logmsg", logmsg);
        params.put("logloclat", logloclat);
        params.put("logloclong", logloclong);

        Alert_Activity.PerformNetworkRequest request = new Alert_Activity.PerformNetworkRequest(Api.URL_STORE_LOG, params, CODE_POST_REQUEST);
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




}