package com.tutsplus.code.zolpo.Models;

import java.io.Serializable;

public class RequestToServer implements Serializable {

    private static final int DEFAULT_DISTANCE = 7;
    private static final String StoresUrl = "http://vmedu153.mtacloud.co.il:8082/api/ProductDetails/";

    //members
    private String mBarcode;
    private double mLatitude;
    private double mLongitude;
    private int mDistance = DEFAULT_DISTANCE;


    public RequestToServer()
    {

    }

    public RequestToServer(String iBarcode, double iLatitude, double iLongitude, int iDistance) {
        this.mBarcode = iBarcode;
        this.mLatitude = iLatitude;
        this.mLongitude = iLongitude;
        this.mDistance = iDistance;
    }

    public int GetDefaultDistance() {
        return DEFAULT_DISTANCE;
    }

    public String getBarcode() {
        return mBarcode;
    }

    public void setBarcode(String mBarcode) {
        this.mBarcode = mBarcode;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int mDistance) {
        this.mDistance = mDistance;
    }

    public String getStoresUrl() { return StoresUrl; }

    public String GetFormattedUrl() {
        String formattedMembersString = StoresUrl;
        formattedMembersString += "?Barcode=" + mBarcode;
        formattedMembersString += "&Latitude=" + mLatitude;
        formattedMembersString += "&Longitude=" + mLongitude;
        formattedMembersString += "&Distance=" + mDistance;

        return formattedMembersString;
    }


}
