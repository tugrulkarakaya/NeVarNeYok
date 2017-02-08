package uk.co.nevarneyok.entities;

/**
 * Created by mcagrikarakaya on 5.02.2017.
 * Kişi listesi ile ilgili işlemler
 */

public class CallConnection {
    private int callanswer;
    private String callerid;
    private int direction;

    private CallConnection() {}

    public CallConnection(int callanswer, String callerid, int direction) {
        this.callanswer = callanswer;
        this.callerid = callerid;
        this.direction=direction;
    }

    public int getCallanswer() {
        return callanswer;
    }

    public String getCallerid() {
        return callerid;
    }
    public int getDirection(){
        return direction;
    }
}