package com.example.authenticationovertime

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

object UStats {
    /*
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
        for (u in usageStatsList) {
            Log.d(
                TAG, "Pkg: " + u.packageName + "\t" + "ForegroundTime: "
                        + u.totalTimeInForeground
            )
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
    } */
}