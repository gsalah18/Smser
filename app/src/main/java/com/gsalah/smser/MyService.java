package com.gsalah.smser;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyService extends IntentService {

    TextToSpeech textToSpeech = null;
    String sender = "";
    String message = "";
    String finalMessage = "";



    public MyService() {
        super("SMS Service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().getString("sender") != null) {
            sender = intent.getExtras().getString("sender");
            sender = getContact(sender);
            message = intent.getExtras().getString("message");
            finalMessage = String.format(getString(R.string.finalMessage), sender, message);
            if (textToSpeech == null) {
                textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        textToSpeech.setLanguage(Locale.UK);
                        speak();
                    }
                });
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void speak() {
        if (textToSpeech != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(finalMessage, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(finalMessage, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private String getContact(String number) {
        ContentResolver contentResolver = getContentResolver();
        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        while (phones.moveToNext()) {
            String mNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String filteredNumber = filterDigits(mNumber)
                    .substring(filterDigits(mNumber).length()-5, filterDigits(mNumber).length());
            if (number.contains(filteredNumber)) {
                String contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                return contactName;
            }
        }
        return number;
    }

    private String filterDigits(String str) {
        String result = "";
        for (int i = 0; i <str.length() ; i++) {
            if (Character.isDigit(str.charAt(i))) {
                result += str.charAt(i);
            }
        }
        return  result;
    }
}
