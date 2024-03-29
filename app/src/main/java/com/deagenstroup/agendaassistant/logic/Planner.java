package com.deagenstroup.agendaassistant.logic;

import android.util.Log;

import com.deagenstroup.agendaassistant.logic.statistics.TimeAnalyzer;
import com.deagenstroup.agendaassistant.logic.taskplanning.ActivityCategory;
import com.deagenstroup.agendaassistant.logic.taskplanning.ActivityPlanner;
import com.deagenstroup.agendaassistant.logic.tasks.CompletedToDoTask;
import com.deagenstroup.agendaassistant.logic.tasks.ScheduledToDoTask;
import com.deagenstroup.agendaassistant.logic.tasks.ToDoTask;
import com.deagenstroup.agendaassistant.logic.taskscheduling.ListCalendar;
import com.deagenstroup.agendaassistant.logic.taskscheduling.TaskManager;
import com.deagenstroup.agendaassistant.logic.taskscheduling.ToDoList;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.*;

/**
 * A wrapper class for interfacing the core functionality of the planner application.
 */
public class Planner {
	
	private String fileName;
	
	/**
	 * The currently selected date for which tasks are being accessed
	 * or manipulated
	 */
	private LocalDate selectedDate;

	/**
	 * The calendar which contains all of the ToDoLists for the days on which they are planned.
	 */
	private ListCalendar calendar;
	
	/**
	 * Object which analyzes the completed tasks within the calender and provides statistics about
	 * them.
	 */
	private TimeAnalyzer timeAnalyzer;
	
	/**
	 * Manages the tasks for the selected day in real time, providing the user with notifications
	 * and reminders for the completion and time expended on tasks.
	 */
	private TaskManager taskManager;

	/**
	 * Object which allows the user to keep track of activities to be done, but not yet scheduled
	 * into a specific day.
	 */
	private ActivityPlanner activityPlanner;
	
	{
		this.selectedDate = LocalDate.now();
		calendar = null;
		activityPlanner = null;
	}

	public Planner() {
		this.calendar = new ListCalendar();
		this.timeAnalyzer = new TimeAnalyzer(this.calendar);
		this.taskManager = new TaskManager(this.getSelectedToDoList());
		this.activityPlanner = new ActivityPlanner();
	}
	
	/**
	 * Constructs the Planner object by loading values from the file provided.
	 */
	public Planner(String fileName) throws FileNotFoundException {
		this.load(fileName);
		this.timeAnalyzer = new TimeAnalyzer(this.calendar);
	}



	// file I/O methods

