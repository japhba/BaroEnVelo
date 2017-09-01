package com.japhba.baroenvelo

/**
 * Created by jan on 14.08.17. (C) Github
 */

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import ninja.sakib.pultusorm.core.PultusORM


class Dreipunkt {
    var lat: Double = 0.0
    var lon: Double = 0.0
    var alt: String = ""
}


class LocationTrackingService : Service() {

    var locationManager: LocationManager? = null
    var sensorManager2: SensorManager? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {

        val localBroadcastManager = LocalBroadcastManager.getInstance(this)

        // Start listening for relevant events BEFORE announcing readiness...
        // that's basically the whole point of announcing ready.
        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Service can take appropriate action now
                Log.i("APP", "Service got intent with action: ${intent.action} and operation ${intent.getStringExtra("operation")}")
            }
        }, IntentFilter("Hallo"))

        // Everything is squared away, let's signal we can start handling messages.
        localBroadcastManager.sendBroadcast(Intent("Hallo"))


        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            sensorManager2 = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager


        var sensorManager = sensorManager2
        sensorManager?.registerListener(
                LTRSensorListener(),
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL
        )




        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }


    companion object {
        val TAG = "LocationTrackingService"

        const val INTENT = "Koordinaten"

        var coords: Location? = null
        var alt: String? = null

        val INTERVAL = 1000.toLong() // In milliseconds
        val DISTANCE = 10.toFloat() // In meters

        val locationListeners = arrayOf(
                LTRLocationListener(LocationManager.GPS_PROVIDER),
                LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        class LTRLocationListener(provider: String) : android.location.LocationListener {

            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                coords = lastLocation
                Log.i(TAG, lastLocation.toString())

                updateData(lastLocation, "alt")
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

        }

        class LTRSensorListener: android.hardware.SensorEventListener {

            override fun onSensorChanged(event: SensorEvent?) {
                val pressure = event!!.values[0]
                alt = SensorManager.getAltitude(
                        SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure).toString()
                updateData(coords, alt!!)
            }


            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }
        }

        fun updateData(loc: Location?, alt: String) {
            Log.i("Data", loc.toString() + alt)
            val appPath = "/data/user/0/com.japhba.baroenvelo/files"
            val database: PultusORM = PultusORM("local.db", appPath)

            val dreipunkt: Dreipunkt = Dreipunkt()
            if (loc != null) {
                dreipunkt.lat = loc.latitude
            }
            if (loc != null) {
                dreipunkt.lon = loc.longitude
            }
            //TODO: fancy pressure normalisation
            dreipunkt.alt = alt

            database.save(dreipunkt)

        }


    }

}