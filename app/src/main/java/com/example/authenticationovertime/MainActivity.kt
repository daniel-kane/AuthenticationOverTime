/*
This code follows a tutorial by Ferdousur Rahman Sarker on getting location in kotlin
https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html

They provide some useful functions for checking and obtaining permission to use the location,
and then show how to use the Fused Location Prodiver API to get the location

It is not exactly what we need to do, but it is a step in the right direction, and we can
reference this code for our location accessing needs.

If you're using an emulator, you'll get the location of the google headquarters. Extra steps
are required for the emulator, but I'm not sure how to do that right now.

 */

package com.example.authenticationovertime

import android.Manifest
import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.io.*
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity() {

    //Any random number
    val PERMISSION_ID = 42

    //use the actual Fused Location Provider API to get users current position
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    //Our db reference
    //private lateinit var database: DatabaseReference
    private val CSV_HEADER = "datetime,lat,lon"




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //database = Firebase.database.reference

        //Call this in a loop/ at an interval. Should figure out how to get
        //location when the app is closed/in background

        //getStats(this)

        //var usm = getUsageStatsManager(this)

      /*  if (getUsageStatsList(this).isEmpty()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }*/
        /*if (UStats.getUsageStatsList(this).isEmpty()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }*/

        printCurrentUsageStatus(this)

        read_app()

        //UStats().printanything()
        //getLocationAtInterval()

        interval()

        //get app usage data here

        //get accerometer data here

    }

    //Checks if GPS or network provider is enabled for location manager
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //Checks if the permission was granted by user to use location
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    //Requests permission from the user
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun interval() {
        fixedRateTimer("default", false, 0L, 20000) {
            if(checkPermissions()) {
                getLastLocation()
            } else {
                requestPermissions()
                getLastLocation()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        //If the permissions were given and location is enabled
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        //WRITE TO DATABASE HERE***************************************************

                        write_location(LocalDateTime.now(), location.latitude.toFloat(), location.longitude.toFloat())

                        read_location()

                        findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                        findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()

                    }
                }
            } else {

                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            //Requests user permission
            requestPermissions()
        }
    }

    //"in rare cases, the location can be null, so we do this, which record the location info in runtime
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        //TO get location every 5-10 seconds, change the values above to 10000 and 5000


        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //When we get an update, do the mLocationCallback code and update textviews
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    fun write_location(datetime: LocalDateTime?, lat: Float?, lon: Float?) {

        val tl = TimeLoc(datetime, lat, lon)

        try {

            var fileOS: FileOutputStream = openFileOutput("location_data.csv", Context.MODE_APPEND)

           // fileOS.write(CSV_HEADER.toByteArray())
            fileOS.write("\n".toByteArray())
            fileOS.write(tl.datetime.toString().toByteArray())
            fileOS.write(",".toByteArray())
            fileOS.write(tl.lat.toString().toByteArray())
            fileOS.write(",".toByteArray())
            fileOS.write(tl.lon.toString().toByteArray())
            fileOS.write("\n".toByteArray())

            fileOS.close()

            println("Write CSV successfully!")
        } catch (e: Exception) {
            println("Writing CSV error!")
            e.printStackTrace()
        }
    }

    fun read_location() {
        var fileReader: BufferedReader? = null

        try {
            var fileIS: FileInputStream = openFileInput("location_data.csv")
            var isr = InputStreamReader(fileIS)

            fileReader = BufferedReader(isr)

            var line = fileReader.readLine()

            while(line != null) {
                println(line)
                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }
//}

//class UStats {

    private val dateFormat =
        SimpleDateFormat("M-d-yyyy HH:mm:ss")
    val TAG = UStats::class.java.simpleName


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getStats(context: Context) {
        val usm = context.getSystemService("usagestats") as UsageStatsManager
        val interval = UsageStatsManager.INTERVAL_YEARLY
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -1)
        val startTime = calendar.timeInMillis
        Log.d(TAG, "Range start:" + dateFormat.format(startTime))
        Log.d(TAG, "Range end:" + dateFormat.format(endTime))
        val uEvents = usm.queryEvents(startTime, endTime)
        while (uEvents.hasNextEvent()) {
            val e = UsageEvents.Event()
            uEvents.getNextEvent(e)
            if (e != null) {
                Log.d(
                    TAG,
                    "Event: " + e.packageName + "\t" + e.timeStamp
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getUsageStatsList(context: Context): List<UsageStats> {
        val usm = getUsageStatsManager(context)
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -1)
        val startTime = calendar.timeInMillis
        Log.d(TAG, "Range start:" + dateFormat.format(startTime))
        Log.d(TAG, "Range end:" + dateFormat.format(endTime))
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun printUsageStats(usageStatsList: List<UsageStats>) {
            try {
                var fileOS: FileOutputStream = openFileOutput("app_data.csv", Context.MODE_APPEND)

                for (u in usageStatsList) {
                    println("Writing: Pkg: " + u.packageName + "\tForegroundTime: " + u.totalTimeInForeground)

                    fileOS.write(u.packageName.toString().toByteArray())
                    fileOS.write(",".toByteArray())
                    fileOS.write(u.totalTimeInForeground.toString().toByteArray())
                    fileOS.write("\n".toByteArray())
                }

                fileOS.close()

                println("Write CSV successfully!")
            } catch (e: Exception) {
                println("Writing CSV error!")
                e.printStackTrace()
            }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun printCurrentUsageStatus(context: Context) {
        printUsageStats(getUsageStatsList(context))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    private fun getUsageStatsManager(context: Context): UsageStatsManager {
        return context.getSystemService("usagestats") as UsageStatsManager
    }

    fun read_app() {
        var fileReader: BufferedReader? = null

        try {
            var fileIS: FileInputStream = applicationContext.openFileInput("app_data.csv")
            var isr = InputStreamReader(fileIS)
            fileReader = BufferedReader(isr)

            var line = fileReader.readLine()

            while(line != null) {
                println(line)
                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }


}

class TimeLoc {
    var datetime: LocalDateTime? = null
    var lat: Float? = null
    var lon: Float? = null

   // constructor() {}
    constructor(datetime: LocalDateTime?, lat: Float?, lon: Float?) {
        this.datetime = datetime
        this.lat = lat
        this.lon = lon
    }

    override fun toString(): String {
        return "TimeLoc = [date and time" + datetime.toString() + ", lat=" + lat.toString() + ", lon=" + lon.toString() + "]"
    }
}



