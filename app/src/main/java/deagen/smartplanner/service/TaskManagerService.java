package deagen.smartplanner.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.time.Duration;

import deagen.smartplanner.MainActivity;
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

    public static final String UPDATE_UI = "TaskManagerService.UPDATE_UI",
                               UPDATE = "TaskManagerService.UPDATE",
                               PRE_UPDATE = "TaskManagerService.PRE_UPDATE",
                               POST_UPDATE = "TaskManagerService.POST_UPDATE",
                               END_TASK = "TaskMangerService.END_TASK";


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

    private String notificationChannelId;

    private TaskManager taskManager;

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

    public void setTaskManager(TaskManager inManager) {
        this.taskManager = inManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null) {
            // wait for taskManager to be set
            while(taskManager == null) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // starting the active mode of the task manager
            this.createCurrentTaskNotification();
            do {
                taskManager.spendTimeOnCurrentTask(Duration.ofSeconds(1));
                this.updateCurrentTask();
//                ScheduledToDoTask task = taskManager.getCurrentTask();
//                Log.d("TESTING", "Current Task: " + task.getName() + " time: " + task.getTimeRemainingString());
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            } while(taskManager.isActive());
        }
    }

    /**
     * Creates the notification which informs the user of the task being worked on and the time
     * spent on that task.
     */
    public void createCurrentTaskNotification() {
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);

        ScheduledToDoTask task = taskManager.getCurrentTask();
        String notificationText = task.getName() + " - " + task.getTimeRemainingString();
        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
        notificationBuilder.setContentTitle("Active Task");
        notificationBuilder.setContentText(notificationText);

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
            notificationChannelId = "planner";
            NotificationChannel channel = new NotificationChannel(notificationChannelId,
                    "Planner notification channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(notificationChannelId);
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

    /**
     * Updates the current task notification and relevent DailyPlannerFragment GUI objects to
     * reflect the task that is currently being worked on.
     */
    public void updateCurrentTask() {
        // getting the current task and sending the text to the current task notification
        ScheduledToDoTask task = taskManager.getCurrentTask();
        String notificationText = task.getName() + " - " + task.getTimeRemainingString();
        notificationBuilder.setContentText(notificationText);
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

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(notificationChannelId);
        }

        notificationManager.notify(0, builder.build());

        this.sendUpdateMessage(PRE_UPDATE);
    }

    public void postEndTaskUpdate() {
        this.sendUpdateMessage(POST_UPDATE);
    }
}
