package com.ta.stb_03;

public class DataPoint {
    String date;
    int yValue, xValue;

    public DataPoint() {
    }

    public DataPoint(String date, int xValue, int yValue) {
        this.date = date;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public String getDate() {
        return date;
    }

    public int getxValue() {
        return xValue;
    }

    public int getyValue() {
        return yValue;
    }
}