	/**
	 * Save the planner to the designated file
	 */
	private void save() {
		if(fileName == null) {
			Log.e("Planner", "fileName is null, unable to save Planner object.");
			return;
		}
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
			this.save(stream);
		} catch(IOException e) {
			Log.e("Planner", "IOException: unable to open stream object for planner save.");
			e.printStackTrace();
		}
	}

	/**
	 * Save the planner to the designated file
	 */
	public void save(ObjectOutputStream stream) {
		try {
			calendar.save(stream);
			activityPlanner.save(stream);
			taskManager.save(stream);
			stream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load a planner into the object from designate file
	 */
	public void load(String inFileName) throws FileNotFoundException {
		fileName = inFileName;
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(new File(fileName)));
			this.load(stream);
		} catch(IOException e) {
			Log.e("Planner", "IOException: planner not loaded successfully.");
		}
	}

	/**
	 * Load a planner into the object from designate file
	 */
	public void load(ObjectInputStream stream) throws FileNotFoundException {
		try {
			calendar = new ListCalendar(stream);
			activityPlanner = new ActivityPlanner(stream);
			taskManager = new TaskManager(this.getSelectedToDoList(), stream);
		} catch(FileNotFoundException e) {
			throw(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// accessors

	public LocalDate getDate() {
		return selectedDate;
	}

	public String getDateText() {
		LocalDate date = this.getDate();
		return "" + date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear();
	}

	public ActivityPlanner getActivityPlanner() {
		return activityPlanner;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public ListCalendar getCalendar() { return calendar; }

	/**
	 * @return The ToDoList for the selected date.
	 */
	private ToDoList getSelectedToDoList() {
		return calendar.getToDoList(this.getDate());
	}

	/**
	 * @return True if a ToDoList exists for the selected date, false otherwise.
	 */
	private boolean selectedToDoListExists() {
		return this.getSelectedToDoList() != null;
	}

	public ScheduledToDoTask getScheduledTask(int position) {
		return this.getScheduledTasks().get(position);
	}

	public CompletedToDoTask getCompletedTask(int position) {
		return this.getCompletedTasks().get(position);
	}

	/**
	 * @return All of the tasks for the selected day which have not been completed yet.
	 */
	public ArrayList<ScheduledToDoTask> getScheduledTasks() {
		if(this.getSelectedToDoList() == null)
			return null;
		return this.getSelectedToDoList().getScheduledTasks();
	}

	/**
	 * @return All of the tasks for the day which have been completed.
	 */
	public ArrayList<CompletedToDoTask> getCompletedTasks() {
		if(this.getSelectedToDoList() == null)
			return null;
		return this.getSelectedToDoList().getCompletedTasks();
	}

	public Duration getTodaysTimeSpent(String category) {
		return timeAnalyzer.getDailyTimeSpent(this.getDate(), category);
	}

	public Duration getThisWeeksTimeSpent(String category) {
		return timeAnalyzer.getWeeklyTimeSpent(this.getDate(), category);
	}

	public Duration getThisMonthsTimeSpent(String category) {
		return timeAnalyzer.getMonthlyTimeSpent(selectedDate, category);
	}

	public Duration getThisWeeklyAverage(String category) {
		return timeAnalyzer.getWeeklyAverage(this.getDate(), category);
	}

	public Duration getThisMonthlyAverage(String category) {
		return timeAnalyzer.getMonthlyAverage(this.getDate(), category);
	}


	// modifiers

	public void selectDate(LocalDate date) {
		selectedDate = date;
		taskManager.setToDoList(this.getSelectedToDoList());
	}
	
	/**
	 * @param task The task to be added to the selected ToDoList
	 */
	public void addTask(ScheduledToDoTask task, int position) {
		if(!this.selectedToDoListExists()) {
			calendar.addToDoList(new ToDoList(), selectedDate);
			taskManager.setToDoList(this.getSelectedToDoList());
		}
		this.getSelectedToDoList().addTask(task, position);
	}

	/**
	 * @param task The task to be added to the selected ToDoList
	 */
	public void addTask(ScheduledToDoTask task) {
		if(!this.selectedToDoListExists()) {
			calendar.addToDoList(new ToDoList(), selectedDate);
			taskManager.setToDoList(this.getSelectedToDoList());
		}
		this.getSelectedToDoList().addTask(task);
	}
	
	public ScheduledToDoTask removeTask(int position) {
		return this.getSelectedToDoList().removeTask(position);
	}

	public void moveScheduledTask(int from, int to) {
		this.getSelectedToDoList().moveTask(from, to);
	}
	
	public void save(String inFileName) {
		fileName = inFileName;
		this.save();
	}

	/**
	 * Used to add stub values to the Planner for testing purposes.
	 */
	public void addTestValues() {
//		calendar.getToDoList(LocalDate.of(2021, 8, 18)).addScheduledTask(
//				new ScheduledToDoTask("nothing", "misc", Duration.ofSeconds(5L)));

		ToDoList list;
		list = calendar.getToDoList(LocalDate.now());

		list.addScheduledTask(new ScheduledToDoTask("study for Calculus final", "school", Duration.ofMinutes(90L)));
		list.addScheduledTask(new ScheduledToDoTask("finish physics lab", "school", null));
		list.addScheduledTask(new ScheduledToDoTask("go to the grocery store", "shopping", null));
		list.addScheduledTask(new ScheduledToDoTask("practice piano", "music", Duration.ofMinutes(20L)));
		list.addScheduledTask(new ScheduledToDoTask("move trash bins", "misc", null));
		list.addScheduledTask(new ScheduledToDoTask("go for a run", "exercise", null));

//		list.addCompletedTask(new CompletedToDoTask("study for Calculus final", "school", Duration.ofMinutes(90L)));
//		list.addCompletedTask(new CompletedToDoTask("finish physics lab", "school", Duration.ofMinutes(16L).plusSeconds(24L)));
//		list.addCompletedTask(new CompletedToDoTask("go to the grocery store", "shopping", Duration.ofMinutes(84L).plusSeconds(47L)));
//		list.addCompletedTask(new CompletedToDoTask("practice piano", "music", Duration.ofMinutes(20L)));
//		list.addCompletedTask(new CompletedToDoTask("move trash bins", "misc", Duration.ofMinutes(0L)));
//		list.addCompletedTask(new CompletedToDoTask("go for a run", "exercise", Duration.ofMinutes(18L).plusSeconds(25L)));



//		list = new ToDoList();
//		list.addCompletedTask(new CompletedToDoTask("skateboard", "exercise", Duration.ofHours(2L)));
//		list.addCompletedTask(new CompletedToDoTask("work on app", "programming", Duration.ofHours(2L)));
//		calendar.addToDoList(list, LocalDate.of(2019, 8, 14));
//
//		list = new ToDoList();
//		list.addCompletedTask(new CompletedToDoTask("do python exercise", "programming", Duration.ofHours(2L)));
//		list.addCompletedTask(new CompletedToDoTask("read news article", "reading", Duration.ofMinutes(30L)));
//		calendar.addToDoList(list, LocalDate.of(2019, 8, 4));
//
//		list = new ToDoList();
//		list.addCompletedTask(new CompletedToDoTask("work on app", "programming", Duration.ofHours(3L)));
//		list.addCompletedTask(new CompletedToDoTask("skateboard", "exercise", Duration.ofHours(2L)));
//		list.addCompletedTask(new CompletedToDoTask("read Neuromancer", "reading", Duration.ofMinutes(60L)));
//		calendar.addToDoList(list, LocalDate.of(2019, 8, 3));
//
//		list = new ToDoList();
//		list.addCompletedTask(new CompletedToDoTask("work on app", "programming", Duration.ofHours(5L)));
//		list.addCompletedTask(new CompletedToDoTask("run", "exercise", Duration.ofHours(2L)));
//		calendar.addToDoList(list, LocalDate.of(2019, 8, 2));
//
//		list = new ToDoList();
//		list.addCompletedTask(new CompletedToDoTask("do programming exercise", "programming", Duration.ofHours(1L)));
//		list.addCompletedTask(new CompletedToDoTask("run", "exercise", Duration.ofHours(2L)));
//		list.addCompletedTask(new CompletedToDoTask("read Neuromancer", "reading", Duration.ofHours(2L)));
//		calendar.addToDoList(list, LocalDate.of(2019, 8, 1));

		ActivityCategory cat = new ActivityCategory("school");
		cat.addToDoTask(new ToDoTask("do physics lab", "school"));
		cat.addToDoTask(new ToDoTask("finish AI project", "school"));
		cat.addToDoTask(new ToDoTask("study for calculus final", "school"));
		activityPlanner.addActivityCategory(cat);

		cat = new ActivityCategory("shopping");
		cat.addToDoTask(new ToDoTask("buy headphones online", "shopping"));
		cat.addToDoTask(new ToDoTask("go to the grocery store", "shopping"));
		activityPlanner.addActivityCategory(cat);

		cat = new ActivityCategory("exercise");
		cat.addToDoTask(new ToDoTask("go for a run", "exercise"));
		activityPlanner.addActivityCategory(cat);

		cat = new ActivityCategory("cleaning");
		activityPlanner.addActivityCategory(cat);
	}

	public void printScheduledTasks() {
		ArrayList<ScheduledToDoTask> tasks = this.getScheduledTasks();
		for(int i = 0; i < tasks.size(); i++) {
			Log.d("Planner", "i: " + i + " " + tasks.get(i).toString());
		}
		if(tasks.size() == 0)
			Log.d("Planner", "No scheduled tasks.");
	}
}
