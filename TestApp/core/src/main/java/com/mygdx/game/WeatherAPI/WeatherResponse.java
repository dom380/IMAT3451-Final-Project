package com.mygdx.game.WeatherAPI;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

/**
 * Created by Dom on 22/02/2017.
 */

public class WeatherResponse {

    private Main main;
    private List<WeatherCondition> weather;

    public WeatherResponse(){}

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<WeatherCondition> getWeather() {
        return weather;
    }

    public void setWeather(List<WeatherCondition> weather) {
        this.weather = weather;
    }

    public static class WeatherCondition {
        private ConditionCode id;
        private String main;
        private String description;

        public WeatherCondition(){}

        public ConditionCode getId() {
            return id;
        }

        public void setId(ConditionCode id) {
            this.id = id;
        }

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public enum ConditionCode{
        UNKNOWN                         (Integer.MIN_VALUE),
        /* Thunderstorm */
        THUNDERSTORM_WITH_LIGHT_RAIN    (200),
        THUNDERSTORM_WITH_RAIN          (201),
        THUNDERSTORM_WITH_HEAVY_RAIN    (202),
        LIGHT_THUNDERSTORM              (210),
        THUNDERSTORM                    (211),
        HEAVY_THUNDERSTORM              (212),
        RAGGED_THUNDERSTORM             (221),
        THUNDERSTORM_WITH_LIGHT_DRIZZLE (230),
        THUNDERSTORM_WITH_DRIZZLE       (231),
        THUNDERSTORM_WITH_HEAVY_DRIZZLE (232),
        /* Drizzle */
        LIGHT_INTENSITY_DRIZZLE         (300),
        DRIZZLE                         (301),
        HEAVY_INTENSITY_DRIZZLE         (302),
        LIGHT_INTENSITY_DRIZZLE_RAIN    (310),
        DRIZZLE_RAIN                    (311),
        HEAVY_INTENSITY_DRIZZLE_RAIN    (312),
        SHOWER_DRIZZLE                  (321),
        /* Rain */
        LIGHT_RAIN                      (500),
        MODERATE_RAIN                   (501),
        HEAVY_INTENSITY_RAIN            (502),
        VERY_HEAVY_RAIN                 (503),
        EXTREME_RAIN                    (504),
        FREEZING_RAIN                   (511),
        LIGHT_INTENSITY_SHOWER_RAIN     (520),
        SHOWER_RAIN                     (521),
        HEAVY_INTENSITY_SHOWER_RAIN     (522),
        /* Snow */
        LIGHT_SNOW                      (600),
        SNOW                            (601),
        HEAVY_SNOW                      (602),
        SLEET                           (611),
        SHOWER_SNOW                     (621),
        /* Atmosphere */
        MIST                            (701),
        SMOKE                           (711),
        HAZE                            (721),
        SAND_OR_DUST_WHIRLS             (731),
        FOG                             (741),
        /* Clouds */
        SKY_IS_CLEAR                    (800),
        FEW_CLOUDS                      (801),
        SCATTERED_CLOUDS                (802),
        BROKEN_CLOUDS                   (803),
        OVERCAST_CLOUDS                 (804),
        /* Extreme */
        TORNADO                         (900),
        TROPICAL_STORM                  (901),
        HURRICANE                       (902),
        COLD                            (903),
        HOT                             (904),
        WINDY                           (905),
        HAIL                            (906);

        private int id;
        ConditionCode (int code) {
            this.id = code;
        }
        @JsonCreator
        static public ConditionCode valueof (int id) {
            for (ConditionCode condition : ConditionCode.values ()) {
                if (condition.id == id)
                    return condition;
            }
            return ConditionCode.UNKNOWN;
        }
        public int getId () {
            return this.id;
        }
    }

    public static class Main {
        private float temp;
        private float tempMin;
        private float tempMax;
        private float pressure;
        private float humidity;

        public Main(){};

        public float getTemp() {
            return temp;
        }

        public void setTemp(float temp) {
            this.temp = temp;
        }

        public float getTempMin() {
            return tempMin;
        }

        public void setTempMin(float tempMin) {
            this.tempMin = tempMin;
        }

        public float getTempMax() {
            return tempMax;
        }

        public void setTempMax(float tempMax) {
            this.tempMax = tempMax;
        }

        public float getPressure() {
            return pressure;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        public float getHumidity() {
            return humidity;
        }

        public void setHumidity(float humidity) {
            this.humidity = humidity;
        }
    }
}
