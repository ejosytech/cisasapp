package com.ejosy.cisasapp;

import android.Manifest;
import android.app.Activity;
import android.app.DirectAction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import GPS_Utilities.GPSTracker;

public class sms_receiver_activity extends AppCompatActivity {
    //
    //database helper object
    private DatabaseHelper db;
    //
    public static String srcphonenumber="";  // Alarmist phone number
    public static String destphonenumber=""; // Receiver set's Number.... to be extracted from Phone Set.
    //
    // GPSTracker nesnesi
    private GPSTracker gpsTracker;
    private String lat_strx = "";
    private String long_strx = "";
    //
    private MediaPlayer mediaPlayer;
    public static List<String> data;
//

    private static final int CODE_POST_REQUEST = 1025;
    private static final int CODE_GET_REQUEST = 1024;
    //
    String latitude_str = "";
    String longitude_str ="";
    //
    //RETREIVE PHONE NUMBER SETTINGS
     String TAG = "smsreceiveractivityTAG";
    Activity activity = sms_receiver_activity.this;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    //
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        db = new DatabaseHelper(this);
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_receiver_activity);
        //
       //RETREIVE PHONE NUMBER PERMISSION SETTINGS BEGIN
        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            Log.d(TAG, "Phone number: " + getPhone());
            //destphonenumber = getPhone(); // Receiver's Number.
            //
            if(TextUtils.isEmpty(getPhone()))
            {
                // String is empty or null
                destphonenumber= "NA";
            }
            else
            {
                // string has value
                destphonenumber = getPhone();
            }
        }

        //RETREIVE PHONE NUMBER PERMISSION SETTINGS END
        // GPS Management ...Begin
        gpsTracker = new GPSTracker(sms_receiver_activity.this);
        Location location = gpsTracker.getLocation();

        // If location information can be obtained, it is displayed on the screen
        double latitude=0.0;
        double longitude=0.0;
        String mobile_no = "";
        int msg_no = 0;
        String msg = "";
        String msgcode="";

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
        DecimalFormat df = new DecimalFormat("#.#########");
        lat_strx = Double.toString(Double.parseDouble(df.format(latitude)));
        long_strx = Double.toString(Double.parseDouble(df.format(longitude)));
        //
        //
        lat_strx = lat_strx.replace(".","");
        long_strx = long_strx.replace(".","");
        // GPS Management ...End

        //MediaPlayer
        try {
            mediaPlayer = MediaPlayer.create(sms_receiver_activity.this, Settings.System.DEFAULT_RINGTONE_URI);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(false);
            //mediaPlayer.reset();
        }catch (Exception e){
            e.printStackTrace();
        }

        //
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
             //String router_mobile_no = extras.getString("MessageNumber");
             String message = extras.getString("Message");
            //String message= "phn:6505551212msg:CMD00AAttacklat:5.567389lon:7.456897";
            //TO DO
            // Extract Alarmist  Number, location, Message and ....... from Server Message
                //
            //
            TextView mobile_noField = (TextView) findViewById(R.id.txt_phone);
            TextView messageField = (TextView) findViewById(R.id.txt_msg_received);
            //
            TextView addressField = (TextView) findViewById(R.id.ave_street);
            ///
            //Message sent from Router        : String message= "phn:+2348124731194" + "C" + CTYPE + "M"  + selected_item_position + "T5.567389N7.456897";
            //E.g.phn:+2348033927733C00SM2T8925329
                     Log.d("sms_receiver_activity:", " message : " +  message.substring(0, 3) );
                     if (message.substring(0, 3).equals("phn"))
                     {
                         try {
                             mobile_no = message.substring(message.indexOf("phn:") + 4, message.indexOf("C")); // Alarmist Number
                         srcphonenumber = mobile_no;
                                            //
                         Log.d("mobile_no_info", "mobile_no: " + mobile_no);
                         //Refer to Example:  [0123]-msg:,  [4-9]-CMD00A,  [10]- begining of main msg

                         msg_no = Integer.parseInt(message.substring(message.indexOf("M") + 1, message.indexOf("T")));
                         //Select Message
                             switch(msg_no)
                             {
                                 case 0 :
                                     msg = "Theft/Robbery";
                                     break;
                                 case 1 :
                                     msg = "Health Issues";
                                     break;
                                 case 2 :
                                     msg = "Fire Incidence";
                                     break;
                                      default :
                                     msg = "";
                             }

                         //
                         msgcode = message.substring(message.indexOf("C") + 1, message.indexOf("M"));
                         //
                         latitude_str = message.substring(message.indexOf("T") + 1, message.indexOf("N"));
                         longitude_str = message.substring(message.indexOf("N") + 1);
                         //
                         }
                         catch (Exception e)
                         {
                             System.out.println("Something went wrong.");
                             System.out.println(e);
                             ;
                         }

                         final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
                         //ImageView imageView = (ImageView) findViewById(R.id.centerImage);
                         //
                         rippleBackground.startRippleAnimation();
                         mediaPlayer.start();


                         //Database Retreival Process
                         //db = new DatabaseHelper(this);
                         //try {
                         //    db.createDataBase();
                         //    db.openDataBase();
                         //} catch (Exception e) {
                         //    e.printStackTrace();
                         //}


                         //
                         Log.d("mobile_no.substring(4)", "mobile_no.substring(4): " + mobile_no.substring(4));

                         // Used Alarmist Call Number(address) to retrieve Client's Details
                         ObjectClient objectClient;
                         objectClient = db.readSingleRecord(mobile_no.substring(4));

                         Log.d("objectClient.name", "objectClient.name: " + objectClient.name);

                         mobile_noField.setText("Message From : " + objectClient.name);
                         addressField.setText("Address: " + objectClient.avenue + " Avenue," + " " + objectClient.street);
                         messageField.setText(msg);

                         //
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
                         Log.d("strmsg", "onReceive: "+ msg);
                         Log.d("lat_strx", "onReceive: "+ lat_strx);
                         Log.d("long_strx", "onReceive: " + long_strx);

                         logdata(TodayDate, TodayTime, MainActivity.Extract_signal,srcphonenumber, destphonenumber, msgcode,  lat_strx, long_strx);
                         //smsIntent.putExtra("MessageNumber", straddr);
                     }
                     else
                      {
                //
                            mobile_no = "Router";
                            msg = message;
                            msgcode="Admin";

                          mobile_noField.setText("Message From : " + mobile_no );
                          addressField.setText("Address: " + msgcode);
                          messageField.setText(msg);

                        }


            }

        Button btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mediaPlayer.stop();

                //Invoke Stop Alarm
                Intent startIntent = new Intent(getApplicationContext(), Alert_Activity.class);
                startActivity(startIntent);

                //Intent homeIntent =new Intent(getApplicationContext(),MainActivity.class);
                //sms_receiver_activity.this.finish();
                //startActivity(homeIntent);


            }

        });

        Button btn_map = (Button) findViewById(R.id.btn_map);
        btn_map.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mediaPlayer.stop();

                Intent webintent = new Intent(getApplicationContext(), webview.class);
                webintent.putExtra("lat_read", latitude_str);
                webintent.putExtra("long_read", longitude_str);
                startActivity(webintent);

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

        sms_receiver_activity.PerformNetworkRequest request = new sms_receiver_activity.PerformNetworkRequest(Api.URL_STORE_LOG, params, CODE_POST_REQUEST);
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



