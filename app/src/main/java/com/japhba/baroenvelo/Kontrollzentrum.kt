package com.japhba.baroenvelo

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Kontrollzentrum : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontrollzentrum)

        val textView: TextView = findViewById(R.id.Anzeige)
        textView.setOnClickListener {
            textView.text = "Heute ist wirklich Sonntag"
        }

        val sensorManager: SensorManager by lazy {
            getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
    }


}

class SensorActivity : Activity(), SensorEventListener {
    private val mSensorManager: SensorManager
    private val mAccelerometer: Sensor

    init {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override protected fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override protected fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val pressure = event.values[0]
        val altitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure).toString()
        print(altitude)
    }
}
