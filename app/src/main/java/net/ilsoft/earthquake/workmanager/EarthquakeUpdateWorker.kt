package net.ilsoft.earthquake.workmanager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.*
import net.ilsoft.earthquake.EarthquakeMainActivity
import net.ilsoft.earthquake.PreferencesActivity
import net.ilsoft.earthquake.R
import net.ilsoft.earthquake.room.Earthquake
import net.ilsoft.earthquake.room.EarthquakeDatabaseAccessor
import net.ilsoft.earthquake.workmanager.EarthquakeUpdateWorker.Companion.NOTIFICATION_CHANNEL
import net.ilsoft.earthquake.workmanager.EarthquakeUpdateWorker.Companion.NOTIFICATION_ID
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.List

class EarthquakeUpdateWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {
    companion object {
        const val TAG = "EarthquakeUpdateWorker"
        const val UPDATE_WORKER_TAG = "update_worker"
        const val PERIODIC_WORKER_TAG = "periodic_worker"

        const val NOTIFICATION_CHANNEL = "earthquake"
        const val NOTIFICATION_ID = 1

        fun scheduleUpdateWorker(context: Context) {
            val constraints: Constraints = Constraints.Builder().apply {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }.build()

            val request: OneTimeWorkRequest =
                // Tell which work to execute
                OneTimeWorkRequestBuilder<EarthquakeUpdateWorker>()
                    .setConstraints(constraints)
                    .addTag(UPDATE_WORKER_TAG)
                    .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override fun doWork(): Result {
        val Iearthquakes = mutableListOf<Earthquake>()
        try {
            val quakeFeed = applicationContext.getString(R.string.earthquake_feed)
            val connection = URL(quakeFeed).openConnection() as HttpURLConnection
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val input = connection.inputStream
                val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()

                val dom = db.parse(input)
                val docEle = dom.documentElement

                // 각 지진 항목의 내역을 가져온다.
                val nl: NodeList? = docEle.getElementsByTagName("entry")
                if (nl != null && nl.length > 0) {
                    for (i in 0 until nl.length) {
                        val entry = nl.item(i) as Element
                        val id = entry.getElementsByTagName("id").item(0) as Element
                        val title = entry.getElementsByTagName("title").item(0) as Element
                        val g = entry.getElementsByTagName("georss:point").item(0) as Element
                        val `when` = entry.getElementsByTagName("updated").item(0) as Element
                        val link = entry.getElementsByTagName("link").item(0) as Element

                        val idString = id.firstChild.nodeValue
                        var details = title.firstChild.nodeValue
                        val hostname = "http://earthquake.usgs.gov"
                        val linkString = hostname + link.getAttribute("href")
                        val point = g.firstChild.nodeValue
                        val dt = `when`.firstChild.nodeValue
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
                        var qdate = GregorianCalendar(0, 0, 0).time
                        try {
                            qdate = sdf.parse(dt) as Date
                        } catch (e: ParseException) {
                            Log.e(TAG, "Date parsing exception.", e)
                        }

                        val location = point.split(" ")
                        val l = Location("dummyGPS")
                        l.latitude = location[0].toDouble()
                        l.longitude = location[1].toDouble()

                        val magnitudeString = details.split(" ")[1]
                        val end = magnitudeString.length - 1
                        val magnitude = magnitudeString.substring(0, end).toDouble()
                        details =
                            if (details.contains("-")) details.split("-")[1].trim() else ""
                        Iearthquakes.add(
                            Earthquake(idString, qdate, details, l, magnitude, linkString)
                        )
                    }
                }
            }
            Log.d(TAG,"$tags 태그입니다.")
            val largestNewEarthquake = findLargestNewEarthquake(Iearthquakes)
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val minimumMagnitude = prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3")
            if (largestNewEarthquake != null && largestNewEarthquake.magnitude >= minimumMagnitude!!.toDouble()) {
                // 알림을 발생시킨다.
                broadcastNotification(largestNewEarthquake)
            }
            //새로 파싱된 Earthquakes 배열을 데이터베이스에 저장한다.
            EarthquakeDatabaseAccessor
                .getInstance(applicationContext)
                .earthquakeDAO()
                .insertEarthquakes(Iearthquakes)

            // 연결을 해제한다.
            connection.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }

        return Result.success()
    }

    fun scheduleNextUpdate(context: Context, params: WorkerParameters) {
        if (params.tags.contains(UPDATE_WORKER_TAG)) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val updateFreq = prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60")!!.toInt()
            val autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false)

            if (autoUpdateChecked) {
                val constraints: Constraints = Constraints.Builder().apply {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }.build()

                val request: PeriodicWorkRequest =
                    // Tell which work to execute
                    PeriodicWorkRequestBuilder<EarthquakeUpdateWorker>(15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .addTag(PERIODIC_WORKER_TAG)
                        .setInitialDelay(60L, TimeUnit.MINUTES)
                        .build()

                WorkManager.getInstance(context).enqueue(request)
            }
        }
    }

    private fun findLargestNewEarthquake(newEarthquakes: List<Earthquake>): Earthquake? {
        val earthquakes = EarthquakeDatabaseAccessor
            .getInstance(applicationContext)
            .earthquakeDAO()
            .loadAllEarthquakesBlocking()
        var largestNewEarthquake: Earthquake? = null
        newEarthquakes
            .filter { earthquakes.contains(it) }
            .forEach {
                if (largestNewEarthquake == null || it.magnitude > largestNewEarthquake?.magnitude!!) {
                    largestNewEarthquake = it
                }
            }
        return largestNewEarthquake
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.earthquake_channel_name)
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.enableVibration(true)

            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun broadcastNotification(earthquake: Earthquake) {
        createNotificationChannel()

        val startActivityIntent = Intent(applicationContext, EarthquakeMainActivity::class.java)
        var launchIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            startActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val earthquakeNotificationBuilder = NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL
        ).apply {
            setSmallIcon(R.drawable.notification_icon)
            setColor(
                ContextCompat.getColor(
                    applicationContext,
                    com.google.android.material.R.color.design_default_color_primary
                )
            )
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(launchIntent)
            setAutoCancel(true)
            setShowWhen(true)
            setWhen(earthquake.date!!.time)
            setContentTitle("M:${earthquake.magnitude}")
            setContentText(earthquake.details)
            setStyle(NotificationCompat.BigTextStyle().bigText(earthquake.details))
        }

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, earthquakeNotificationBuilder.build())
    }

    override fun onStopped() {
        super.onStopped()
        Log.d(TAG, "onStopped")
    }
}