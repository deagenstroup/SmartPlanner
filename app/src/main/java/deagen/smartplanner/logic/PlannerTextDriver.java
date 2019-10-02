package deagen.smartplanner.logic;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalTime;

public class PlannerTextDriver {
	
	public static String fileName = null;
	
	public static Planner planner;
	
	public static Scanner inputScanner;

//	public static LocalTime currentActivityEnd;
	
	public static void main(String[] args) throws IOException {
		inputScanner = new Scanner(System.in);
		if(fileName == null) {
			planner = new Planner(null);
			planner.addTestValues();
		} else
			planner = new Planner(null);
		
		System.out.println("----SmartPlanner----");
		mainMenu();
		Runtime.getRuntime().exec("clear");
		
		inputScanner.close();
	}
	
	public static void mainMenu() {
		do {
			System.out.println("--MainMenu--");
			System.out.println("1) ToDoPlanner");
			System.out.println("2) ActivityPlanner");
			System.out.println("3) Exit");
			System.out.print("Enter a menu choice: ");
			int input;
			do {
				input = inputScanner.nextInt();
				inputScanner.nextLine();
				if(input < 1 || input > 3) {
					System.out.print("Incorrect option, try again: ");
					input = -1;
				}
			} while(input == -1);
			System.out.println();
			
			switch(input) {
			case 1:
				plannerMenu();
				break;
			case 2:
				activityPlannerMenu();
				break;
			default:
				return;
			}
		} while(true);
	}
	
	/**
	 * Displays the options available to the user, gets user input,
	 * and takes respective action
	 */
	public static void plannerMenu() {
		do {
			System.out.println("--ToDoPlanner--:");
			System.out.println("1) Display selected Schedule");
			System.out.println("2) Select a date");
			System.out.println("3) Start tasks");
			System.out.println("5) Extend current task");
			System.out.println("6) Cut short current task");
			System.out.println("7) Add a task to the current schedule");
			System.out.println("8) Remove a task from the current schedule");
			System.out.println("9) Move a task");
			System.out.println("10) Save");
			System.out.println("11) Exit");
			System.out.print("Input Menu Option: ");
			int input;
			do {
				input = inputScanner.nextInt();
				inputScanner.nextLine();
				if(input < 1 || input > 11) {
					System.out.print("Invalid input, try again: ");
					input = -1;
				}
			} while(input == -1);
			System.out.println();
			
			switch(input) {
				case 1:
					displaySchedule();
					break;
				case 2:
					selectDate();
					break;
				case 3:
					startTasks();
					break;
				case 5:
					extendCurrentTask();
					break;
				case 6:
					cutShortCurrentTask();
					break;
				case 7:
					addATask();
					break;
				case 8:
					removeATask();
					break;
				case 9:
					moveATask();
					break;
				case 10:
					fileName = "testPlanner.dat";
					planner.save(fileName);
					break;
				default:
					return;
			}
			System.out.println("Press enter to continue...");
			inputScanner.nextLine();
		} while(true);
	}
	
	public static void activityPlannerMenu() {
		do {
			System.out.println("--ActivityPlanner--");
			System.out.println("1) View tasks from a category");
			System.out.println("2) Add a task to a category");
			System.out.println("3) Remove a tasks from a category");
			System.out.println("4) Add a category");
			System.out.println("5) Remove a category");
			System.out.println("6) View time stats for a category");
			System.out.println("7) Save");
			System.out.println("8) Exit");
			
			System.out.print("Select a menu option: ");
			int input;
			do {
				input = inputScanner.nextInt();
				inputScanner.nextLine();
				if(input < 1 || input > 8) {
					System.out.print("Incorrect input, try again: ");
					input = -1;
				}
			} while(input == -1);
			System.out.println();
			
			switch(input) {
			case 1:
				viewTasks();
				break;
			case 2:
				addTask();
				break;
			case 3:
				removeTask();
				break;
			case 4:
				addCategory();
				break;
			case 5:
				removeCategory();
				break;
			case 6:
				viewStats();
				break;
			case 7:
				planner.save();
				break;
			default:
				return;
			}
			System.out.println("Press enter to continue...");
			inputScanner.nextLine();
		} while(true);
	}
	
