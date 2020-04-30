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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity(), SensorEventListener {

    //Any random number
    val PERMISSION_ID = 42

    //use the actual Fused Location Provider API to get users current position
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private val CSV_HEADER = "datetime,lat,lon"


    private val dateFormat =
        SimpleDateFormat("M-d-yyyy HH:mm:ss")
    val TAG = UStats::class.java.simpleName


    //For accelerometer data
    lateinit var sensorManager: SensorManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Print the current usage stats. This would be called potentially every day
        // to update the usage stats
        printCurrentUsageStatus(this)
        //Reads the app usage statistics from the csv file
        read_app()

        //Launches method to get location at an interval
        //Stores and reads the csv files every 10
        getLocationAtInterval()

        //Get user's gait information

        getAccelerometerData()

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
    private fun getLocationAtInterval() {
        //Timer runs the below process every 20 seconds. In reality, study into battery usage and
        //  real feasibility of getting the location data would need to take place. In addition
        //  upon implementing methods to study the location, we would need some sort of guidance
        //  on how often to get the location in order to properly authentication the user.
        fixedRateTimer("default", false, 0L, 20000) {
            if(checkPermissions()) {
                //If permissions are enabled.
                getLastLocation()
            } else {
                //There is a bug in which, upon the first install, it asks the user for permissions
                //but then doesn't actually get the location. A restart (not reinstall) of the
                //application is necessary.
                requestPermissions()
                getLastLocation()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        //If the permissions were given and location is enabled
        //Function from tutorial by Ferdousur Rahman Sarker
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {

                        //CSV Write Occurs here
                        write_location(LocalDateTime.now(), location.latitude.toFloat(), location.longitude.toFloat())

                        //Prints the contents of the CSV
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


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        //Function from tutorial by Ferdousur Rahman Sarker
        //"in rare cases, the location can be null, so we do this, which record the location info in runtime"
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
        //Function from tutorial by Ferdousur Rahman Sarker
        //"in rare cases, the location can be null, so we do this, which record the location info in runtime
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    fun write_location(datetime: LocalDateTime?, lat: Float?, lon: Float?) {
        //Function to write the current location and timestamp to CSV file.

        //TimeLoc is an object of the datetime, lat, and lon
        val tl = TimeLoc(datetime, lat, lon)

        try {
            //Create FD to our csv file.
            var fileOS: FileOutputStream = openFileOutput("location_data.csv", Context.MODE_APPEND)

            //Write the data, separated by commas
            fileOS.write(tl.datetime.toString().toByteArray())
            fileOS.write(",".toByteArray())
            fileOS.write(tl.lat.toString().toByteArray())
            fileOS.write(",".toByteArray())
            fileOS.write(tl.lon.toString().toByteArray())
            fileOS.write("\n".toByteArray())

            //Close out file
            fileOS.close()
            println("Write CSV successfully!")
        } catch (e: Exception) {
            println("Writing CSV error!")
            e.printStackTrace()
        }
    }

    fun read_location() {
        //Function to read the location csv file and print to system
        var fileReader: BufferedReader? = null

        try {
            //Create FD to read csv file
            var fileIS: FileInputStream = openFileInput("location_data.csv")
            var isr = InputStreamReader(fileIS)
            fileReader = BufferedReader(isr)

            var line = fileReader.readLine()

            //Print each line
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

    //The below functions are from a github repo by Cole Murray
    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getStats(context: Context) {
        //Function by Cole Murray
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
        //Function By Cole Murray
        //Gets list of apps from UsageStatsManager
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
        //Function partially by Cole Murray
            try {
                //Similar write code
                var fileOS: FileOutputStream = openFileOutput("app_data.csv", Context.MODE_APPEND)

                for (u in usageStatsList) {
                    println("Writing: Pkg: " + u.packageName + "\tForegroundTime: " + u.totalTimeInForeground)

                    //Android studio thing...
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        fileOS.write(LocalDateTime.now().toString().toByteArray())
                        fileOS.write("\n".toByteArray())
                    }
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
        //Function by Cole Murray
        printUsageStats(getUsageStatsList(context))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    private fun getUsageStatsManager(context: Context): UsageStatsManager {
        //Function by Cole Murray
        return context.getSystemService("usagestats") as UsageStatsManager
    }

    fun read_app() {
        //Reads the csv file containing app usage stats
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

    private fun getAccelerometerData() {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        atv1.text = "x = ${event!!.values[0]}"
        atv2.text = "y = ${event.values[1]}"
        atv3.text = "z = ${event.values[2]}"
    }
}

class TimeLoc {
    //Object for printing to location csv file.
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



