package com.labs.tatu.kibanda.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labs.tatu.kibanda.OrderStatus;
import com.labs.tatu.kibanda.R;
import com.labs.tatu.kibanda.common.Common;
import com.labs.tatu.kibanda.model.Request;

import java.util.Random;

public class ListenOrder extends Service implements ChildEventListener {

    DatabaseReference mDatabase;

    public ListenOrder() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = FirebaseDatabase.getInstance().getReference("Requests");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDatabase.addChildEventListener(this);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (Common.currentUser.getName().equals("admin")) {
            //Trigger Here
            Request request = dataSnapshot.getValue(Request.class);
            if (request.getStatus().equals("0")) {
                showOrderNotification(dataSnapshot.getKey(), request);
            }
        }

    }

    private void showOrderNotification(String key, Request request) {
        Intent intent = new Intent(getBaseContext(), OrderStatus.class);

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Kibanda Admin")
                .setContentInfo("New Order")
                .setContentIntent(contentIntent)
                .setContentText("You have a new order #" + key)
                .setSmallIcon(R.mipmap.ic_launcher);
        int randomInt = new Random().nextInt(9999 - 1) + 1;

        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(randomInt, builder.build());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        if (!Common.currentUser.getName().equals("admin")) {
            //Trigger Here
            Request request = dataSnapshot.getValue(Request.class);
            showNotification(dataSnapshot.getKey(), request);
        }

    }

    private void showNotification(String key, Request request) {
        Intent intent = new Intent(getBaseContext(), OrderStatus.class);
        intent.putExtra("userPhone", request.getPhone());
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentInfo("Your order was updated!")
                .setContentText("Order #" + key + " was updated to " + Common.convertCodeToStatus(request.getStatus()))
                .setContentIntent(contentIntent)
                .setContentInfo("Info")
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
