package com.bluefletch.nfcdemo;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {



    private static String TAG = "NFCDEMO:"+MainActivity.class.getSimpleName();

    NfcAdapter mNfcAdapter;

    private SQLiteDatabase database;
    SqLiteHelper sqlHelper;

    private SQLiteDatabase logsDatabase;
    LogSqLiteHelper logSqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        sqlHelper  = new SqLiteHelper(this);
        database = sqlHelper.getWritableDatabase();

        logSqLiteHelper  = new LogSqLiteHelper(this);
        logsDatabase = sqlHelper.getWritableDatabase();

        displayCurrentBalance();
        displayCurrentLog();

        setContentView(R.layout.activity_main);

        Button beamData = findViewById(R.id.beamData);
        beamData.setOnClickListener( _onBeamClick );

        Button load = findViewById(R.id.load);
        load.setOnClickListener(_onAddMoney);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.i(TAG, "onNewIntent");

        displayCurrentBalance();
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            processNFCData(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        displayCurrentBalance();
        displayCurrentLog();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processNFCData(getIntent());
        }
    }

    private void processNFCData( Intent inputIntent ) {

        Log.i(TAG, "processNFCData");
        Parcelable[] rawMessages =
                inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages != null && rawMessages.length > 0) {

            NdefMessage[] messages = new NdefMessage[rawMessages.length];

            for (int i = 0; i < rawMessages.length; i++) {

                messages[i] = (NdefMessage) rawMessages[i];

            }

            Log.i(TAG, "message size = " + messages.length);

            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMessages[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            String base = new String(msg.getRecords()[0].getPayload());

            sqlHelper.addBalance( Integer.parseInt(base) );
            displayCurrentBalance();

            logSqLiteHelper.addLog("Received " + Integer.parseInt(base) + " by Tap. New Balance: " + sqlHelper.getCurrentBalance());
            displayCurrentLog();

            resetBalance();

        }
    }

    private void displayCurrentBalance( ) {
        displayBalance(sqlHelper.getCurrentBalance() );
    }

    private void displayBalance( int text ) {
        TextView view = findViewById(R.id.balanceValue);
        if ( view != null ) {
            view.setText(Integer.toString(text));
        }
    }

    private void resetBalance() {
        TextView view = findViewById(R.id.send);
        if ( view != null ) {
            view.setText("");
        }
    }

    private void resetLoadAmount() {
        TextView view = findViewById(R.id.loadAmount);
        if ( view != null ) {
            view.setText("");
        }
    }

    private void displayLog( String text ) {
        TextView view = findViewById(R.id.logs);
        setTextCustom(view, text);
//        if ( view != null ) {
//            view.setText(text);
//        }
    }

    private void setTextCustom(final TextView view, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( view != null ) {
                    view.setText(text);
                }
            }
        });
    }

    private void displayCurrentLog( ) {
        displayLog(logSqLiteHelper.getCurrentLogs() );
    }

    private View.OnClickListener _onBeamClick = new View.OnClickListener() {
        @Override
        public void onClick(View inputView) {
            Log.i(TAG, "_onBeamClick onClick");
            turnOnNfcBeam();
        }
    };


    private View.OnClickListener _onAddMoney = new View.OnClickListener() {
        @Override
        public void onClick(View inputView) {
            Log.i(TAG, "_onAddMoneyClick onClick");

            EditText loadAmount = findViewById(R.id.loadAmount);
            int addAmount = Integer.valueOf(loadAmount.getText().toString());

            sqlHelper.addBalance(addAmount);
            displayCurrentBalance();

            logSqLiteHelper.addLog("Added " + addAmount + " to the Wallet. New Balance: " + sqlHelper.getCurrentBalance());
            displayCurrentLog();

            resetLoadAmount();
        }
    };

    private View.OnClickListener _onGetBalance = new View.OnClickListener() {
        @Override
        public void onClick(View inputView) {
            Log.i(TAG, "_onGetMoneyClick onClick");

            displayCurrentBalance();
        }
    };

    /* **************************************************************
        This will create the NFC Adapter, if available,
        and setup the Callback listener when create message is needed.
     */
    private void turnOnNfcBeam() {
        // Check for available NFC Adapter
        if ( mNfcAdapter == null ) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            mNfcAdapter = null;
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }

        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(_onNfcCreateCallback, this);
    }

    private NfcAdapter.CreateNdefMessageCallback _onNfcCreateCallback = new NfcAdapter.CreateNdefMessageCallback() {
        @Override
        public NdefMessage createNdefMessage(NfcEvent inputNfcEvent) {
            Log.i(TAG, "createNdefMessage");
            return createMessage();
        }
    };

    private NdefMessage createMessage() {
        EditText editText = findViewById(R.id.send);

        String text = editText.getText().toString();
        sqlHelper.addBalance(-1 * Integer.parseInt(text) );
        displayCurrentBalance();

        logSqLiteHelper.addLog("Sent " + text + " by Tap. New Balance: " + sqlHelper.getCurrentBalance());
        displayCurrentLog();

        resetBalance();

        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/com.bluefletch.nfcdemo.mimetype", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                        */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                });

        return msg;
    }

}
