package com.example.mdp.Controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.mdp.R;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private int mViewResourceId;

    public DeviceListAdapter(Context context, int tvResourceId) {
        super(context, tvResourceId);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);
        BluetoothDevice device = getItem(position);
        TextView name = (TextView) convertView.findViewById(R.id.deviceName);
        if (device.getName() == null)
            name.setText("Unknown device");
        else
            name.setText(device.getName());
        TextView release = (TextView) convertView.findViewById(R.id.deviceAddress);
        release.setText(device.getAddress());
        return convertView;
    }
}