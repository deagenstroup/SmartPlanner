package deagen.smartplanner.logic.taskscheduling;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import deagen.smartplanner.logic.taskscheduling.ToDoList;

/**
 * A class for managing ToDoLists within a calendar.
 * @author Deagen Stroup
 */
public class ListCalendar {
	
	/**
	 * A mapping of tasks to the respective dates for which the tasks are scheduled.
	 */
	private HashMap<LocalDate, ToDoList> calendar;
	
	public ListCalendar() {
		calendar = new HashMap<>();
	}
	
	public ListCalendar(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		this.load(stream);
	}
	
	/**
	 * Get a ToDoList from the calendar
	 * @param date LocalDate object representing the date of the ToDoList to get
	 * @return The ToDoList
	 */
	public ToDoList getToDoList(LocalDate date) {
		return calendar.get(date);
	}
	
	/**
	 * Add a ToDoList for a specific date to the calendar
	 * @param list The ToDoList
	 * @param date LocalDate that the ToDoList is for.
	 */
	public void addToDoList(ToDoList list, LocalDate date) {
		calendar.put(date, list);
	}
	
	/**
	 * @return True if a ToDoList exists at the provided date.
	 */
	public boolean toDoListExists(LocalDate date) {
		if(calendar.get(date) != null)
			return true;
		else
			return false;
	}
	
	public void save(ObjectOutputStream stream) throws IOException {
		stream.writeInt(calendar.size());
		Iterator it = calendar.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			LocalDate date = (LocalDate)pair.getKey();
			ToDoList list = (ToDoList)pair.getValue();
			stream.writeObject(date);
			list.save(stream);
		}
	}
	
	public void load(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		int x = stream.readInt();
		calendar = new HashMap<>();
		for(int i = 0; i < x; i++) {
			LocalDate date = (LocalDate) stream.readObject();
			ToDoList list = new ToDoList(stream);
			calendar.put(date, list);
		}
	}
	
}
