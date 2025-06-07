package com.example.mit_plateform.Firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

/**
 * Service pour gérer les messages Firebase Cloud Messaging (FCM).
 * Traite les notifications push et les nouveaux tokens FCM.
 */
public class MessagingService extends FirebaseMessagingService {
}
