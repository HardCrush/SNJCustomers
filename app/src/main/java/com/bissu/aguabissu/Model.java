package com.bissu.aguabissu;


import androidx.annotation.Keep;


@Keep
public class Model {
    public boolean getVia;

    public boolean isGetVia() {
        return getVia;
    }

    public void setGetVia(boolean getVia) {
        this.getVia = getVia;
    }


    private String NAME;
    private String MOBILE_NUMBER;
    private long TIME;
    private String WALLET_STATUS;
    private long QUANTITY;
    private String TRANSACTION_NO;
    private long AMOUNT;
    private String PAID_VIA;
    private long PAID_AMOUNT;
    private String NOTE;
    private String id;
    private String  From;
    private String  DATE;

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNOTE() {
        return NOTE;
    }

    public void setNOTE(String NOTE) {
        this.NOTE = NOTE;
    }

    public long getPAID_AMOUNT() {
        return PAID_AMOUNT;
    }

    public void setPAID_AMOUNT(long PAID_AMOUNT) {
        this.PAID_AMOUNT = PAID_AMOUNT;
    }

    public long getAMOUNT() {
        return AMOUNT;
    }

    public void setAMOUNT(long AMOUNT) {
        this.AMOUNT = AMOUNT;
    }

    public String getTRANSACTION_NO() {
        return TRANSACTION_NO;
    }

    public void setTRANSACTION_NO(String TRANSACTION_NO) {
        this.TRANSACTION_NO = TRANSACTION_NO;
    }



    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getMOBILE_NUMBER() {
        return MOBILE_NUMBER;
    }

    public void setMOBILE_NUMBER(String MOBILE_NUMBER) {
        this.MOBILE_NUMBER = MOBILE_NUMBER;
    }

    //




    public long getTIME() {
        return TIME;
    }

    public void setTIME(long TIME) {
        this.TIME = TIME;
    }

    public String getWALLET_STATUS() {
        return WALLET_STATUS;
    }

    public void setWALLET_STATUS(String WALLET_STATUS) {
        this.WALLET_STATUS = WALLET_STATUS;
    }

    public long getQUANTITY() {
        return QUANTITY;
    }

    public void setQUANTITY(long QUANTITY) {
        this.QUANTITY = QUANTITY;
    }

    public Model(){

    }




    public String getPAID_VIA() {
        return PAID_VIA;
    }

    public void setPAID_VIA(String PAID_VIA) {
        this.PAID_VIA = PAID_VIA;
    }
}


