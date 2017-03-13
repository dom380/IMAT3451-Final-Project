package dmu.mygdx.game;

/**
 * Created by Dom on 20/02/2017.
 * Interface for the location service.
 */

public interface LocationService {

    /**
     * Method to connect to the backend location service.
     */
    void connect();

    /**
     * Method to disconnect from the backend location service.
     */
    void disconnect();

    /**
     * Check if the location service is reachable.
     * @return True if can use the location service.
     */
    boolean isAvailable();

    /**
     * Retrieve the latitude and longitude of the device.
     * @return The latitude and longitude of the device.
     */
    double[] getLatLong();

    /**
     * Disable the location service. All future calls will return null until re-enabled.
     */
    void disable();


    /**
     * Enables the location service for use.
     */
    void enable();
}
