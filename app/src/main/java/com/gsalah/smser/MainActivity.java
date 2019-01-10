package com.gsalah.smser;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    static final int PERMISSION_REQUEST = 123;
    private Context context = this;
    TextToSpeech textToSpeech = null;
    String sender = "";
    String message = "";
    String finalMessage = "";

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        requestPermission();

        if (getIntent().getExtras() != null && getIntent().getExtras().getString("sender") != null) {
            sender = getIntent().getExtras().getString("sender");
            sender = getContact(sender);
            message = getIntent().getExtras().getString("message");
            finalMessage = String.format(getString(R.string.finalMessage), sender, message);
            textView.setText(String.format(getString(R.string.message), sender, message));
            if (textToSpeech == null)
                textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        textToSpeech.setLanguage(Locale.US);
                        readClicked(null);
                    }
                });
        }
    }

    public void readClicked(View view) {
        if (textToSpeech != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(finalMessage, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(finalMessage, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) !=
                                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            boolean granted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (!granted) {
                messageDialog("The App must have the Permissions");
            }
        }
    }

    void messageDialog(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Alert")
                .setMessage(message)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST);
                    }
                }).show();
    }

    private String getContact(String number) {
        ContentResolver contentResolver = getContentResolver();
        Cursor phones = contentResolver.query(Phone.CONTENT_URI, null,
                null, null, null);
        while (phones.moveToNext()) {
            String mNumber = phones.getString(phones.getColumnIndex(Phone.NUMBER));
            String filteredNumber = filterDigits(mNumber)
                    .substring(filterDigits(mNumber).length() - 5, filterDigits(mNumber).length());
            if (number.contains(filteredNumber)) {
                String contactName = phones.getString(phones.getColumnIndex(Phone.DISPLAY_NAME));
                return contactName;
            }
        }
        return number;
    }

    private String filterDigits(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                result += str.charAt(i);
            }
        }
        return result;
    }
}
