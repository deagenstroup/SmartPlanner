package deagen.smartplanner.logic.taskscheduling;

import java.util.ArrayList;
import java.io.*;

import deagen.smartplanner.logic.tasks.CompletedToDoTask;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;

/**
 * Keeps track of completed tasks and scheduled tasks.
 * @author deagen
 *
 */
public class ToDoList {
	
	/**
	 * List of tasks which have already been completed
	 */
	private ArrayList<CompletedToDoTask> completedTasks;
	
	/**
	 * List of tasks to be done, in the order they are to be done in
	 */
	private ArrayList<ScheduledToDoTask> toDoList;
	
	public ToDoList() {
		toDoList = new ArrayList<>();
		completedTasks = new ArrayList<>();
	}
	
//	public ToDoList(int i) {
//		this.fillTestValues(i);
//	}
	
	public ToDoList(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		this.load(stream);
	}
	
	/**
	 * Get a task at a particular position in the ToDoList
	 * @param position The position in the ToDoList of the task to be returned
	 * @return The ToDoTask
	 */
	public ScheduledToDoTask getToDoTask(int position) {
		return toDoList.get(position);
	}
	
	public ArrayList<ScheduledToDoTask> getScheduledTasks() {
		return toDoList;
	}
	
	public int getNumberOfScheduledTasks() {
		return toDoList.size();
	}
	
	/**
	 * @return True if there are tasks in this ToDoList
	 */
	public boolean hasTasks() {
		if(this.getScheduledTasks().size() > 0 || this.getCompletedTasks().size() > 0)
			return true;
		return false;
	}
	
	public ArrayList<CompletedToDoTask> getCompletedTasks() {
		return completedTasks;
	}
	
	/**
	 * @return The first task on the list, being worked on currently or to be worked on first
	 */
	public ScheduledToDoTask getCurrentTask() {
		if(this.getScheduledTasks() == null || this.getScheduledTasks().isEmpty())
			return null;
		return this.getScheduledTasks().get(0);
	}
	
	public void finishCurrentTask() {
		if(completedTasks == null)
			completedTasks = new ArrayList<CompletedToDoTask>();
		this.completedTasks.add(this.getCurrentTask().finish());
		this.removeTask(this.getCurrentTask());
	}
	
	/**
	 * Put a task on the list
	 * @param task The task to be put in the list
	 * @param position The position in the list where the task is to be put
	 */
	public void insertToDoTask(ScheduledToDoTask task, int position) {
		toDoList.add(position, task);
	}
	
	/**
	 * Adds a task to the end of the list
	 * @param task The task
	 */
	public void addTask(ScheduledToDoTask task) {
		toDoList.add(task);
	}
	
	/**
	 * Used for testing, to be removed in final version
	 */
	public void addCompletedTask(CompletedToDoTask task) {
		completedTasks.add(task);
	}

	public void addScheduledTask(ScheduledToDoTask task) { toDoList.add(task); }

	/**
	 * Removes a task from the ToDoList and returns it
	 * @param position
	 * @return
	 */
	public ScheduledToDoTask removeTask(int position) {
		return toDoList.remove(position);
	}
	
	public void removeTask(ScheduledToDoTask task) {
		toDoList.remove(task);
	}
	
	/**
	 * Move a task from one position to another
	 * @param fromPosition The position of the task to be moved
	 * @param toPosition The position where the task is to be moved to
	 */
	public void moveTask(int fromPosition, int toPosition) {
		toDoList.add(toPosition, toDoList.remove(fromPosition));
	}
	
	public void load(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		int x = stream.readInt();
		completedTasks = new ArrayList<>();
		for(int i = 0; i < x; i++) {
			completedTasks.add(new CompletedToDoTask(stream));
		}
		x = stream.readInt();
		toDoList = new ArrayList<>();
		for(int i = 0; i < x; i++) {
			toDoList.add(new ScheduledToDoTask(stream));
		}
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		if(completedTasks != null) {
			stream.writeInt(completedTasks.size());
			for(int i = 0; i < completedTasks.size(); i++) {
				completedTasks.get(i).save(stream);
			}
		} else {
			stream.writeInt(0);
		}
		if(toDoList != null) {
			stream.writeInt(toDoList.size());
			for(int i = 0; i < toDoList.size(); i++) {
				toDoList.get(i).save(stream);
			}
		} else {
			stream.writeInt(0);
		}
	}
	
//	public void fillWithTestTasks() {
//		this.completedTasks = new ArrayList<CompletedToDoTask>();
//		this.completedTasks.add(new CompletedToDoTask("eat breakfast", "meal", Duration.ofMinutes(15)));
//		this.addTask(new ScheduledToDoTask("go for a run", "exercise",  Duration.ofMinutes(20)));
//		this.addTask(new ScheduledToDoTask("program", "programming",  Duration.ofMinutes(120)));
//		this.addTask(new ScheduledToDoTask("read", "reading",  Duration.ofMinutes(40)));
//	}
//	
//	public void fillTestValues(int i) {
////		if(completedTasks == null)
////			completedTasks = new ArrayList<CompletedToDoTask>();
////		switch(i) {
////		case 1:
////			completedTasks.add(new CompletedToDoTask("worked on app", Duration.ofMinutes(180), "programming"));
////			completedTasks.add(new CompletedToDoTask("cleaned bathroom", Duration.ofMinutes(60), "cleaning"));
////			break;
////		case 2:
////			completedTasks.add(new CompletedToDoTask("worked on app", Duration.ofHours(5), "programming"));
////			completedTasks.add(new CompletedToDoTask("went for a run", Duration.ofMinutes(30), "exercise"));
////			break;
////		case 3:
////			completedTasks.add(new CompletedToDoTask("cleaned room", Duration.ofMinutes(90), "cleaning"));
////			completedTasks.add(new CompletedToDoTask("skateboard", Duration.ofMinutes(45), "exercise"));
////			break;
////		}
//	}
	
}
