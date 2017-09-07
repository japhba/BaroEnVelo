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
import com.google.android.gms.common.api.GoogleApiClient
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.Ignore
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMQuery
import ninja.sakib.pultusorm.exceptions.PultusORMException
import java.io.File
import java.nio.file.FileSystem

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;  //Document:  needed to add this to init SensingKit
import org.sensingkit.sensingkitlib.data.SKSensorData;
import org.sensingkit.sensingkitlib.SKSensorDataListener;
import org.sensingkit.sensingkitlib.configuration.SKConfiguration
import org.sensingkit.sensingkitlib.data.SKBarometerData
import org.sensingkit.sensingkitlib.data.SKLocationData

import com.google.android.gms.*
import com.google.android.gms.location.LocationServices

class Kontrollzentrum : AppCompatActivity(), SensorEventListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontrollzentrum)

        val initPoint = getSharedPreferences("lastPoint", Context.MODE_PRIVATE)
        val pointEditor = initPoint.edit()
        pointEditor.putFloat("pressure", 0.0f)
        pointEditor.putFloat("lat", 0.0f)
        pointEditor.putFloat("lng", 0.0f)


        if (!getSharedPreferences("Information", Context.MODE_PRIVATE).contains("user_id")) {

            val sharedPref = getSharedPreferences("Information", Context.MODE_PRIVATE);
            val editor = sharedPref.edit();

            // Save your string in SharedPref
            val random = Random()
            val randUserID = random.nextInt((9999-1000)+1)+1000
            editor.putInt("user_id", randUserID)
            editor.commit();

        }

        val mGoogleApiClient: GoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */,
                      GoogleApiClient.OnConnectionFailedListener {  } /* OnConnectionFailedListener */)
            .addApi(LocationServices.API)
            .build();

        val sensorLib = SensingKitLib.getSensingKitLib(this)


        Log.i("user", getSharedPreferences("Information", Context.MODE_PRIVATE).getString("user_id",""))

        val textView: TextView = findViewById(R.id.Anzeige)
        textView.setOnClickListener {
            textView.text = "Heute ist wirklich Sonntag"
        }

        val startBtn: Button = findViewById(R.id.start)
        startBtn.setOnClickListener {
            sensorLib.registerSensor(SKSensorType.BAROMETER)
            sensorLib.registerSensor(SKSensorType.LOCATION)

            sensorLib.subscribeSensorDataListener(SKSensorType.BAROMETER) { moduleType, sensorData ->
                val raw = sensorData as SKBarometerData
                //Log.i("alt", raw.pressure.toString())  // Print data in CSV format

                fun updateData(lat: Double, lng: Double, alt: Double) {
                    Log.i("Data", lat.toString() + ", " + lng.toString() + ", " + alt)
                    val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                    val database: PultusORM = PultusORM("local5.db", appPath)

                    val dreipunkt: Dreipunkt = Dreipunkt()
                    if (lat != null) {
                        dreipunkt.lat = lat.toDouble()
                    }
                    if (lng != null) {
                        dreipunkt.lng = lng.toDouble()
                    }
                    //TODO: fancy pressure normalisation
                    dreipunkt.alt = alt

                    database.save(dreipunkt)

                }

                fun tmpData(lat: Double, lng: Double, alt: Double, prs: Double) {
                    val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                    val database: PultusORM = PultusORM("tmp.db", appPath)

                    val vierpunkt: Vierpunkt = Vierpunkt()

                    vierpunkt.lat = lat.toDouble()
                    vierpunkt.lng = lng.toDouble()
                    vierpunkt.alt = alt.toDouble()

                    //TODO: fancy pressure normalisation
                    vierpunkt.prs = prs.toDouble()

                    database.save(vierpunkt)
                }

                val sharedPref = getSharedPreferences("lastPoint", Context.MODE_PRIVATE);
                val editor = sharedPref.edit()
                editor.putFloat("pressure", raw.pressure).commit()

                val lat = sharedPref.getFloat("lat", 0.0f)
                val lng = sharedPref.getFloat("lng", 0.0f)
                val alt = sharedPref.getFloat("alt", 0.0f)

                tmpData(lat.toDouble(), lng.toDouble(), alt.toDouble(), raw.pressure.toDouble())
            }

            sensorLib.subscribeSensorDataListener(SKSensorType.LOCATION) { moduleType, sensorData ->
                val raw = sensorData as SKLocationData
                Log.i("loc", raw.latitude.toString() + ", " + raw.longitude.toString())  // Print data in CSV format

                fun updateData(lat: Double, lng: Double, alt: Double) {
                    Log.i("Data", lat.toString() + ", " + lng.toString() + ", " + alt)
                    val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                    val database: PultusORM = PultusORM("local5.db", appPath)

                    val dreipunkt: Dreipunkt = Dreipunkt()
                    if (lat != null) {
                        dreipunkt.lat = lat.toDouble()
                    }
                    if (lng != null) {
                        dreipunkt.lng = lng.toDouble()
                    }
                    //TODO: fancy pressure normalisation
                    dreipunkt.alt = alt.toDouble()

                    database.save(dreipunkt)

                }

                fun tmpData(lat: Double, lng: Double, alt: Double, prs: Double) {
                    val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                    val database: PultusORM = PultusORM("tmp.db", appPath)

                    val vierpunkt: Vierpunkt = Vierpunkt()

                    vierpunkt.lat = lat.toDouble()
                    vierpunkt.lng = lng.toDouble()
                    vierpunkt.alt = alt.toDouble()

                    //TODO: fancy pressure normalisation
                    vierpunkt.prs = prs.toDouble()

                    database.save(vierpunkt)
                }

                val sharedPref = getSharedPreferences("lastPoint", Context.MODE_PRIVATE);
                val editor = sharedPref.edit()
                editor.putFloat("lat", raw.location.latitude.toFloat()).commit()
                editor.putFloat("lng", raw.location.longitude.toFloat()).commit()
                editor.putFloat("alt", raw.altitude.toFloat()).commit()

                val pressure = sharedPref.getFloat("pressure", 0.0f)

                tmpData(raw.latitude, raw.longitude, raw.altitude, pressure.toDouble())

            }




            sensorLib.startContinuousSensingWithAllRegisteredSensors()
        }


        val stopBtn: Button = findViewById(R.id.stop)
        stopBtn.setOnClickListener {
            sensorLib.stopContinuousSensingWithAllRegisteredSensors()
            sensorLib.deregisterSensor(SKSensorType.LOCATION)
            sensorLib.deregisterSensor(SKSensorType.BAROMETER)

            fun updateData(lat: Double, lng: Double, alt: Double) {
                Log.i("Data", lat.toString() + ", " + lng.toString() + ", " + alt)
                val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                val database: PultusORM = PultusORM("local6.db", appPath)

                val dreipunkt: Dreipunkt = Dreipunkt()
                if (lat != null) {
                    dreipunkt.lat = lat.toDouble()
                }
                if (lng != null) {
                    dreipunkt.lng = lng.toDouble()
                }
                //TODO: fancy pressure normalisation
                dreipunkt.alt = alt.toDouble()

                database.save(dreipunkt)

            }

            val appPath = "/data/user/0/com.japhba.baroenvelo/files"
            val tmpData: PultusORM = PultusORM("tmp.db", appPath)
            //val database: PultusORM = PultusORM("local5.db", appPath)

            val punkte = tmpData.find(Vierpunkt())
            var moyenPrs = 0.0
            var moyenAlt = 0.0
            for (value in punkte) {
                val punkt = value as Vierpunkt
                moyenPrs += punkt.prs
                moyenAlt += punkt.alt
            }
            moyenPrs = moyenPrs/punkte.size
            moyenAlt = moyenAlt/punkte.size

            for (value in punkte) {
                val pkt = value as Vierpunkt
                updateData(pkt.lat, pkt.lng, (pkt.prs/moyenPrs)*moyenAlt) //Dreisatz
            }

            val cleanDB: PultusORM = PultusORM("tmp.db", appPath)
            cleanDB.drop(Vierpunkt())
            cleanDB.close()

        }

        val uploadBtn: Button = findViewById(R.id.upload)
        uploadBtn.setOnClickListener {
            val appPath = "/data/user/0/com.japhba.baroenvelo/files"
            val database: PultusORM = PultusORM("local6.db", appPath)

            val punkte = database.find(Dreipunkt())
            var lat = (punkte[0] as Dreipunkt).lat.toString()
            var lng = (punkte[0] as Dreipunkt).lng.toString()
            var alt = (punkte[0] as Dreipunkt).alt.toString()

            for ((index, value) in punkte.withIndex()) {
                if (index == 0) continue
                val punkt = value as Dreipunkt
                lat += ":" + punkt.lat.toString()
                lng += ":" + punkt.lng.toString()
                alt += ":" + punkt.alt.toString()
                //break
            }

            //Log.i("request" , "{ \"userID\" : \"admin\" , \"lat\" : " + lat +" , \"lon\" : "  + lng +  " , \"alt\" : " + alt +  " , \"sternzeit\" : \"0\" }")
            Fuel.post("http://baroenvelo.azurewebsites.net/insert.php", listOf("userID" to "admin", "lat" to lat, "lng" to lng, "alt" to alt, "sternzeit" to "0")).response { request, response, result ->

                Log.i("debug", request.toString())
                Log.i("response", response.toString())
            }
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



