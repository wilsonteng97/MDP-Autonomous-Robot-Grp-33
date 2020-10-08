package com.example.mdp;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public class BTConnectionService extends IntentService {

    private static final String TAG = "BTConnectionService";
    private static final String appName = "MDP Group 33";

    //UUID
    private static final UUID mdpUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    public  BluetoothDevice myDevice;
    private UUID deviceUUID;

    Context context;

    // constructor
    public BTConnectionService() {

        super("BluetoothConnectionService");
    }

    // when service is created, start method
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        context = getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (intent.getStringExtra("serviceType").equals("listen")) {

            myDevice = (BluetoothDevice) intent.getExtras().getParcelable("device");

            Log.d(TAG, "Service Handle: startAcceptThread");

            startAcceptThread();
        } else {
            myDevice = (BluetoothDevice) intent.getExtras().getParcelable("device");
            deviceUUID = (UUID) intent.getSerializableExtra("id");

            Log.d(TAG, "Service Handle: startClientThread");

            startClientThread(myDevice, deviceUUID);
        }

    }

    /*
        server component that accepts incoming connections
    */
    private class AcceptThread extends Thread {

        //Local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;

            //Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, mdpUUID);
                Log.d(TAG, "AcceptThread: Setting up server using: " + mdpUUID);

            } catch (IOException e) {

            }

            mmServerSocket = tmp;
        }

        public void run() {

            Log.d(TAG, "AcceptThread: Running");

            BluetoothSocket socket = null;
            Intent connectionStatusIntent;

            try {

                Log.d(TAG, " Server socket start....");

                socket = mmServerSocket.accept();

                connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "connect");
                connectionStatusIntent.putExtra("Device", ConnectBT.getBluetoothDevice());
                LocalBroadcastManager.getInstance(context).sendBroadcast(connectionStatusIntent);

                // Successfully connected
                Log.d(TAG, "Server socket accepted connection");

                // start bluetooth chat
                BluetoothCommunication.connected(socket, myDevice, context);


            } catch (IOException e) {

                connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "connectionFail");
                connectionStatusIntent.putExtra("Device",  ConnectBT.getBluetoothDevice());

                Log.d(TAG, "AcceptThread: Connection Failed ,IOException: " + e.getMessage());
            }


            Log.d(TAG, "Ended AcceptThread");

        }

        public void cancel() {

            Log.d(TAG, "Cancel: Canceling AcceptThread");

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Cancel: Closing AcceptThread Failed. " + e.getMessage());
            }
        }


    }

    /*
        outgoing connection w device
    */
    private class ConnectThread extends Thread {

        private BluetoothSocket mySocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {

            Log.d(TAG, "ConnectThread: started");
            myDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Intent connectionStatusIntent;

            Log.d(TAG, "Run: myConnectThread");

            /*
            Get a BluetoothSocket for a
            connection with given BluetoothDevice
            */
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRFcommSocket using UUID: " + mdpUUID);
                tmp = myDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {

                Log.d(TAG, "ConnectThread: Could not create InsecureRFcommSocket " + e.getMessage());
            }

            mySocket = tmp;

            //Cancel discovery to prevent slow connection
            bluetoothAdapter.cancelDiscovery();

            try {

                Log.d(TAG, "Connecting to Device: " + myDevice);
                //Blocking call and will only return on a successful connection / exception
                mySocket.connect();


                //BROADCAST CONNECTION MSG
                connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "connect");
                connectionStatusIntent.putExtra("Device", myDevice);
                LocalBroadcastManager.getInstance(context).sendBroadcast(connectionStatusIntent);

                Log.d(TAG, "run: ConnectThread connected");

                //START BLUETOOTH CHAT
                BluetoothCommunication.connected(mySocket, myDevice, context);

                //CANCEL ACCEPT THREAD FOR LISTENING
                if (acceptThread != null) {
                    acceptThread.cancel();
                    acceptThread = null;
                }

            } catch (IOException e) {

                //Close socket on error

                try {
                    mySocket.close();

                    connectionStatusIntent = new Intent("btConnectionStatus");
                    connectionStatusIntent.putExtra("ConnectionStatus", "connectionFail");
                    connectionStatusIntent.putExtra("Device", myDevice);

                    LocalBroadcastManager.getInstance(context).sendBroadcast(connectionStatusIntent);
                    Log.d(TAG, "run: Socket Closed: Connection Failed!! " + e.getMessage());

                } catch (IOException e1) {
                    Log.d(TAG, "connectThread, run: Unable to close socket connection: " + e1.getMessage());
                }

            }

        }

        public void cancel() {

            try {
                Log.d(TAG, "Cancel: Closing Client Socket");
                mySocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Cancel: Closing mySocket in ConnectThread Failed " + e.getMessage());
            }
        }
    }

    // listen for incoming connection
    public synchronized void startAcceptThread() {

        Log.d(TAG, "start");

        //Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    /*
       start to make connection w other devices
    */
    public void startClientThread(BluetoothDevice device, UUID uuid) {

        Log.d(TAG, "startClient: Started");

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();

    }


}
