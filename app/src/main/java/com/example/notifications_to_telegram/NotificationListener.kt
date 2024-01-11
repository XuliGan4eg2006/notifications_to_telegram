package com.example.notifications_to_telegram

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

fun sendToTelegram(text: String, telegramToken: String, id: String){
    try{
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaTypeOrNull()
        val body = "{\"chat_id\": \"${id}\", \"text\": \"${text}\"}".toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.telegram.org/bot${telegramToken}/sendMessage")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful){
                println("response good")
            }
            else{
                println(response.code)
            }
            println(response)
        }

    }
    catch (e: IOException){
        println(e)
    }
}


class TelegramNotificationListenerService : NotificationListenerService() {

    lateinit var intentT: Intent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intentT = intent!!

        if (intent.action != null) {
            if (intent.action == "STOP_SERVICE"){
                val notificationManager = NotificationManagerCompat.from(this)
                notificationManager.cancel(1)
                println("Stopping by: ${intent.action}")
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
        val app_name = sbn.packageName.toString()
        // Обработка входящего уведомления и отправка в телеграм бота
        println("NEW!")
        Thread{
            sendToTelegram("Приложение: $app_name \nЗаголовок: $title \nУведомление: $text", intentT?.getStringExtra("token").toString(), intentT?.getStringExtra("id").toString())
        }.start()
    }
}
