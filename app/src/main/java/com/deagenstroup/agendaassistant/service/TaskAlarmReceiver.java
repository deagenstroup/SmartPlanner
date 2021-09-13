package com.deagenstroup.agendaassistant.service;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * Unused, possibly use for end of task notification when putting task to sleep if that feature
 * is ever implemented.
 */
public class TaskAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Testing", "Alarm broadcast has been received by TaskAlarmReceiver.");
        this.sendRestartNotification(context);
    }

    public void sendRestartNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

        String notificationText = "Background service for AgendaAssistant running: " + this.isMyServiceRunning(context, TaskManagerService.class);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
        notificationBuilder.setContentText(notificationText);
        notificationBuilder.setSound(null);

//        Intent activityIntent = new Intent(this, MainActivity.class);
//        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        TaskStackBuilder builder = TaskStackBuilder.create(this);
//        builder.addNextIntentWithParentStack(activityIntent);
//        notificationBuilder.setContentIntent(builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));

        Intent activityIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        activityIntent.setPackage(null);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, activityIntent, 0));

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String notificationChannelId = "planner1";
            NotificationChannel channel = new NotificationChannel(notificationChannelId,
                    "Planner notification channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(notificationChannelId);

//            this.notificationSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.getApplicationContext().getPackageName() + "/" + R.raw.positive_notification);
//            audioChannelId = "planner_audio_2";
//            NotificationChannel audioChannel = new NotificationChannel(audioChannelId,
//                    "Planner audio alert channel", NotificationManager.IMPORTANCE_DEFAULT);
//            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .setUsage(AudioAttributes.USAGE_ALARM)
//                    .build();
//            audioChannel.enableLights(true);
//            audioChannel.setSound(notificationSound,
//                    audioAttributes);
//            notificationManager.createNotificationChannel(audioChannel);
        }

        notificationManager.notify(4, notificationBuilder.build());
    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
