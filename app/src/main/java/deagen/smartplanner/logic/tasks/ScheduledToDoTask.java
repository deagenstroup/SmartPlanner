package deagen.smartplanner.logic.tasks;

import java.time.Duration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import deagen.smartplanner.logic.tasks.CompletedToDoTask;
import deagen.smartplanner.logic.tasks.ToDoTask;

public class ScheduledToDoTask extends ToDoTask {
	
	/**
	 * The amount of time allocated for the task, if any at all. A null value indicates that the
	 * task does not have a specified time limit.
	 */
	private Duration allocatedTime;
	
	/**
	 * The time spent so far on the task.
	 */
	private Duration timeSpent;

	public ScheduledToDoTask() {
		this("*task*", "*category*", Duration.ofHours(1L));
	}

	public ScheduledToDoTask(String inName, String category, Duration inAllocatedTime) {
		super(inName, category);
		this.setAllocatedTime(inAllocatedTime);
		timeSpent = Duration.ofMinutes(0);
	}

	/**
	 * Creates a ScheduledToDoTask from a regular ToDoTask
	 * @param inTask ToDoTask to be converted
	 * @param inDur Duration of time that the task is scheduled for
	 */
	public ScheduledToDoTask(ToDoTask inTask, Duration inDur) {
		this(inTask.getName(), inTask.getCategory(), inDur);
	}
	
	public ScheduledToDoTask(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}

	/**
	 * @return The amount of time remaining for this task if there is a specified time limit. Null
	 * if there is no specified time limit.
	 */
	public Duration getTimeRemaining() {
		if(allocatedTime == null)
			return null;
		return allocatedTime.minus(timeSpent);
	}

	public String getTimeSpentString() {
		return ToDoTask.getTimeText(this.getTimeSpent());
	}

	public String getTimeRemainingString() {
		if(allocatedTime == null)
			return null;
		return ToDoTask.getTimeText(this.getTimeRemaining());
	}

	public Duration getTimeSpent() {
		return timeSpent;
	}

	/**
	 * @return True if this task does not have a specified timeframe of completion.
	 */
	public boolean isUntimedTask() {
		return this.getTimeRemaining() == null;
	}
	
	public void setAllocatedTime(Duration inTime) {
		allocatedTime = inTime;
	}

	public void allocateMoreTime(Duration inTime) {
		allocatedTime = timeSpent.plus(inTime);
	}

	/**
	 * @return True if the specified time limit has been reached for a task, ie. the time spent
	 * on the task has reached the time allocated for it, false otherwise
	 */
	public boolean isFinished() {
		if(allocatedTime == null) {
			return false;
		}
		return allocatedTime.compareTo(timeSpent) <= 0;
	}
	
	/**
	 * Mark the task as completed
	 */
	public CompletedToDoTask finish() {
		return new CompletedToDoTask(this);
	}
	
	public void spendTime(Duration time) {
		this.timeSpent = this.timeSpent.plus(time);
	}
	
	public void extendTime(Duration timeTaken) {
		allocatedTime = allocatedTime.plus(timeTaken);
	}
	
	public void cutShortTime(Duration timeCut) {
		allocatedTime = allocatedTime.minus(timeCut);
	}
	
	public String toString() {
		return super.toString() + ", " + timeSpent + " of " + allocatedTime;
	}
	
	public void load(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		super.load(stream);
		allocatedTime = (Duration) stream.readObject();
		timeSpent = (Duration) stream.readObject();
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		super.save(stream);
		stream.writeObject(allocatedTime);
		stream.writeObject(timeSpent);
	}
	
}
