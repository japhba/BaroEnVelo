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
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.widget.Button
import com.github.kittinunf.fuel.Fuel
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.Ignore
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMQuery
import ninja.sakib.pultusorm.exceptions.PultusORMException
import java.io.File
import java.nio.file.FileSystem

class Kontrollzentrum : AppCompatActivity(), SensorEventListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontrollzentrum)


        val textView: TextView = findViewById(R.id.Anzeige)
        textView.setOnClickListener {
            textView.text = "Heute ist wirklich Sonntag"
        }

        val startBtn: Button = findViewById(R.id.start)
        startBtn.setOnClickListener {
            Log.i("Halli", "Hallo")
            val context = applicationContext
            val intent = Intent(context, LocationTrackingService::class.java)
            if (context != null) {
                context.startService(intent)
            }
        }

        val uploadBtn: Button = findViewById(R.id.upload)
        uploadBtn.setOnClickListener {
            //Fuel.upload("/post").source { request, url ->
                //val appPath: String = "/data/user/0/com.japhba.baroenvelo/files/"
                //val filename: String = "local.db"
                //val database: File = File(appPath, filename)
            //}
        }

        val mapBtn: Button = findViewById(R.id.viewMap)
        mapBtn.setOnClickListener {
            val showMap = Intent(this, Karte::class.java)
            startActivity(showMap)
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



