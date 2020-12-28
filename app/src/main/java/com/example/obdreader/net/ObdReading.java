/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.obdreader.net;

import java.util.HashMap;

/**
 * DTO用于OBD读数。
 */
public class ObdReading {
    private double latitude, longitude, altitude;
    private long timestamp;
    private String vin; //车辆编号
    private java.util.Map<String, String> readings;

    public ObdReading() {
        readings = new HashMap<>();
    }

    public ObdReading(double latitude, double longitude, double altitude, long timestamp, String vin, java.util.Map<String, String> readings) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timestamp = timestamp;
        this.vin = vin;
        this.readings = readings;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vehicleid) {
        this.vin = vehicleid;
    }

    public java.util.Map<String, String> getReadings() {
        return readings;
    }

    public void setReadings(java.util.Map<String, String> readings) {
        this.readings = readings;
    }

    public String toString() {

        return "lat:" + latitude + ";" +
                "long:" + longitude + ";" +
                "alt:" + altitude + ";" +
                "vin:" + vin + ";" +
                "readings:" + readings.toString().substring(10).replace("}", "").replace(",", ";");
    }

}
