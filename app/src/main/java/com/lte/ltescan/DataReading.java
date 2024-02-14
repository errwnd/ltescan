package com.lte.ltescan;

import java.io.Serializable;
import java.util.Date;

class DataReading implements Serializable {

    static final int UNAVAILABLE = 2147483647;
    static final int LOW_RSRP = -141;
    static final int LOW_RSRQ = -20;
    static final int PCI_NA = -1;
    static final int EXECELLENT_RSRP_THRESHOLD = -95;
    static final int GOOD_RSRP_THRESHOLD = -110;
    static final int POOR_RSRP_THRESHOLD = -140;

    private Date timestamp;
    private int rsrp;
    private int rsrq;
    private int sinr;

    private int rssi;

    DataReading() {
        this.timestamp = new Date();
        this.rsrp = UNAVAILABLE;
        this.rsrq = UNAVAILABLE;
        this.sinr = PCI_NA;
        this.rssi =UNAVAILABLE;
    }

    DataReading(DataReading dataReading) {
        this.timestamp = new Date();
        this.rsrp = dataReading.rsrp;
        this.rsrq = dataReading.rsrq;
        this.sinr = dataReading.sinr;
        this.rssi = dataReading.rssi;
    }

    Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    int getRsrp() {
        return rsrp;
    }

    void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    int getRsrq() {
        return rsrq;
    }

    void setRsrq(int rsrq) {
        this.rsrq = rsrq;
    }


    int getSinr() {
        return sinr;
    }

    void setSinr(int sinr) {
        this.sinr = sinr;
    }
    int getRssi() { return rssi; }
    void setRssi(int rssi ) { this.rssi =rssi;}
}
