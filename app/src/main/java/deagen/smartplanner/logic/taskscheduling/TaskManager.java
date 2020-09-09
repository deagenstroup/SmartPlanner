package deagen.smartplanner.logic.taskscheduling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.time.Duration;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import deagen.smartplanner.MainActivity;
import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;
import deagen.smartplanner.service.TaskManagerService;


/**
 * Handles logic of actively managing tasks. TaskManager can be put into an active mode where it periodically
 * increases the amount of time spent on the current task and pauses if the full time has been
 * spent to wait for user input. TaskManager can also extend and cut short the time allocated for
 * the current task. TaskManager is designed to provide real-time accountability and tracking
 * to the user for the ToDoList they have created separate of the user interface.
 * @author Deagen Stroup
 */
public class TaskManager {

	private static final boolean OLD_LOAD_FLAG = false;

	/**
	 * The Service which is used to run TaskManager's functionality in the background.
	 */
	private TaskManagerService service;

	/**
	 * The ToDoList which is actively being completed by the user.
	 */
	private ToDoList list;
	
//	/**
//	 * The time at which the current activity is projected to end if the
//	 * module is in active mode;
//	 */
//	private LocalTime currentActivityEnd;
	
	/**
	 * If true, then the tasks of ToDoList are actively being completed
	 * by the user and the TaskManager is actively keeping track of the time spent 
	 * on the current activity.
	 */
	private boolean active;

	/**
	 * The time since the current task was made active or amount of time since the current task was
	 * last logged, in milliseconds from epoch.
	 */
	private long startTimeMilli = 0L;

	private DailyPlannerFragment fragment;
	
//	public TaskManager() {
//		list = null;
//	}
	
	public TaskManager(ToDoList inList) {
		this.setToDoList(inList);
	}

	public TaskManager(ToDoList inList, ObjectInputStream stream) {
		this(inList);
		if(!OLD_LOAD_FLAG)
			this.load(stream);
	}

	public void setDailyPlannerFragment(DailyPlannerFragment inFragment) {
		fragment = inFragment;
	}

	public boolean isActive() {
		return active;
	}

	public long getStartTime() {
		return startTimeMilli;
	}

	/**
	 * @return The task which is currently being worked on by the user.
	 */
	public ScheduledToDoTask getCurrentTask() {
		if(list == null)
			return null;
		return list.getCurrentTask();
	}

//	/**
//	 * @return The time at which the current activity was started.
//	 */
//	public LocalTime getCurrentActivityEnd() {
//		return currentActivityEnd;
//	}

	public void setToDoList(ToDoList inList) {
		if(!active)
			list = inList;
	}



	/**
	 * Calculates the amount of time that has passed since the TaskManager was put into active mode
	 * and saves it into the current task object.
	 */
	public void logTimeOnCurrentTask() {
		long timeDifference = System.currentTimeMillis() - startTimeMilli;
		startTimeMilli = System.currentTimeMillis();
		this.spendTimeOnCurrentTask(Duration.ofMillis(timeDifference));
	}

	/**
	 * Increases the amount of time spent on the current activity by the provided amount. Designed
	 * to be called periodically on every iteration of the TaskManagers main thread.
	 * @param timeSpent Amount of time to spend on the current task.
	 */
	public void spendTimeOnCurrentTask(Duration timeSpent) {
		this.getCurrentTask().spendTime(timeSpent);

		// if the current task is finished,
		if(this.getCurrentTask().isFinished()) {
			// stop the task manager
			this.stopTasks();

			// notify service that task has finished
			service.currentTaskFinish();
		}
	}

	public void finishTask() {
		list.finishCurrentTask();
	}
	
	/**
	 * Puts the planner into an active state, in which the current task is being executed. And the
	 * current ToDoList is actively being managed.
	 * @return True if it was successful, false otherwise
	 */
	public boolean startTasks(DailyPlannerFragment inFragment) {
		ScheduledToDoTask task = this.getCurrentTask();
		if(task == null) {
			Log.d("ERROR", "current task is null");
			return false;
		}
		active = true;
		startTimeMilli = System.currentTimeMillis();
		fragment = inFragment;
//		currentActivityEnd = LocalTime.now().plus(this.getCurrentTask().getTimeRemaining());

		startTaskManagerService();

		return true;
	}

	public void stopTasks() {
		Context context = fragment.getContext();
		context.stopService(new Intent(context, TaskManagerService.class));
		Log.d("DEBUG", "TaskManager service has stopped");
		active = false;
		fragment = null;
		this.saveToFile();
	}

