package com.japhba.baroenvelo

import android.Manifest
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
import android.util.Log
import java.util.*
import android.location.LocationManager
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.widget.Button
import com.github.kittinunf.fuel.Fuel
/*import com.github.pwittchen.reactivesensors.library.ReactiveSensorEvent
import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter
import com.github.pwittchen.reactivesensors.library.ReactiveSensors*/
import com.google.android.gms.common.api.GoogleApiClient
import ninja.sakib.pultusorm.core.PultusORM


//import rx.android.schedulers.AndroidSchedulers
//import rx.functions.Action1

//import org.sensingkit.*;
import org.sensingkit.sensingkitlib.*


import org.sensingkit.sensingkitlib.data.SKBarometerData
import org.sensingkit.sensingkitlib.data.SKLocationData

import com.google.android.gms.location.LocationServices
import com.maxcruz.reactivePermissions.ReactivePermissions
import com.maxcruz.reactivePermissions.entity.Permission

class Kontrollzentrum : AppCompatActivity(), SensorEventListener {

    // Define a code to request the permissions
    private val REQUEST_CODE = 10
    // Instantiate the library
    val reactive: ReactivePermissions = ReactivePermissions(this, REQUEST_CODE)

    // Receive the response from the user and pass to the lib
    override fun onRequestPermissionsResult(code: Int, permissions: Array<String>, results: IntArray) {
        if (code == REQUEST_CODE)
            reactive.receive(permissions, results)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontrollzentrum)

        val initPoint = getSharedPreferences("lastPoint", Context.MODE_PRIVATE)
        val pointEditor = initPoint.edit()
        pointEditor.putFloat("prs", 0.0f)
        pointEditor.putFloat("lat", 0.0f)
        pointEditor.putFloat("lng", 0.0f)
        pointEditor.putFloat("alt", 0.0f)

        //get permission
        val location = Permission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                R.string.rationale_permission_title,
                false // If the user deny this permission, block the app
        )

        val permissions = listOf(location)
        reactive.evaluate(permissions)


        val locationManager: LocationManager
        val locationProvider: LocationProvider

        /* Get LocationManager and LocationProvider for GPS */
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);

        /* Check if GPS LocationProvider supports altitude */
        Log.i("supportsAlt", locationProvider.supportsAltitude().toString())

        //initial startup
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

        //Log.i("user", getSharedPreferences("Information", Context.MODE_PRIVATE).getInt("user_id",""))

        val textView: TextView = findViewById(R.id.Anzeige)
        textView.setOnClickListener {
            textView.text = "Heute ist wirklich Sonntag"
        }

        val startBtn: Button = findViewById(R.id.start)
        startBtn.setOnClickListener {
            sensorLib.registerSensor(SKSensorType.BAROMETER)
            sensorLib.registerSensor(SKSensorType.LOCATION)

            /*ReactiveSensors(this).observeSensor(Sensor.)
                    .subscribeOn(rx.schedulers.Schedulers.computation())
                    .filter(ReactiveSensorFilter.filterSensorChanged())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Action1<ReactiveSensorEvent>() {
                        fun call(reactiveSensorEvent: ReactiveSensorEvent) {
                            val event: SensorEvent = reactiveSensorEvent.getSensorEvent();

                            val x = event.values[0];
                            val y = event.values[1];
                            val z = event.values[2];

                            val message: String = String.format("x = %f, y = %f, z = %f", x, y, z);

                            Log.d("gyroscope readings", message);
                        }
                    });*/



            sensorLib.subscribeSensorDataListener(SKSensorType.BAROMETER) { moduleType, sensorData ->
                val raw = sensorData as SKBarometerData
                //Log.i("prs", raw.pressure.toString())  // Print data in CSV format

                fun updateData(lat: Double, lng: Double, alt: Double) {
                    Log.i("Data", lat.toString() + ", " + lng.toString() + ", " + alt)
                    val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                    val database: PultusORM = PultusORM("local9.db", appPath)

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
                editor.putFloat("prs", raw.pressure).commit()

                val lat = sharedPref.getFloat("lat", 0.0f)
                val lng = sharedPref.getFloat("lng", 0.0f)
                val alt = sharedPref.getFloat("alt", 0.0f)

                //tmpData(lat.toDouble(), lng.toDouble(), alt.toDouble(), raw.pressure.toDouble())
            }

            sensorLib.subscribeSensorDataListener(SKSensorType.LOCATION) { moduleType, sensorData ->
                val raw = sensorData as SKLocationData

                val coords: TextView = findViewById(R.id.coords)

                Log.i("accuracy", raw.accuracy.toString())
                Log.i("loc", raw.latitude.toString() + ", " + raw.longitude.toString() + ", " + raw.altitude.toString())  // Print data in CSV format

                fun updateData(lat: Double, lng: Double, alt: Double) {
                    Log.i("Data", lat.toString() + ", " + lng.toString() + ", " + alt)
                    val appPath = "/data/user/0/com.japhba.baroenvelo/files"
                    val database: PultusORM = PultusORM("local9.db", appPath)

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

                val pressure = sharedPref.getFloat("prs", 0.0f)
                coords.text = raw.latitude.toString() + ", " + raw.longitude.toString() + ", " + pressure.toString()

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
                val database: PultusORM = PultusORM("local9.db", appPath)

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

            Log.i("moyen", moyenAlt.toString() + ", " + moyenPrs.toString())

            for (value in punkte) {
                val pkt = value as Vierpunkt
                //updateData(pkt.lat, pkt.lng, (pkt.prs/moyenPrs)*moyenAlt) //Dreisatz
                updateData(pkt.lat, pkt.lng, pkt.prs) //Dreisatz
            }

            val cleanDB: PultusORM = PultusORM("tmp.db", appPath)
            cleanDB.drop(Vierpunkt())
            cleanDB.close()

        }

        val uploadBtn: Button = findViewById(R.id.uploadBtn)
        uploadBtn.setOnClickListener {
            val appPath = "/data/user/0/com.japhba.baroenvelo/files"
            val database: PultusORM = PultusORM("local9.db", appPath)

            val textView: TextView = findViewById(R.id.uploadTxt)
            textView.text = "Upload gestartet..."

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
                val textView: TextView = findViewById(R.id.uploadTxt)
                textView.text = "Upload beendet."
                //clearDB?
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



