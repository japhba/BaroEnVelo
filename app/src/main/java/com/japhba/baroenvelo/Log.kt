package com.japhba.baroenvelo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import ninja.sakib.pultusorm.core.PultusORM

/**
 * Created by jan on 10.10.17.
 */


class Log: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val appPath = "/data/user/0/com.japhba.baroenvelo/files"
        val database: PultusORM = PultusORM("local9.db", appPath)

        val logView: TextView = findViewById(R.id.logView)

        val punkte = database.find(Dreipunkt())
        var log = ""

        for ((index, value) in punkte.withIndex()) {
            if (index == 0) continue
            val punkt = value as Dreipunkt
            //Log.i("alt", punkt.alt.toString()) //check if pressure was recorded
            if(punkt.lng < 7) {
                var tmpalt = (288.15/0.0065)*( 1 - Math.pow(punkt.alt/1013.25, 1/5.255))
                log += "Lat: " + punkt.lat.toString() + "  " + "Lng: " + punkt.lng.toString() + "  " + "Prs:" + tmpalt.toString() + "\n"
            }
        }

        logView.text = log

}   }