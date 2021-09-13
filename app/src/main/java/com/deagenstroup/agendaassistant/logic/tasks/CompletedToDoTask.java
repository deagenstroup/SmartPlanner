package com.deagenstroup.agendaassistant.logic.tasks;
import java.time.Duration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CompletedToDoTask extends ToDoTask {

	/**
	 * The amount of time spent on the activity.
	 */
	private Duration timeSpent;
	
	public CompletedToDoTask(ScheduledToDoTask task) {
		this(task.getName(), task.getCategory(), task.getTimeSpent());
	}
	
	public CompletedToDoTask(String inName, Duration inTimeSpent) {
		this.setName(inName);
		this.timeSpent = inTimeSpent;
	}
	
	public CompletedToDoTask(String inName, String category, Duration inTimeSpent) {
		this.setName(inName);
		this.setCategory(category);
		this.timeSpent = inTimeSpent;
	}
	
	public CompletedToDoTask(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}
	
	public Duration getTimeSpent() {
		return timeSpent;
	}

	public String getTimeSpentString() {
		return ToDoTask.getTimeText(this.getTimeSpent());
	}
	
	public String toString() {
		return super.toString() + ", " + timeSpent;
	}
	
	public void load(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		super.load(stream);
		timeSpent = (Duration) stream.readObject();
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		super.save(stream);
		stream.writeObject(timeSpent);
	}
	
}
