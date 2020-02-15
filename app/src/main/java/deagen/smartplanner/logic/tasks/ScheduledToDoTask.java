package deagen.smartplanner.logic.tasks;

import java.time.Duration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import deagen.smartplanner.logic.tasks.CompletedToDoTask;
import deagen.smartplanner.logic.tasks.ToDoTask;

public class ScheduledToDoTask extends ToDoTask {
	
	/**
	 * The amount of time allocated for the task.
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
	
	public Duration getTimeRemaining() {
		return allocatedTime.minus(timeSpent);
	}

	public String getTimeRemainingString() {
		return ToDoTask.getTimeText(this.getTimeRemaining());
	}

	public Duration getTimeSpent() {
		return timeSpent;
	}
	
	public void setAllocatedTime(Duration inTime) {
		allocatedTime = inTime;
	}

	public void allocateMoreTime(Duration inTime) {
		allocatedTime = timeSpent.plus(inTime);
	}
	
	public boolean isFinished() {
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
