package deagen.smartplanner.logic.taskplanning;

import java.util.ArrayList;
import java.io.*;

import deagen.smartplanner.logic.tasks.ToDoTask;

public class ActivityCategory {

	private String name;
	
	/**
	 * The tasks that are within this category.
	 */
	private ArrayList<ToDoTask> tasks;
	
	public ActivityCategory(String inName) {
		name = inName;
		tasks = new ArrayList<>();
	}
	
	public ActivityCategory(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}
	
	public String getName() {
		return name;
	}
	
	public ToDoTask getTask(int position) {
		return tasks.get(position);
	}

	public ToDoTask getTask(String name) {
		for(ToDoTask task : tasks) {
			if(task.getName().equals(name))
				return task;
		}
		return null;
	}

	public int getNumberOfTasks() {
		return tasks.size();
	}
	
	public void addToDoTask(int position, ToDoTask task) {
		task.setCategory(name);
		tasks.add(position, task);
	}
	
	public void addToDoTask(ToDoTask task) {
		if(this.getTask(task.getName()) != null)
			return;
		task.setCategory(name);
		tasks.add(task);
	}

	public void moveTask(int from, int to) {
		ToDoTask task = tasks.remove(from);
		tasks.add(to, task);
	}

	public ToDoTask removeTask(int taskPosition) {
		return tasks.remove(taskPosition);
	}

	public void removeTask(ToDoTask task) {
		tasks.remove(task);
	}
	
	public String toString() {
		return this.getName();
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		if(tasks != null) {
			stream.writeUTF(name);
			stream.writeInt(tasks.size());
			for(int i = 0; i < tasks.size(); i++) {
				tasks.get(i).save(stream);
			}
		} else {
			
		}
	}
	
	public void load(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		name = stream.readUTF();
		tasks = new ArrayList<>();
		int x = stream.readInt();
		for(int i = 0; i < x; i++) {
			tasks.add(new ToDoTask(stream));
		}
	}
	
}
