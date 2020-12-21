package com.example.amvchat.Notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.amvchat.MassageChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService(){


    override fun onMessageReceived(mRemoteMassage: RemoteMessage) {
        super.onMessageReceived(mRemoteMassage)

        val sented = mRemoteMassage.data["sented"]

        val user = mRemoteMassage.data["user"]

        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        val currentOnlineUser = sharedPref.getString("currentUser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && sented == firebaseUser.uid){

            if (currentOnlineUser != user){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                    sendOreoNotification(mRemoteMassage)
                }
                else{

                    sendNotification(mRemoteMassage)
                }
            }
        }
    }


    private fun sendNotification(mRemoteMassage: RemoteMessage) {

        val user = mRemoteMassage.data["user"]
        val icon = mRemoteMassage.data["icon"]
        val title = mRemoteMassage.data["title"]
        val body = mRemoteMassage.data["body"]

        val notification = mRemoteMassage.notification
        val j = user?.replace("[\\D]".toRegex(),"")?.toInt()
        val intent =Intent(this, MassageChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userId", user)
        bundle.putString("icon", icon)
        bundle.putString("title", title)
        bundle.putString("body", body)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = j?.let {
            PendingIntent.getActivity(this,
                it, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        val defoultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defoultSound)
            .setContentIntent(pendingIntent)

        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if (j != null) {
            if (j>0){

                i = j
            }
        }

        noti.notify(i,builder.build())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendOreoNotification(mRemoteMassage: RemoteMessage) {

        val user = mRemoteMassage.data["user"]
        val icon = mRemoteMassage.data["icon"]
        val title = mRemoteMassage.data["title"]
        val body = mRemoteMassage.data["body"]

        val notification = mRemoteMassage.notification
        val j = user?.replace("[\\D]".toRegex(),"")?.toInt()
        val intent =Intent(this, MassageChatActivity::class.java)


        val bundle = Bundle()
        bundle.putString("userId", user)
        bundle.putString("icon", icon)
        bundle.putString("title", title)
        bundle.putString("body", body)

        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


        val pendingIntent =
            j?.toInt()?.let {
                PendingIntent.getActivity(this,
                    it, intent, PendingIntent.FLAG_ONE_SHOT)
            }


        val defoultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotifications(this)

        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defoultSound, icon)

        var i = 0
        if (j != null) {
            if (j>0){

                i = j
            }
        }

        oreoNotification.getManager?.notify(i,builder.build())
    }
}