package com.tutsplus.code.zolpo.Models;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {

    private String mName, mAddress;
    private LatLng mLatlng;

    public PlaceInfo(String iName, String iAddress, LatLng iLatlng) {
        this.mName = iName;
        this.mAddress = iAddress;
        this.mLatlng = iLatlng;
    }

    public PlaceInfo() {
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public LatLng getLatlng() {
        return mLatlng;
    }

    public void setLatlng(LatLng latlng) {
        this.mLatlng = latlng;
    }


}
