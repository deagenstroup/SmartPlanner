package deagen.smartplanner.logic;

import java.util.ArrayList;
import java.io.*;

public class ActivityPlanner {

	/**
	 * All of the categories of activities within the planner.
	 */
	private ArrayList<ActivityCategory> categories;
	
	public ActivityPlanner() {
		categories = new ArrayList<>();
	}
	
	public ActivityPlanner(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}
	
	public ActivityCategory getActivityCategory(String inName) {
		for(ActivityCategory cat : categories) {
			if(cat.getName().equals(inName))
				return cat;
		}
		return null;
	}

	public String[] getCategories() {
		String[] strings = new String[categories.size()];
		for(int i = 0; i < strings.length; i++) {
			strings[i] = categories.get(i).getName();
		}
		return strings;
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
