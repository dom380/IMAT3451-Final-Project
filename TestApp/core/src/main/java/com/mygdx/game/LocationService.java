package com.mygdx.game;

/**
 * Created by Dom on 20/02/2017.
 */

public interface LocationService {

    void connect();

    void disconnect();

    boolean isAvailable();

    double[] getLatLong();

    void disable();
    void enable();
}
