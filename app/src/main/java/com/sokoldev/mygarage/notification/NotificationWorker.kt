package com.sokoldev.mygarage.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sokoldev.mygarage.R
import com.sokoldev.mygarage.vehicle.data.Car
import java.text.SimpleDateFormat
import java.util.*


class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"
    override fun doWork(): Result {
        // Retrieve the date from Firebase
        getCarList()
        return Result.success()
    }

    fun getCarList() {


        var db = FirebaseFirestore.getInstance()
        var productsRef = db.collection("cars")
        val query: Query =
            productsRef.whereEqualTo(
                "owner",
                FirebaseAuth.getInstance().currentUser?.uid.toString()
            )
        query.addSnapshotListener(EventListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@EventListener
            }
            for (document in snapshot!!.documents) {
                val car = document.toObject(Car::class.java)
                if (car != null) {
                    if (car.lastOilChange != "") {
                        checkDate(car.lastOilChange)
                    }
                }
            }
        })
    }


    fun checkDate(lastOilChange: String?) {
        val dateString = lastOilChange
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = format.parse(dateString)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val newDate = calendar.time
        // Calculate the time difference
        val currentDate = Date()
        val timeDiff = newDate.time - currentDate.time
        // Schedule the notification if the time difference is less than 24 hours
        if (timeDiff < 86400000) { // 24 hours in milliseconds
            Log.d(TAG, timeDiff.toString())
            val notificationTitle = "Reminder"
            val notificationBody = "Your Oil Change Date is in less than 24 hours!"
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            addNotification(notificationTitle, notificationBody, notificationManager)
        }
    }

    private fun addNotification(s: String, s1: String, notificationManager: NotificationManager) {

        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(applicationContext, channelId)
                .setContentTitle(s)
                .setContentText(s1)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.drawable.ic_launcher_background
                    )
                )
        } else {
            builder = Notification.Builder(applicationContext)
                .setContentTitle(s)
                .setContentText(s1)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.drawable.ic_launcher_background
                    )
                )
        }

        notificationManager.notify(1234, builder.build())
    }

    companion object {
        private const val TAG = "NotificationWorker"

    }
}
