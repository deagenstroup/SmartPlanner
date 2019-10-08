package deagen.smartplanner.logic.taskscheduling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.time.Duration;
import java.time.LocalTime;

import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;
import deagen.smartplanner.service.TaskManagerService;

/**
 * Actively manages a ToDoList in real time. TaskManager can be put into an active mode where it periodically
 * increases the amount of time spent on the current task and moves onto the next task if
 * the full time has been spent. TaskManager can also extend and cut short the time allocated for
 * the current task. TaskManager is designed to provide real-time accountability and tracking
 * to the user for the ToDoList they have created seperate of the user interface.
 * @author Deagen Stroup
 */
public class TaskManager {

	/**
	 * The Service which is used to run TaskManager's functionality in the background.
	 */
	private TaskManagerService service;

	/**
	 * The ToDoList which is actively being completed by the user.
	 */
	private ToDoList list;
	
	/**
	 * The time which the current activity is projected to end if the
	 * module is in active mode;
	 */
	private LocalTime currentActivityEnd;
	
	/**
	 * If true, then the tasks of ToDoList are actively being completed
	 * by the user and the TaskManager is actively keeping track of the time spent 
	 * on the current activity.
	 */
	private boolean active;
	
	public TaskManager() {
		list = null;
	}
	
	public TaskManager(ToDoList inList) {
		this.setToDoList(inList);
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * @return The task which is currently being worked on by the user.
	 */
	public ScheduledToDoTask getCurrentTask() {
		return list.getCurrentTask();
	}

	/**
	 * @return The time at which the current activity was started.
	 */
	public LocalTime getCurrentActivityEnd() {
		return currentActivityEnd;
	}

	public void setToDoList(ToDoList inList) {
		list = inList;
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

	/**
	 * Increases the amount of time spent on the current activity by the provided amount. Designed
	 * to be called periodically on every iteration of the TaskManagers main thread.
	 * @param timeSpent Amount of time to spend on the current task.
	 */
	public void spendTimeOnCurrentTask(Duration timeSpent) {
		this.getCurrentTask().spendTime(timeSpent);
		service.updateCurrentTask();
		// update Service Interface
		// update FragmentInterface if nessesarry
		if(this.getCurrentTask().isFinished()) {
			service.preEndTaskUpdate();
			list.finishCurrentTask();
			service.postEndTaskUpdate();
			if(list.getNumberOfScheduledTasks() == 0 && this.active == true) {
				this.stopTasks();
			}
		}
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
	
	/**
	 * Puts the planner into an active state, in which the current task being executed. And the
	 * current ToDoList is actively being managed.
	 */
	public void startTasks(DailyPlannerFragment fragment) {
		active = true;
//		userInterface = fragment;
		currentActivityEnd = LocalTime.now().plus(this.getCurrentTask().getTimeRemaining());
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

			}
		};
		context.bindService(intent, connection, 0);
	}

	public void stopTasks() {
		active = false;
	}

	// stub code to be removed later, used for text-based implementation that is to be removed
	public void startTasks() {

	}

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
