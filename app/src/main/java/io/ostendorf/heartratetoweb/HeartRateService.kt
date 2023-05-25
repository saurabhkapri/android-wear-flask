package io.ostendorf.heartratetoweb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.RequestQueue
import com.android.volley.request.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.math.roundToInt


class HeartRateService : Service(), SensorEventListener {

    private lateinit var mHeartRateSensor: Sensor
    private lateinit var hrvSensor: Sensor
    private lateinit var mSensorManager: SensorManager
    private lateinit var httpQueue: RequestQueue
    private lateinit var preferences: SharedPreferences
    private var previousTimestamp: Long = 0

    private val CHANNEL_ID = "HeartRateService"

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, HeartRateService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, HeartRateService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        doSomething()

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(R.string.notification_title.toString())
            .setContentText(R.string.notification_text.toString())
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            R.string.notification_channel_title.toString(),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager!!.createNotificationChannel(serviceChannel)
    }

    private fun doSomething() {

        preferences = this.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
        httpQueue = Volley.newRequestQueue(this)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        //hrv sensor
//        hrvSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_BEAT)

        startMeasure()

    }

    private fun startMeasure() {
        val sensorRegistered: Boolean = mSensorManager.registerListener(
            this,
            mHeartRateSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        Log.d("Sensor Status:", " Sensor registered: " + (if (sensorRegistered) "yes" else "no"))

        // hrv sensor
//        val sensorRegistered2: Boolean = mSensorManager.registerListener(
//            this,
//            hrvSensor,
//            SensorManager.SENSOR_DELAY_FASTEST
//        )
//        Log.d("Sensor Status:", " Sensor registered: " + (if
//        (sensorRegistered2) "yes" else "no"))


        sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_STARTING)
    }

    private fun stopMeasure() {
        mSensorManager.unregisterListener(this)
        sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_NOT_RUNNING)
    }

     override fun onSensorChanged(event: SensorEvent?) {
         val mHeartRateFloat: Float = event!!.values[0]

         val mHeartRate: Int = mHeartRateFloat.roundToInt()
         val mHRV: Long = calculateRRInterval(event.timestamp)
         Log.d("HR: ", mHeartRate.toString())

         sendHeartRate(mHeartRate,mHRV)
         sendHeartRateToActivity(mHeartRate)
     }

    // override fun onSensorChanged(event: SensorEvent?) {
    //     when (event?.sensor?.type) {
    //         Sensor.TYPE_HEART_RATE -> {
    //             // Handle heart rate sensor data
    //             val heartRateFloat: Float = event.values[0]
    //             val heartRate: Int = heartRateFloat.roundToInt()
    //             Log.d("Heart Rate: ", heartRate.toString())
    //             sendHeartRate(heartRate)
    //             sendHeartRateToActivity(heartRate)
    //         }
    //         Sensor.TYPE_ACCELEROMETER -> {
    //             // Handle accelerometer sensor data
    //             val accelerometerX = event.values[0]
    //             val accelerometerY = event.values[1]
    //             val accelerometerZ = event.values[2]
    //             Log.d("Accelerometer", "X: $accelerometerX, Y: $accelerometerY, Z: $accelerometerZ")
    //             // Handle accelerometer data as needed
    //         }
    //         // Handle data from other sensors as needed
    //     }
    // }

//    override fun onSensorChanged(event: SensorEvent?){
//        val sensorType = event?.sensor?.type
//
//        when(sensorType) {
//            Sensor.TYPE_HEART_RATE, Sensor.TYPE_HEART_BEAT -> {
//                val values = event?.values
//                val mHeartRate: Int = event!!.values[0].roundToInt()
////                val mHRV: Int = event!!.values[1]
//                val mHRV: Int = 3
//                Log.d("HR: ", mHeartRate.toString())
////                Log.d("HRV: ", mHRV.toString())
//                sendHeartRate(mHeartRate, mHRV)
//                sendHeartRateToActivity(mHeartRate)
//            }
//        }
//    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // ignored
    }

    private fun sendHeartRate(heartrate: Int, hrv: Long) {

        val httpUrl = "http://" +
                preferences.getString(
                    MainActivity.Config.CONF_HTTP_HOSTNAME,
                    MainActivity.Config.CONF_HTTP_HOSTNAME_DEFAULT
                ) +
                ":" + preferences.getInt(
            MainActivity.Config.CONF_HTTP_PORT,
            MainActivity.Config.CONF_HTTP_PORT_DEFAULT
        ).toString()

        val httpRequest = object : StringRequest(
            Method.POST,
            httpUrl,
            { response ->
                Log.d("HTTP Reponse: ", response)
                sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_OK)
            },
            {
                Log.e("HTTP Error", it.message.toString())
                sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_ERROR)
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            override fun getBody(): ByteArray {
                return ("rate=$heartrate" + " hrv=$hrv").toByteArray(Charsets.UTF_8)
            }
        }

        httpQueue.add(httpRequest)
    }

    override fun onDestroy() {
        stopMeasure()
        super.onDestroy()
    }

    private fun sendHeartRateToActivity(heartrate: Int) {
        val intent = Intent(MainActivity.Config.CONF_BROADCAST_HEARTRATE_UPDATE)
        intent.putExtra("heartrate", heartrate)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendStatusToActivity(status: String) {
        val intent = Intent(MainActivity.Config.CONF_BROADCAST_STATUS)
        intent.putExtra("status", status)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    private fun calculateRRInterval(currentTimestamp: Long): Long {
        val rrInterval: Long = currentTimestamp - previousTimestamp
        previousTimestamp = currentTimestamp

        return rrInterval
    }
}
