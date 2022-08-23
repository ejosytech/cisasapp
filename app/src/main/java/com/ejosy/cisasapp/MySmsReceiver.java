package com.ejosy.cisasapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.TELEPHONY_SERVICE;

public class MySmsReceiver extends BroadcastReceiver {
    private static final String TAG = MySmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";
    public static final String srcphonenumber="+2348124731194";  //Router Number
    //

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context  The Context in which the receiver is running.
     * @param intent   The Intent received.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        String strMessage = "";
        String  straddr ="";
        String  strmsg = "";

        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Check the Android version.
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            SmsMessage[] msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                //
                // Start Application's  MainActivty activity

                //
                // Build the message to show.
                strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                strMessage += " :" + msgs[i].getMessageBody() + "\n";
                straddr += msgs[i].getOriginatingAddress();
                strmsg +=  msgs[i].getMessageBody();
                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
                // Start Application's  MainActivty activity
//
                Intent smsIntent=new Intent(context,sms_receiver_activity.class);
                smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //
                //
                if (straddr.equals(srcphonenumber))  // Call attention only when Routers Calls
                {
                    smsIntent.putExtra("Message", strmsg);
                    context.startActivity(smsIntent);
                }


            }


        }
    }


}
