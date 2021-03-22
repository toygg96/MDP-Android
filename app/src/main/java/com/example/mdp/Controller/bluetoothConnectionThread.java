package com.example.mdp.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class bluetoothConnectionThread {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public class connectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private BluetoothAdapter bluetoothAdapter;
        private MyBluetoothService bs;
        private MyBluetoothService.ConnectedThread ct;
        private Activity activity;
        private boolean autoReconnectFlag = false;
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.

        public connectThread(Activity activity, BluetoothDevice device, BluetoothAdapter adapter, boolean reconnectFlag) {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            this.activity = activity;
            this.bluetoothAdapter = adapter;
            this.autoReconnectFlag = reconnectFlag;
            bs = new MyBluetoothService(activity);
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG,String.valueOf(autoReconnectFlag));
            if (autoReconnectFlag) {
                while (true) {
                    try {
                        Thread.sleep(4000);
                        // Connect to the remote device through the socket. This call blocks
                        // until it succeeds or throws an exception.
                        mmSocket.connect();
                        Log.d(TAG, "Connection succeeded");
                        break;
                    } catch (IOException | InterruptedException connectException) {
                        // Unable to connect; close the socket and return.
                        Log.e(TAG,"exception: ",connectException);
                        try {
                            mmSocket.close();
                        } catch (IOException closeException) {
                            Log.e(TAG, "Could not close the client socket", closeException);
                        }
                    }
                }
            } else {
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                    Log.d(TAG, "Connection succeeded");
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                    }
                    return;
                }
            }
            autoReconnectFlag = true;
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            ct = bs.new ConnectedThread(mmSocket);
            ct.start();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
                if (ct != null)
                    ct.cancel();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }


        //public void setFinishedFlag(boolean value) {
        //    this.finishedFlag = value;
        //}
        public void write(byte[] bytes) {
            ct.write(bytes);
        }
    }

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private Activity activity;
        BluetoothSocket socket = null;
        private MyBluetoothService bs;
        private  MyBluetoothService.ConnectedThread ct;

        public AcceptThread(Activity activity, BluetoothAdapter adapter) {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            this.activity = activity;
            bs = new MyBluetoothService(activity);
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = adapter.listenUsingRfcommWithServiceRecord("RPI Server", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                Log.d(TAG,"RUNNING ACCEPT THREAD");
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    try {
                        ct = bs.new ConnectedThread(socket);
                        ct.start();
                        mmServerSocket.close();
                    } catch (Exception e){
                        Log.e(TAG,"Creating connected thread failed",e);
                    }
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

        public void write(byte[] bytes) {
            if (ct != null)
                ct.write(bytes);
            else
                Log.d(TAG,"NOT RUNNING");
        }
    }
}