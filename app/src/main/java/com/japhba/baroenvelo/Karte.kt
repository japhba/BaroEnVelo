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
import com.cocoahero.android.geojson.GeoJSON
import com.cocoahero.android.geojson.Point
import com.cocoahero.android.geojson.Position
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.commons.geojson.Feature
import ninja.sakib.pultusorm.core.PultusORM
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.lang.reflect.Array.getDouble
import org.json.JSONArray
import org.json.JSONObject

class Karte: AppCompatActivity(), OnMapReadyCallback {

    override fun onMapReady(mapboxMap: MapboxMap) {

        val points = getPoints()
        var geojsonString = ""
        var features: ArrayList<String> = ArrayList()

        var geojson = JSONObject()

        for(point in points) {
            //TODO: proper JSON creation
            val element = point.latitude.toString() + point.longitude.toString()
            features.add(element)
        }

        //geojson.put("features", )

        val data = GeoJsonSource("dataSource", geojson.toString(0))
        val circleLayer = CircleLayer("circles", "dataSource")

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
        val appPath = "/data/user/0/com.japhba.baroenvelo/files"
        val database: PultusORM = PultusORM("local9.db", appPath)
        val points: ArrayList<LatLng> = ArrayList()

        val punkte = database.find(Dreipunkt())
        for(punkt in punkte) {
            val pkt = punkt as Dreipunkt
            points.add(LatLng(pkt.lat, pkt.lat))
        }
        return points
    }
}
