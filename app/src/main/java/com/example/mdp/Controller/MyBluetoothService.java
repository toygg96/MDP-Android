package com.example.mdp.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service
    private Activity activity;

    public MyBluetoothService(Activity activity){
        this.activity = activity;
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        protected ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            Intent connectionStatusIntent = new Intent("btConnectionStatus");
            connectionStatusIntent.putExtra("Device", socket.getRemoteDevice().getName());
            activity.getApplicationContext().sendBroadcast(connectionStatusIntent);

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    String incomingMessage = new String(mmBuffer,0,numBytes);
                    //Log.d(TAG,incomingMessage);
                    //BROADCAST INCOMING MSG
                    Intent incomingMsgIntent = new Intent("IncomingMsg");
                    incomingMsgIntent.putExtra("receivingMsg", incomingMessage);
                    activity.getApplicationContext().sendBroadcast(incomingMsgIntent);

                    // Send the obtained bytes to the UI activity.
                } catch (IOException e) {
                    Intent disconnectedMsgIntent = new Intent("disconnectedMsg");
                    activity.getApplicationContext().sendBroadcast(disconnectedMsgIntent);
                    cancel();
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            } catch (Exception e) {
                Log.d(TAG, String.valueOf(e.getStackTrace()));
            }
        }

        public void cancel(){
            try {
                mmInStream.close();
                mmOutStream.close();
            } catch (Exception e) {
                Log.d(TAG,e.getStackTrace().toString());
            }

        }
    }
}