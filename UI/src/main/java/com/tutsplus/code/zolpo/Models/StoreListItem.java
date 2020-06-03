package com.tutsplus.code.zolpo.Models;

import java.io.Serializable;

public class StoreListItem implements Serializable {

    public String productName, fullStoreName, fullAddress, promotion, chainImageURL, productImageURL;
    public double productPrice, distance;

    public StoreListItem() {
    }

    public StoreListItem(double iProductPrice, String iProductName, String iProductImageURL, double iDistance,
                         String iFullAddress, String iFullStoreName, String iChainImageURL, String iPromotion)
    {
        this.productName = iProductName;
        this.fullStoreName = iFullStoreName;
        this.fullAddress = iFullAddress;
        this.promotion = iPromotion;
        this.chainImageURL = iChainImageURL;
        this.productImageURL = iProductImageURL;
        this.productPrice = iProductPrice;
        this.distance = iDistance;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getFullStoreName() {
        return fullStoreName;
    }

    public void setFullStoreName(String fullStoreName) {
        this.fullStoreName = fullStoreName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getChainImageURL() {
        return chainImageURL;
    }

    public void setChainImageURL(String chainImageURL) {
        this.chainImageURL = chainImageURL;
    }

    public String getProductImageURL() {
        return productImageURL;
    }

    public void setProductImageURL(String productImageURL) {
        this.productImageURL = productImageURL;
    }
}
