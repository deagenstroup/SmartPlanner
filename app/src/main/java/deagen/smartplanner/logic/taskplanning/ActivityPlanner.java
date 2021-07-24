package deagen.smartplanner.logic.taskplanning;

import java.util.ArrayList;
import java.io.*;

import deagen.smartplanner.logic.tasks.ToDoTask;

/**
 * Class responsible for keeping track of activities that have not yet been scheduled into the
 * user's calender.
 */
public class ActivityPlanner {

	/**
	 * Container for each of the categories in which each activity falls into.
	 */
	private ArrayList<ActivityCategory> categories;
	
	public ActivityPlanner() {
		categories = new ArrayList<>();
	}
	
	public ActivityPlanner(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}

	public String getTaskNameInCategory(int categoryPosition, int taskPosition) {
		return this.getActivityCategory(categoryPosition).getTask(taskPosition).getName();
	}

	public int getTaskNumberInCategory(int categoryPosition) {
		return this.getActivityCategory(categoryPosition).getNumberOfTasks();
	}

	public ActivityCategory getActivityCategory(String inName) {
		for(ActivityCategory cat : categories) {
			if(cat.getName().equals(inName))
				return cat;
		}
		return null;
	}

	public String[] getCategories() {
		if(categories.size() == 0)
			categories.add(new ActivityCategory("misc"));
		String[] strings = new String[categories.size()];
		for(int i = 0; i < strings.length; i++) {
			strings[i] = categories.get(i).getName();
		}
		return strings;
	}

	public void addTask(ToDoTask inTask) {
		this.getActivityCategory(inTask.getCategory()).addToDoTask(inTask);
	}

	public ActivityCategory getActivityCategory(int position) {
		return categories.get(position);
	}
	
	public int getNumberOfCategories() {
		return categories.size();
	}

	public void addActivityCategory(ActivityCategory cat) {
		categories.add(cat);
	}
	
	public void removeActivityCategory(ActivityCategory category) {
		categories.remove(category);
	}

	public ActivityCategory removeActivityCategory(int position) {
		return categories.remove(position);
	}

	public void moveActivityCategory(int from, int to) {
		ActivityCategory cat = categories.remove(from);
		categories.add(to, cat);
	}

	public void fillTestValues() {
		ActivityCategory homework = new ActivityCategory("homework");
		categories.add(homework);
		homework.addToDoTask(new ToDoTask("read chapter 5"));
		homework.addToDoTask(new ToDoTask("complete exercises 3-10"));
		homework.addToDoTask(new ToDoTask("finish paper outline"));
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		if(categories != null) {
			int x = categories.size();
			stream.writeInt(categories.size());
			for(int i = 0; i < categories.size(); i++) {
				categories.get(i).save(stream);
			}
		} else {
			stream.writeInt(0);
		}
	}
	
	public void load(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		categories = new ArrayList<>();
		int x = stream.readInt();
		for(int i = 0; i < x; i++) {
			categories.add(new ActivityCategory(stream));
		}
	}
	
}
