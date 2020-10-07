package com.example.mdp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class BluetoothCommunication {

    private static final String TAG = "BluetoothCommunication";
    private static Context mmContext;

    private static BluetoothSocket mmSocket;
    private static InputStream inputStream;
    private static OutputStream outPutStream;
    private static BluetoothDevice BTConnectionDevice;

    public static void startCommunication(BluetoothSocket socket) {

        Log.d(TAG, "ConnectedThread: Starting");

        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;


        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream = tmpIn;
        outPutStream = tmpOut;

        //Buffer store for the stream
        byte[] buffer = new byte[1024];

        int numbytes; // bytes returned from read()

        while (true) {
            //Read from the InputStream
            try {
                numbytes = inputStream.read(buffer);

                String incomingMessage = new String(buffer, 0, numbytes);
                Log.d(TAG, "InputStream: " + incomingMessage);

                //BROADCAST INCOMING MSG
                Intent incomingMsgIntent = new Intent("IncomingMsg");
                incomingMsgIntent.putExtra("receivingMsg", incomingMessage);
                LocalBroadcastManager.getInstance(mmContext).sendBroadcast(incomingMsgIntent);

            } catch (IOException e) {

                //BROADCAST CONNECTION MSG
                Intent connectionStatusIntent = new Intent("btConnectionStatus");
                connectionStatusIntent.putExtra("ConnectionStatus", "disconnect");
                connectionStatusIntent.putExtra("Device",BTConnectionDevice);
                LocalBroadcastManager.getInstance(mmContext).sendBroadcast(connectionStatusIntent);

                Log.d(TAG, "chat service closed");
                e.printStackTrace();
                break;

            } catch (Exception e){
                Log.d(TAG, "chat service closed"+ e);
                e.printStackTrace();

            }

        }
    }

    // send message to remote device
    public static void write(byte[] bytes) {

        String text = new String(bytes, Charset.defaultCharset());
        Log.d(TAG, "Write: Writing to outputstream: " + text);

        try {
            outPutStream.write(bytes);
        } catch (IOException e) {
            // make a toast
        } catch (NullPointerException e) {
            // make a toast
        }
    }


    // start communicating with remote device
    static void connected(BluetoothSocket mmSocket, BluetoothDevice BTDevice, Context context) {
        Log.d(TAG, "Connected: Starting");

        BTConnectionDevice = BTDevice;
        mmContext = context;
        startCommunication(mmSocket);
    }

    // write to connect thread (unsynchronise manner)
    public static void writeMsg(byte[] out) {
        write(out);
    }
}
