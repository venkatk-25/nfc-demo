package com.bluefletch.nfcdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "NFCDEMO:" + MainActivity.class.getSimpleName();
    private static final String BT_SOCKET_SCHEMA = "btsocket://";
    private static final UUID svcUuid = UUID.randomUUID();
    NfcAdapter mNfcAdapter;
    SqLiteHelper sqlHelper;
    BluetoothAdapter bluetoothAdapter;
    int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        sqlHelper = new SqLiteHelper(this);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        setContentView(R.layout.activity_main);

        Button beamData = findViewById(R.id.beamData);
        beamData.setOnClickListener(_onBeamClick);

        Button load = findViewById(R.id.load);
        load.setOnClickListener(_onAddMoney);

        Button balance = findViewById(R.id.balance);
        balance.setOnClickListener(_onGetBalance);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.i(TAG, "onNewIntent");

        setDisplayText ( "onNewIntent " + intent.getAction());
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            processNFCData(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        setDisplayText("onResume " + getIntent().getAction());
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processNFCData(getIntent());
        }
    }

    private void processNFCData(Intent inputIntent) {

        Log.i(TAG, "processNFCData");
        Parcelable[] rawMessages =
                inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages != null && rawMessages.length > 0) {

            NdefMessage[] messages = new NdefMessage[rawMessages.length];

            for (int i = 0; i < rawMessages.length; i++) {

                messages[i] = (NdefMessage) rawMessages[i];

            }

            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMessages[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            String btAddr = new String(msg.getRecords()[0].getPayload());

            Log.i(TAG, "remote BT Addr = " + btAddr);

            Uri remoteUri = Uri.parse(btAddr);
            UUID serviceUuid = UUID.fromString(remoteUri.getPath().substring(1));
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(remoteUri.getAuthority());
            try {
                BluetoothSocket socket = remoteDevice.createInsecureRfcommSocketToServiceRecord(serviceUuid);
                socket.connect();

                while (socket.getInputStream().available() == 0) {}
                byte[] b = new byte[socket.getInputStream().available()];
                socket.getInputStream().read(b);

                setDisplayText(new String(b));
            } catch (IOException e) {
                Log.e(TAG, "Not able to connect to BT device: " + btAddr);
            }
        }
    }

    private void displayCurrentBalance() {
        setDisplayText("Current Money: " + sqlHelper.getCurrentBalance());
    }

    private void setDisplayText(String text) {
        TextView veiw = findViewById(R.id.viewdata);
        if (veiw != null) {
            veiw.setText(text);
        }
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
            setDisplayText("New Balance: " + sqlHelper.getCurrentBalance());
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
        if (mNfcAdapter == null) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            mNfcAdapter = null;
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }

        BluetoothServerSocket serverSocket;
        try {
            serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("santa_wallet", svcUuid);

            Field socketField = BluetoothServerSocket.class.getDeclaredField("mSocket");
            socketField.setAccessible(true);
            BluetoothSocket tmpSocket = (BluetoothSocket)socketField.get(serverSocket);

            Field portField = BluetoothSocket.class.getDeclaredField("mPort");
            portField.setAccessible(true);
            port = (Integer)portField.get(tmpSocket);

//            mNfcAdapter.setNdefPushMessageCallback(_onNfcCreateCallback, this);

            BluetoothSocket socket  = serverSocket.accept(100000);

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("10".getBytes());

            setDisplayText("10000");
        } catch (Exception e){
            Log.e(TAG, "not able to listen on BT device", e);
        }

    }

    private NfcAdapter.CreateNdefMessageCallback _onNfcCreateCallback = new NfcAdapter.CreateNdefMessageCallback() {
        @Override
        public NdefMessage createNdefMessage(NfcEvent inputNfcEvent) {
            Log.i(TAG, "createNdefMessage");
            return createMessage();
        }
    };

    private NdefMessage createMessage() {

        StringBuilder btRequest = new StringBuilder(BT_SOCKET_SCHEMA)
                .append(bluetoothAdapter.getAddress())
                .append("/")
                .append(svcUuid)
                .append("?channel=" + port);
        Log.i(TAG, btRequest.toString());
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/com.bluefletch.nfcdemo.mimetype", btRequest.toString().getBytes())
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