	public static void viewTasks() {
		ActivityCategory category = categorySelector();
		System.out.println("Tasks in " + category.getName() + ":");
		if(category.getNumberOfTasks() == 0) {
			System.out.println("There are no tasks in this category yet.");
			return;
		}
		for(int i = 0; i < category.getNumberOfTasks(); i++) {
			System.out.println(i+1 + ") " + category.getTask(i));
		}
	}
	
	public static void viewStats() {
		ActivityCategory category = categorySelector();
		System.out.println("Time spent today: " + planner.getTodaysTimeSpent(category.getName()));
		System.out.println("Time spent this week: " + planner.getThisWeeksTimeSpent(category.getName()));
		System.out.println("Daily average this week: " + planner.getThisWeeklyAverage(category.getName()));
		System.out.println("Time spent this month: " + planner.getThisMonthsTimeSpent(category.getName()));
		System.out.println("Daily average this month: " + planner.getThisMonthlyAverage(category.getName()));
	}
	
	public static void removeTask() {
		ActivityCategory category = categorySelector();
		category.removeTask(taskSelector(category));
	}
	
	public static void addTask() {
		ActivityCategory category = categorySelector();
		System.out.print("Enter task: ");
		category.addToDoTask(new ToDoTask(inputScanner.nextLine()));
	}
	
	public static void addCategory() {
		System.out.print("Enter the name of the category: ");
		String name = inputScanner.nextLine();
		planner.getActivityPlanner().addActivityCategory(new ActivityCategory(name));
	}
	
	public static void removeCategory() {
		ActivityCategory category = categorySelector();
		planner.getActivityPlanner().removeActivityCategory(category);
	}
	
 	public static ToDoTask taskSelector(ActivityCategory category) {
		for(int i = 0; i < category.getNumberOfTasks(); i++) {
			System.out.println(i + ")" + category.getTask(i));
		}
		System.out.print("Select a task: ");
		int input;
		do {
			input = inputScanner.nextInt();
			inputScanner.nextLine();
			if(input < 0 || input >= category.getNumberOfTasks()) {
				System.out.print("Incorrect input, try again: ");
				input = -1;
			}
		} while(input == -1);
		
		return category.getTask(input);
	}
	
	public static ActivityCategory categorySelector() {
		ActivityPlanner activityPlanner = planner.getActivityPlanner();
		for(int i = 0; i < activityPlanner.getNumberOfCategories(); i++) {
			System.out.println(i + ")" + activityPlanner.getActivityCategory(i));
		}
		System.out.print("Select a category: ");
		int input;
		do {
			input = inputScanner.nextInt();
			inputScanner.nextLine();
			if(input < 0 || input >= activityPlanner.getNumberOfCategories()) {
				System.out.print("Incorrect input, try again: ");
				input = -1;
			}
		} while(input == -1);
		
		return activityPlanner.getActivityCategory(input);
	}
	
	/**
	 * Prompts the user for a date and changes the current to do list for the
	 * one of the provided date
	 */
	public static void selectDate() {
		System.out.print("Input a date of the format mm/dd/yyyy: ");
		String[] subStr = inputScanner.nextLine().split("/");
		LocalDate date = LocalDate.of(Integer.parseInt(subStr[2]), Integer.parseInt(subStr[0]), Integer.parseInt(subStr[1]));
		planner.selectDate(date);
	}
	
