package com.example.mdp;

public class bluetoothDevice {
    private String deviceName, deviceAddress;

    public bluetoothDevice(String deviceName, String deviceAddress){
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName() { return deviceName; }

    public String getDeviceAddress() { return deviceAddress; }

    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public void setDeviceAddress(String deviceAddress) { this.deviceAddress = deviceAddress; }

    @Override
    public String toString(){
        return ("Device name : " + deviceName + "\nDevice Address : " + deviceAddress);
    }
}
