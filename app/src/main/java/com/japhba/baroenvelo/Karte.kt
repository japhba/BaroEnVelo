package com.japhba.baroenvelo

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.MapView
import android.graphics.Color.parseColor
import android.text.TextUtils
import android.util.Log
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.lang.reflect.Array.getDouble
import org.json.JSONArray
import org.json.JSONObject

class Karte: AppCompatActivity(), OnMapReadyCallback {

    override fun onMapReady(mapboxMap: MapboxMap) {

        val points = getPoints()
        mapboxMap.addPolyline(PolylineOptions()
                .addAll(points)
                .color(Color.parseColor("#b2ebf2"))
                .width(0.2f))
                //.alpha = 0.1F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.access_token))

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_karte)

        var mapView: MapView = findViewById(R.id.mapview)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }

    fun getPoints(): ArrayList<LatLng>  {
        val inputStream = assets.open("AC-PH.geojson")
        val sb = inputStream.bufferedReader().use { it.readText() }
        Log.i("json", sb)
        val points: ArrayList<LatLng> = ArrayList()

        // Parse JSON
        val json = JSONObject(sb)
        val geometry = json.getJSONObject("geometry")
        if (geometry != null) {
            val type = geometry.getString("type")

            // Our GeoJSON only has one feature: a line string
            if (!TextUtils.isEmpty(type) && type.equals("LineString", ignoreCase = true)) {

                // Get the Coordinates
                val coords = geometry.getJSONArray("coordinates")

                for (lc in 0..coords.length() - 1) {
                    val coord = coords.getJSONArray(lc)
                    val latLng = LatLng(coord.getDouble(1), coord.getDouble(0))
                    points.add(latLng)
                }
            }
        }

        inputStream.close()
        return points

    }
}
