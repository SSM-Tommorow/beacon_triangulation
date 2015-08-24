package com.example.hyeonseob.beacontriangulation.Class;

import com.perples.recosdk.RECOBeacon;

public class Beacon {
    private int id, major, minor, rssi;

    public Beacon(int id, int major, int minor, int rssi){
        this.id = id;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
    }

    public void setID(int id, int major, int minor){
        this.id = id;
        this.major = major;
        this.minor = minor;
    }

    public void setRSSI(int rssi){
        this.rssi = rssi;
    }

    public int getId() { return id; }
    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getRSSI() { return rssi; }
}

