package com.gsalah.smser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";


    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] smsObjects = (Object[]) bundle.get("pdus");
                if (smsObjects.length == 0) {
                    return;
                }
                SmsMessage[] messages = new SmsMessage[smsObjects.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < smsObjects.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) smsObjects[i]);
                    sb.append(messages[i].getMessageBody());
                }
                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                PackageManager pm = context.getPackageManager();
                Intent launchIntent = pm.getLaunchIntentForPackage("com.gsalah.smser");
                launchIntent.putExtra("message", message);
                launchIntent.putExtra("sender", sender);
                context.startActivity(launchIntent);
            }
        }
    }

}