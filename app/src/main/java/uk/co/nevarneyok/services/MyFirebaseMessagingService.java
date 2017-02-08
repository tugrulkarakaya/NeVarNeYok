package uk.co.nevarneyok.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uk.co.nevarneyok.BuildConfig;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.api.EndPoints;
import uk.co.nevarneyok.ux.MainActivity;
import uk.co.nevarneyok.ux.OpenTokVideoActivity;

/**
 * Created by mcagrikarakaya on 29.01.2017.
 * Bildirim geldiğinde telefonda nasıl gözükeceğine dair işlemleri burada yapıyoruz.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final int NOTIFICATION_ID = 6342806;

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Mesaj data içeriği: " + remoteMessage.getData());


        sendNotification(remoteMessage);
    }
    private void sendNotification(RemoteMessage remoteMessage) {
        String Sessionid=remoteMessage.getData().get(EndPoints.NOTIFICATION_SESSION_ID);
        if (Sessionid != null) {
            //Arama geldiğinde yapılacak servis uyandırma işlemleri burada yapılacak
        }
        else{
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                    .setContentTitle(remoteMessage.getData().get(EndPoints.NOTIFICATION_TITLE))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(EndPoints.NOTIFICATION_MESSAGE)))
                    .setContentText(remoteMessage.getData().get(EndPoints.NOTIFICATION_MESSAGE))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            String imageUrl = remoteMessage.getData().get(EndPoints.NOTIFICATION_IMAGE_URL);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Bitmap bitmap = getBitmapFromURL(imageUrl);
                if (bitmap != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Set big icon");
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setBigContentTitle(remoteMessage.getData().get(EndPoints.NOTIFICATION_TITLE))
                            .setSummaryText(remoteMessage.getData().get(EndPoints.NOTIFICATION_MESSAGE)));
                } else {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Cannot download image");
                }
            }
            String link = remoteMessage.getData().get(EndPoints.NOTIFICATION_LINK);

            if (BuildConfig.DEBUG) Log.d(TAG, "Fcm linkType: " + link);
            Intent notificationIntent;
            if (link != null && link.contains("http")) {
                Uri url;
                try {
                    url = Uri.parse(link);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Parsing notification url failed.");
                    return;
                }
                notificationIntent = new Intent(Intent.ACTION_VIEW, url);
            } else {
                notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                notificationIntent.putExtra(EndPoints.NOTIFICATION_LINK, link);
                notificationIntent.putExtra(EndPoints.NOTIFICATION_TITLE, remoteMessage.getData().get(EndPoints.NOTIFICATION_TITLE));
            }

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(contentIntent);
            ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
        }





    }

    private Bitmap getBitmapFromURL(String stringURL) {
        try {
            URL url = new URL(stringURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
