package com.japhba.baroenvelo

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_kontrollzentrum.*
import android.widget.Toast
import android.util.Log
import android.view.View
import java.io.IOException
import java.util.*
import android.location.LocationManager
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager

class Kontrollzentrum : AppCompatActivity(), SensorEventListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontrollzentrum)

        val textView: TextView = findViewById(R.id.Anzeige)
        textView.setOnClickListener {
            textView.text = "Heute ist wirklich Sonntag"
        }

        val localBroadcastManager = LocalBroadcastManager.getInstance(this)

        // Bind our "serverReady" listener BEFORE we start the Service,
        // in case it happens to initialize quickly
        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // We can stop listening immediately
                localBroadcastManager.unregisterReceiver(this)

                // Now that the Service is ready, we can start sending it messages.
                // You can do this from any activity
                localBroadcastManager
                        .sendBroadcast(Intent(LocationTrackingService.INTENT)
                                .putExtra("operation", "doSomething"))
            }
            // defined in ServerService below
        }, IntentFilter(LocationTrackingService.INTENT))

        val context = applicationContext
        val intent = Intent(context, LocationTrackingService::class.java)
        if (context != null) {
            context.startService(intent)
        }

    }

    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }


    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val textView2: TextView = findViewById(R.id.Anzeige)

        val pressure = event!!.values[0]
        val altitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure).toString()
        textView2.text = altitude

    }



}



