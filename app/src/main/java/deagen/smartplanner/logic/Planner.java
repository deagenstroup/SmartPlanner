package deagen.smartplanner.logic;

import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.logic.taskplanning.ActivityCategory;
import deagen.smartplanner.logic.taskplanning.ActivityPlanner;
import deagen.smartplanner.logic.tasks.CompletedToDoTask;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;
import deagen.smartplanner.logic.tasks.ToDoTask;
import deagen.smartplanner.logic.taskscheduling.ListCalendar;
import deagen.smartplanner.logic.taskscheduling.TaskManager;
import deagen.smartplanner.logic.taskscheduling.ToDoList;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
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
		this.taskManager = new TaskManager(this.getSelectedToDoList());
	}



	// file I/O methods

	/**
	 * Save the planner to the designated file
	 */
	private void save() {
		if(fileName == null)
			return;
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
			calendar.save(stream);
			activityPlanner.save(stream);
			stream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load a planner into the object from designate file
	 */
	private void load(String inFileName) throws FileNotFoundException {
		fileName = inFileName;
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(new File(fileName)));
			calendar = new ListCalendar(stream);
			activityPlanner = new ActivityPlanner(stream);
		} catch(FileNotFoundException e) {
			throw(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	// simple accessors

	public LocalDate getDate() {
		return selectedDate;
	}

	public ActivityPlanner getActivityPlanner() {
		return activityPlanner;
	}

	/**
	 * @return The ToDoList for the selected date.
	 */
	public ToDoList getSelectedToDoList() {
		return calendar.getToDoList(this.getDate());
	}

	/**
	 * @return True if a ToDoList exists for the selected date, false otherwise.
	 */
	public boolean selectedToDoListExists() {
		if(this.getSelectedToDoList() != null)
			return true;
		else
			return false;
	}



	// simple modifiers

	public void selectDate(LocalDate date) {
		selectedDate = date;
		taskManager.setToDoList(this.getSelectedToDoList());
}


	// TASK MANAGER REFRACTOR
	public boolean isActive() {
		return taskManager.isActive();
	}

	public void startTasks(DailyPlannerFragment fragment) {
		taskManager.startTasks(fragment);
	}

	public void startTasks() { taskManager.startTasks(); }

	public void stopTasks() {
		taskManager.stopTasks();
	}

	public void extendCurrentTask(Duration inDur) {
		taskManager.extendCurrentActivity(inDur);
	}
	
	public void cutShortCurrentTask(Duration inDur) {
		taskManager.cutShortCurrentActivity(inDur);
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
	
	/**
	 * @param task The task to be added to the selected ToDoList
	 */
	public void addTask(ScheduledToDoTask task) {
		if(!this.selectedToDoListExists())
			calendar.addToDoList(new ToDoList(), selectedDate);
		this.getSelectedToDoList().addTask(task);
	}

	public void addTaskToCategory(int categoryPosition, ToDoTask task) {
		this.getActivityPlanner().getActivityCategory(categoryPosition).addToDoTask(task);
	}
	
	public ScheduledToDoTask removeTask(int position) {
		return this.getSelectedToDoList().removeTask(position);
	}

	public ToDoTask removeTaskFromCategory(int categoryPosition, int taskPosition) {
		return this.getActivityPlanner().getActivityCategory(categoryPosition).removeTask(taskPosition);
	}

	public ActivityCategory removeActivityCategory(int categoryPosition) {
		return this.getActivityPlanner().removeActivityCategory(categoryPosition);
	}

	public void moveScheduledTask(int from, int to) {
		this.getSelectedToDoList().moveTask(from, to);
	}

	public void moveCategory(int from, int to) {
		activityPlanner.moveActivityCategory(from, to);
	}

	public void moveTaskInCategory(int categoryPosition, int from, int to) {
		ActivityCategory category = this.getActivityPlanner().getActivityCategory(categoryPosition);
		category.moveTask(from, to);
	}

	public void extendCurrentActivity(Duration inDuration) {
		taskManager.extendCurrentActivity(inDuration);
	}
	
	public void cutShortCurrentActivity(Duration inDuration) {
		taskManager.cutShortCurrentActivity(inDuration);
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
	
	public void save(String inFileName) {
		fileName = inFileName;
		this.save();
	}

	/**
	 * Used to add stub values to the Planner for testing purposes.
	 */
	public void addTestValues() {
		ToDoList list;
		list = new ToDoList();
		list.addScheduledTask(new ScheduledToDoTask("work on app", "programming", Duration.ofSeconds(5L)));
		list.addScheduledTask(new ScheduledToDoTask("do python exercise", "programming", Duration.ofHours(2L)));
		list.addScheduledTask(new ScheduledToDoTask("read Neuromancer", "reading", Duration.ofMinutes(60L)));
		list.addCompletedTask(new CompletedToDoTask("read news article", "reading", Duration.ofMinutes(30L)));
		list.addCompletedTask(new CompletedToDoTask("run", "exercise", Duration.ofHours(2L)));
		calendar.addToDoList(list, LocalDate.now());

		list = new ToDoList();
		list.addCompletedTask(new CompletedToDoTask("skateboard", "exercise", Duration.ofHours(2L)));
		list.addCompletedTask(new CompletedToDoTask("work on app", "programming", Duration.ofHours(2L)));
		calendar.addToDoList(list, LocalDate.of(2019, 8, 14));
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
		
		ActivityCategory cat = new ActivityCategory("programming");
		cat.addToDoTask(new ToDoTask("work on app", "programming"));
		activityPlanner.addActivityCategory(cat);
		
		cat = new ActivityCategory("reading");
		cat.addToDoTask(new ToDoTask("read Neuromancer", "reading"));
		activityPlanner.addActivityCategory(cat);
		
		cat = new ActivityCategory("exercise");
		cat.addToDoTask(new ToDoTask("run", "exercise"));
		activityPlanner.addActivityCategory(cat);
	}
	
}
