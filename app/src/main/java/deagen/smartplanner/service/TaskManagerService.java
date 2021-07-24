package deagen.smartplanner.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import java.time.Duration;

import deagen.smartplanner.MainActivity;
import deagen.smartplanner.R;
import deagen.smartplanner.logic.DailyPlannerUserInterface;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;
import deagen.smartplanner.logic.taskscheduling.TaskManager;

/**
 * A service on which the TaskManager object can perform its duties. Service provides a thread
 * on which TaskManager can run, as well as providing a notification interface for TaskManager
 * to provide information to the user. TaskManagerService is designed to be the background
 * running aspect of the application, which provides real-time tracking and accountability
 * for the user's current ToDoList.
 */
public class TaskManagerService extends IntentService implements DailyPlannerUserInterface {

    /**
     * Fixed string values which specify messages to be spent between the service and GUI instances
     * of the application.
     */
    public static final String UPDATE_UI = "TaskManagerService.UPDATE_UI",
                               UPDATE = "TaskManagerService.UPDATE",
                               PRE_UPDATE = "TaskManagerService.PRE_UPDATE",
                               POST_UPDATE = "TaskManagerService.POST_UPDATE",
                               END_TASK = "TaskMangerService.END_TASK";

    public Uri notificationSound;

    private TaskManager taskManager;

    /**
     * Builder for the notification which displays the status of the current task.
     */
    private NotificationCompat.Builder notificationBuilder;

    /**
     * Manager which initially displays and updates the notification which displays the status of
     * the current task and task completed notifications.
     */
    private NotificationManager notificationManager;

    /**
     * Sends broadcast messages which are used to inform the DailyPlannerFragment to update the UI.
     */
    private LocalBroadcastManager broadcaster;

    private String notificationChannelId, audioChannelId;

    private boolean stopFlag = false;

    private final IBinder binder = new TaskManagerServiceBinder();



    /**
     * A binder class which allows components to bind to this service and call methods upon it.
     */
    public class TaskManagerServiceBinder extends Binder {
        public TaskManagerService getService() {
            return TaskManagerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public TaskManagerService() {
        super("TaskManagerService");
        taskManager = null;
    }

    /**
     * Called when the service is initialized.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Called when the startService method is used to start the actual service after it has been
     * created.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopFlag = false;
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * The main loop that handles the logic of the service.
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null) {
            // wait for taskManager to be set
            while(taskManager == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // starting the active mode of the task manager
            this.createCurrentTaskNotification();
            long saveTime = System.currentTimeMillis();
            while(taskManager.isActive()) {
                taskManager.logTimeOnCurrentTask();

                // save the TaskManager to file every 60 seconds
                if((System.currentTimeMillis() - saveTime) >= Duration.ofMinutes(1L).toMillis()) {
                    taskManager.saveToFile();
                    saveTime = System.currentTimeMillis();
                }



                this.updateCurrentTask();

                if(stopFlag) {
                    taskManager.stopTasks();
                    this.stopSelf();
                }
//                ScheduledToDoTask task = taskManager.getCurrentTask();
//                Log.d("TESTING", "Current Task: " + task.getName() + " time: " + task.getTimeRemainingString());
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void setTaskManager(TaskManager inManager) {
        this.taskManager = inManager;
    }

    /**
     * Creates the notification which informs the user of the task being worked on and the time
     * spent on that task.
     */
    public void createCurrentTaskNotification() {
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);

        ScheduledToDoTask task = taskManager.getCurrentTask();
        String notificationText = task.getName() + " - ";
        if(task.isUntimedTask()) {
            notificationText = notificationText + task.getTimeSpentString();
        } else {
            notificationText = notificationText + task.getTimeRemainingString();
        }
        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
        notificationBuilder.setContentText(notificationText);
        notificationBuilder.setSound(null);

//        Intent activityIntent = new Intent(this, MainActivity.class);
//        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        TaskStackBuilder builder = TaskStackBuilder.create(this);
//        builder.addNextIntentWithParentStack(activityIntent);
//        notificationBuilder.setContentIntent(builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));

        Intent activityIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        activityIntent.setPackage(null);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, activityIntent, 0));

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannelId = "planner1";
            NotificationChannel channel = new NotificationChannel(notificationChannelId,
                    "Planner notification channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(notificationChannelId);

            this.notificationSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.getApplicationContext().getPackageName() + "/" + R.raw.positive_notification);
            audioChannelId = "planner_audio_2";
            NotificationChannel audioChannel = new NotificationChannel(audioChannelId,
                    "Planner audio alert channel", NotificationManager.IMPORTANCE_DEFAULT);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            audioChannel.enableLights(true);
            audioChannel.setSound(notificationSound,
                    audioAttributes);
            notificationManager.createNotificationChannel(audioChannel);
        }

        notificationManager.notify(1, notificationBuilder.build());
    }

    /**
     * Sends messages to the DailyPlannerFragment object telling it to update the UI.
     * @param message
     */
    public void sendUpdateMessage(String message) {
        Intent intent = new Intent(UPDATE_UI);
        intent.putExtra(UPDATE_UI, message);
        broadcaster.sendBroadcast(intent);
    }

    public void setStopFlag(boolean flag) {
        this.stopFlag = flag;
    }

    /**
     * Updates the current task notification and relevent DailyPlannerFragment GUI objects to
     * reflect the task that is currently being worked on.
     */
    public void updateCurrentTask() {
        // getting the current task and sending the text to the current task notification
        ScheduledToDoTask task = taskManager.getCurrentTask();
        String notificationText = task.getName() + " - ";
        if(task.isUntimedTask()) {
            notificationText = notificationText + task.getTimeSpentString();
        } else {
            notificationText = notificationText + task.getTimeRemainingString();
        }
        notificationBuilder.setContentText(notificationText);
        notificationBuilder.setSound(null);
        notificationManager.notify(1, notificationBuilder.build());
        this.sendUpdateMessage(UPDATE);
    }

    /**
     * Sends broadcast back to DailyPlannerFragment indicating that decision needs to be made to
     * finish the current task.
     */
    public void currentTaskFinish() {
        this.preEndTaskUpdate();
        this.sendUpdateMessage(END_TASK);
    }

    public void preEndTaskUpdate() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        String taskName = taskManager.getCurrentTask().getName();
        builder.setSmallIcon(android.R.drawable.checkbox_on_background);
        builder.setContentTitle("Current task completed");
        builder.setContentText(taskName);
        builder.setLights(Color.GREEN, 500, 500);
        builder.setSound(notificationSound);

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(audioChannelId);
        }

        notificationManager.notify(0, builder.build());

        this.sendUpdateMessage(PRE_UPDATE);
    }

    public void postEndTaskUpdate() {
        this.sendUpdateMessage(POST_UPDATE);
    }

    public void onDestroy() {
    }
}
