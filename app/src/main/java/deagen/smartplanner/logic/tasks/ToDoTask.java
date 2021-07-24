package deagen.smartplanner.logic.tasks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.time.Duration;

public class ToDoTask {
	// change
	/**
	 * Name/description of task to be done
	 */
	protected String name;
	
	/**
	 * The name of the category of activities this task belongs to.
	 */
	protected String category;
	
	public ToDoTask(String inName) {
		this.setName(inName);
	}
	
	public ToDoTask(String inName, String inCategory) {
		this.setName(inName);
		this.setCategory(inCategory);
	}
	
	public ToDoTask() {
		this.setName("task");
	}

	public ToDoTask(ScheduledToDoTask inTask) {
		this(inTask.getName(), inTask.getCategory());
	}
	
	public ToDoTask(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String inName) {
		name = inName;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String inCategory) {
		category = inCategory;
	}
	
	public String toString() {
		return name + ", " + category;
	}
	
	public void load(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		name = stream.readUTF();
		category = stream.readUTF();
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		stream.writeUTF(name);
		stream.writeUTF(category);
	}

	public static String getTimeText(Duration inDur) {
		Duration time = inDur;
		String timeString = "";
		if(inDur.isNegative()) {
			time = inDur.plusDays(1);
			timeString = "+";
		}
		int hours = (int)time.toHours();
		if(hours > 0) {
			timeString += hours;
			timeString += ":";
		}
		int minutes = (int) time.toMinutes() % 60;
		if(minutes < 10 && timeString.length() > 0) {
			timeString += "0" + minutes;
		} else {
			timeString += minutes;
		}
		int seconds = (int) time.getSeconds() % 60;
		if(seconds < 10) {
			timeString += ":0" + seconds;
		} else {
			timeString += ":" + seconds;
		}
		return timeString;
	}

}
