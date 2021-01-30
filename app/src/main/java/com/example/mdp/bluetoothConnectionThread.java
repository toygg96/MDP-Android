package com.example.mdp;

import com.example.mdp.MyBluetoothService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class bluetoothConnectionThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private BluetoothAdapter bluetoothAdapter;
        private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private MyBluetoothService bs;
        private  MyBluetoothService.ConnectedThread ct;
        private Activity activity;
        private boolean finishedFlag = true;

    public bluetoothConnectionThread(Activity activity,BluetoothDevice device,BluetoothAdapter adapter) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        this.activity = activity;
        bs = new MyBluetoothService(activity);
        BluetoothSocket tmp = null;
        mmDevice = device;
        bluetoothAdapter = adapter;
        ParcelUuid[] uuids = device.getUuids();
        Log.d(TAG, String.valueOf(uuids[0].getUuid()));

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuids[0].getUuid());

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.d(TAG,"Connection succeeded");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            ct = bs.new ConnectedThread(mmSocket);
            ct.start();
            Intent connectionStatusIntent = new Intent("btConnectionStatus");
            connectionStatusIntent.putExtra("Device", mmDevice.getName());
            activity.getApplicationContext().sendBroadcast(connectionStatusIntent);
            while (finishedFlag) {}
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
                if(ct != null)
                    ct.cancel();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        public void setFinishedFlag(boolean value) {
            this.finishedFlag = value;
        }
        public void write(byte[] bytes) {
            ct.write(bytes);
        }
}