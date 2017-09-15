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

import com.mapbox.mapboxsdk.style.functions.Function.property
import com.mapbox.mapboxsdk.style.functions.Function.zoom
import com.mapbox.mapboxsdk.style.functions.stops.Stop.stop
import com.mapbox.mapboxsdk.style.functions.stops.Stops.categorical
import com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius

import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.commons.geojson.Feature
import com.mapbox.services.commons.geojson.FeatureCollection
import com.mapbox.services.commons.geojson.LineString
import kotlinx.android.synthetic.main.activity_kontrollzentrum.*
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
        //Log.i("sfas", points.toString())
        var features: ArrayList<com.mapbox.services.commons.models.Position> = ArrayList()


        for(point in points) {
            features.add(com.mapbox.services.commons.models.Position.fromLngLat(point.longitude, point.latitude))
        }

        val lineString = LineString.fromCoordinates(features)
        val featureCollection = FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(lineString)))

        val data = GeoJsonSource("dataSource", featureCollection)
        mapboxMap.addSource(data)
        val circleLayer = CircleLayer("circles", "dataSource")
                .withProperties(circleRadius(
                        zoom(
                                exponential(
                                        stop(12, circleRadius(1f)),
                                        stop(22, circleRadius(50f))
                                ).withBase(1.75f)
                        )
                ),
                 circleColor(Color.parseColor("#0099ff"))
                        )

        mapboxMap.addLayer(circleLayer)

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
            points.add(LatLng(pkt.lat, pkt.lng))
        }
        return points
    }
}