	/**
	 * Similar to startTasks method, except this is called when it is assumed that the TaskManager
	 * has been built from a save file that did not properly exit and has unaccounted time.
	 * @param inFragment The DailyPlannerFragment which called this method.
	 * @return True if completed successfully, false otherwise.
	 */
	public boolean restartTasks(DailyPlannerFragment inFragment) {
		ScheduledToDoTask task = this.getCurrentTask();
		if(task == null) {
			Log.d("ERROR", "current task is null");
			return false;
		}
		fragment = inFragment;

		startTaskManagerService();

		return true;
	}

	private void startTaskManagerService() {
		// starting the background service to notify the user
		Context context = fragment.getContext();
		Intent intent = new Intent(context, TaskManagerService.class);
		context.startService(new Intent(context, TaskManagerService.class));
		ServiceConnection connection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className, IBinder binder) {
				TaskManagerService.TaskManagerServiceBinder serviceBinder = (TaskManagerService.TaskManagerServiceBinder) binder;
				service = serviceBinder.getService();
				service.setTaskManager(TaskManager.this);
			}
			@Override
			public void onServiceDisconnected(ComponentName className) {
//				Log.d("DEBUG", "TaskManager service has been disconnected.");
			}
		};
		context.bindService(intent, connection, 0);
	}

	/**
	 * Analyzes the startTime and activity status just after being loaded from file to determine
	 * if the TaskManager or TaskManager service had been stopped in the middle of active status.
	 * @return True if the TaskManager or TaskManager service had been stopped abruptly, false otherwise.
	 */
	public boolean checkForAbruptExit() {
		if(active && startTimeMilli < System.currentTimeMillis()) {
			long currentTime = System.currentTimeMillis();
			if (this.isActive() && (currentTime - this.getStartTime()) <= Duration.ofHours(3L).toMillis()) {
				return true;
			}
		}
		return false;
	}

	public void stopService() {
		service.setStopFlag(true);
	}

	public void saveToFile() {
		if (fragment != null) {
			MainActivity mainActivity = (MainActivity) fragment.getActivity();
			mainActivity.saveToFile();
		}
	}

	public void save(ObjectOutputStream stream) {
		try {
			stream.writeBoolean(active);
			stream.writeLong(startTimeMilli);
			Log.d("Debug", "TaskManager saved successfully.");
		} catch(java.io.IOException exception) {
			Log.e("TaskManager", "IOException: Error writing TaskManager to file.");
		}
	}

	public void load(ObjectInputStream stream) {
		try {
			active = stream.readBoolean();
			startTimeMilli = stream.readLong();
			Log.d("Debug", "TaskManager loaded successfully.");
		} catch(java.io.IOException exception) {
			Log.e("TaskManager", "IOException: Error reading TaskManager from file.");
		}
	}

	/**
	 * Extends the amount of time allocated for the task currently being completed
	 * @param timeExtension The amount of time by which the current task is to be extended
	 */
	public void extendCurrentActivity(Duration timeExtension) {
		this.getCurrentTask().extendTime(timeExtension);
	}

	/**
	 * Cuts short the amount of time allocated for the task currently being completed
	 * @param timeCut The amount of time by which the current task is to be cut short
	 */
	public void cutShortCurrentActivity(Duration timeCut) {
		this.getCurrentTask().cutShortTime(timeCut);
	}

	//	public void checkCurrentTask() {
//		if(this.getCurrentTask().isFinished()) {
//			userInterface.preEndTaskUpdate();
//			list.finishCurrentTask();
//			userInterface.postEndTaskUpdate();
//			if(list.getNumberOfScheduledTasks() == 0 && this.active == true) {
//				this.active = false;
//			}
//		}
//	}

//	// Obsulete code which uses AsyncTask implementation
//	// new implementation uses background service
//	private class TaskRunner extends AsyncTask<String, String, String> {
//		@Override
//		protected String doInBackground(String... params) {
//			do {
//				try {
//					Thread.sleep(1000);
//					spendTimeOnCurrentTask(Duration.ofSeconds(1L));
//					if(active)
//						publishProgress();
//				} catch(InterruptedException e) {
//					System.out.println(e.getStackTrace());
//				}
//			} while(active);
//			return "null";
//		}
//
//		@Override
//		protected void onProgressUpdate(String... text) {
//			userInterface.updateCurrentTask();
//		}
//	}
}
