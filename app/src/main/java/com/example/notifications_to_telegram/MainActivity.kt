package com.example.notifications_to_telegram

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat.STOP_FOREGROUND_DETACH
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.notifications_to_telegram.ui.theme.Notifications_to_telegramTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            checkNotificationPermission(LocalContext.current)
            Greeting(LocalContext.current)
        }
    }
}

fun checkNotificationPermission(context: Context){
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 12)
        } //12 - любое число
    }
    else{
        println("Have Permission")
    }
}

fun createNotification(context: Context) {
    val channelId = "default_channel"
    val notificationId = 1
    val notificationManager = NotificationManagerCompat.from(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Default Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = "Default Channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Title")
        .setContentText("Notification text")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .addAction(R.drawable.ic_launcher_background, "Завершить прослушивание", getClosePendingIntent(context))
        .build()
    notificationManager.notify(notificationId, notificationBuilder)
}

fun getClosePendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, TelegramNotificationListenerService::class.java).apply {
        action = "STOP_SERVICE"
    }
    return  PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(context: Context) {
    val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    var intent = Intent(context, TelegramNotificationListenerService::class.java)
    var botToken by remember { mutableStateOf("") }
    var userid by remember { mutableStateOf("") }


    Box(modifier = Modifier.fillMaxSize()){
        OutlinedTextField(
            value = botToken,
            modifier = Modifier
                .height(65.dp)
                .width(350.dp)
                .align(Alignment.Center),
            onValueChange = { botToken = it },
            label = { Text(text = "Токен",
                fontSize = 20.sp,
                modifier = Modifier) }
        )
        OutlinedTextField(
            value = userid,
            modifier = Modifier
                .height(195.dp)
                .width(350.dp)
                .padding(top = 130.dp)
                .align(Alignment.Center),
            onValueChange = { userid = it },
            label = { Text(text = "Telegram Id",
                fontSize = 20.sp,
                modifier = Modifier) }
        )
        Button(onClick = {
            if (manager.isNotificationListenerAccessGranted(ComponentName(context, TelegramNotificationListenerService::class.java))) {
                if (botToken != "" && userid != "") {
                    intent.putExtra("token", botToken)
                    intent.putExtra("id", userid)
                    context.startForegroundService(intent)
                    createNotification(context)
                    Toast.makeText(context, "Служба запущена", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                }
            } else {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }, modifier = Modifier
            .align(Alignment.Center)
            .padding(top = 250.dp)) {
            Text(text = "Запустить сервис")
        }
//        Button(onClick = {
//            println("Остановлено")
//        }, modifier = Modifier.align(Alignment.Center).padding(top = 130.dp)) {
//            Text(text = "Остановить службу")
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting(LocalContext.current)
}