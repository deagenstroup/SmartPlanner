package com.deagenstroup.agendaassistant.logic.taskscheduling;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
		if(calendar.get(date) == null)
			this.addToDoList(new ToDoList(), date);
		return calendar.get(date);
	}

	/**
	 * Check if the provided date has tasks scheduled on it.
	 * @param date LocalDate of day to check.
	 * @return True if the day has tasks scheduled.
	 */
	public boolean hasScheduledTasks(LocalDate date) {
		ToDoList list = calendar.get(date);
		if(list == null || !list.hasScheduledTasks())
			return false;
		return true;
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
	 * Takes the tasks scheduled on the source date and reschedules them to the destination date.
	 * @param src LocalDate of day from which to transfer the tasks.
	 * @param dest LocalDate of the day to transfer tasks to.
	 */
	public void transferTasks(LocalDate src, LocalDate dest) {

		// Quit if there are no tasks on the source date.
		if(!this.hasScheduledTasks(src))
			return;

		// Get the ToDoList's of both dates,
		ToDoList srcList = this.getToDoList(src);
		ToDoList destList = this.getToDoList(dest);

		// Transfer the tasks from list to list.
		while(srcList.hasScheduledTasks()) {
			destList.addScheduledTask(srcList.removeTask(0));
		}

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