	/**
	 * Displays the currently selected schedule.
	 * If tasks are currently being completed, then schedule lists time
	 * stamps for tasks.
	 */
	public static void displaySchedule() {
		System.out.println("Selected date: " + planner.getDate().toString());
		ToDoList selectedToDoList = planner.getSelectedToDoList();
		if(selectedToDoList == null || !selectedToDoList.hasTasks()) {
			System.out.println("There are no tasks for this date yet");
			return;
		}
		System.out.println("Daily Schedule:");
		
		ArrayList<CompletedToDoTask> tasks = planner.getCompletedTasks();
		// check for completed tasks and print them out
		if(tasks != null && tasks.size() != 0) {
			System.out.println("Completed Tasks: ");
			for(int i = 0; i < tasks.size(); i++) {
				CompletedToDoTask task = tasks.get(i);
				System.out.println(i + ") " + task.getName() + ", " + task.getCategory() + " " + task.getTimeSpent());
			}
		}
		
		// check for uncompleted tasks and print them out
		ArrayList<ScheduledToDoTask> scheduledTasks = planner.getScheduledTasks();
		if(scheduledTasks != null && scheduledTasks.size() != 0) {
			System.out.println("Scheduled Tasks: ");
			for(int i = 0; i < scheduledTasks.size(); i++) {
				ScheduledToDoTask task = scheduledTasks.get(i);
				System.out.println(i + ") " + task.getName() + ", " + task.getCategory() + " " + task.getTimeRemaining());
			}
		}
		
	}
	
	public static void displayActiveSchedule() {
		ArrayList<ScheduledToDoTask> tasks = planner.getScheduledTasks();
		ScheduledToDoTask currentTask = tasks.get(0);
		System.out.println("current task: " + currentTask.getName());
		System.out.println("time spent: " + currentTask.getTimeSpent());
		System.out.println("time remaining: " + currentTask.getTimeRemaining());
		
		System.out.println("Scheduled Tasks: ");
		ArrayList<ScheduledToDoTask> scheduledTasks = planner.getScheduledTasks();
		
		LocalTime time = planner.getCurrentActivityEnd();
		for(int i = 1; i < scheduledTasks.size(); i++) {
			ScheduledToDoTask task = scheduledTasks.get(i);
			System.out.print(i + ") " + task.getName() + " " + task.getTimeRemaining() + " " + time);
			time = time.plus(task.getTimeRemaining());
			System.out.println("-" + time);
		}
	}
	
	public static void startTasks() {
		planner.startTasks();
		inputScanner.nextLine();
		planner.stopTasks();
	}
	
	public static void extendCurrentTask() {
		planner.extendCurrentActivity(promptForDuration());
	}
	
	public static void cutShortCurrentTask() {
		planner.cutShortCurrentActivity(promptForDuration());
	}

	public static Duration promptForDuration() {
		System.out.print("Enter amount of minutes: ");
		int minutes = inputScanner.nextInt();
		inputScanner.nextLine();
		return Duration.ofMinutes(minutes);
	}
	
	/**
	 * Displays all of the tasks for the currently selected ToDoList and prompts the user
	 * to select one of them.
	 * @return The selected ToDoTask
	 */
	public static ToDoTask selectToDoTask() {
		return null;
	}
	
	/**
	 * Displays the details of the provided task.
	 * @param task
	 */
	public static void displayTask(ToDoTask task) {
		
	}
	
	/**
	 * Prompt the user for the index of a task in the list.
	 * @return
	 */
	public static int selectTask() {
		displaySchedule();
		System.out.print("Enter number of scheduled task: ");
		int selection = inputScanner.nextInt();
		inputScanner.nextLine();
		return selection;
	}
	
	public static void addATask() {
//		System.out.print("Enter name of task: ");
//		String taskName = inputScanner.nextLine();
//		System.out.print("Enter the category of the task: ");
//		String categoryName = inputScanner.nextLine();
		ActivityCategory cat = categorySelector();
		ToDoTask task = taskSelector(cat);
		System.out.print("Enter projected time taken in minutes: ");
		int taskMinutes = inputScanner.nextInt();
		inputScanner.nextLine();
		planner.addTask(new ScheduledToDoTask(task.getName(), task.getCategory(), Duration.ofMinutes(taskMinutes)));
		System.out.print("Would you like to remove the task from the selected category? ");
		String answer = inputScanner.nextLine();
		if(answer.charAt(0) == 'y')
			cat.removeTask(task);
	}

	public static void removeATask() {
		int index = selectTask();
		planner.removeTask(index);
	}

	public static void moveATask() {
		int index = selectTask();
		System.out.print("Enter position in list for task to be moved to: ");
		int position = inputScanner.nextInt();
		inputScanner.nextLine();
		planner.moveScheduledTask(index, position);
	}
}
