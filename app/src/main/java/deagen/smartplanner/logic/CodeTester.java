package deagen.smartplanner.logic;

import java.time.Duration;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;

public class CodeTester {
	public static void main(String[] args) {
		//testToDoTask();
		//testToDoList();
		//testPlanner();
		testSaveAndLoad();
	}
	
	public static void testSaveAndLoad() {
		System.out.println("Testing ToDoTasks...");
		File file = new File("testfile.dat");
		ToDoTask savedTask = new ToDoTask("do coding exercise", "programming");
		ScheduledToDoTask savedScheduled = new ScheduledToDoTask("go for a run", "exercise", Duration.ofMinutes(30));
		CompletedToDoTask savedCompleted = new CompletedToDoTask("go to the store", "shopping", Duration.ofMinutes(60));
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(file));
			savedTask.save(outputStream);
			savedScheduled.save(outputStream);
			savedCompleted.save(outputStream);
			outputStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ToDoTask loadedTask = null;
		ScheduledToDoTask loadedScheduled = null;
		CompletedToDoTask loadedCompleted = null;
		ObjectInputStream inputStream = null;
		try {
			inputStream = new ObjectInputStream(new FileInputStream(new File("testfile.dat")));
			loadedTask = new ToDoTask(inputStream);
			loadedScheduled = new ScheduledToDoTask(inputStream);
			loadedCompleted = new CompletedToDoTask(inputStream);
			inputStream.close();
		} catch (IOException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(savedTask.toString().equals(loadedTask.toString())) {
			System.out.println("ToDoTask success");
		}
		if(savedScheduled.toString().equals(loadedScheduled.toString())) {
			System.out.println("ScheduledToDoTask success");
		}
		if(savedCompleted.toString().equals(loadedCompleted.toString())) {
			System.out.println("CompletedToDoTask success");
		}
	}
	
	public static void testToDoTask() {
//		ToDoTask task = new ToDoTask("fold clothes");
//		
//		// constructor test case
//		System.out.println("Constructor: " + (task.toString() == "fold clothes" ? "success" : "fail"));
//		
//		Duration hourThirtyFive = Duration.ofHours(1).plus(Duration.ofMinutes(35));
//		ScheduledToDoTask schTask = new ScheduledToDoTask("go for a run", hourThirtyFive);
//		System.out.println("ScheduledTaskConstructor: " + (schTask.toString().equals("go for a run, PT1H35M, false") ? "success" : "fail"));
//		
//		schTask.extendTime(hourThirtyFive);
//		System.out.println("ScheduledTaskExtend: " + (schTask.toString().contentEquals("go for a run, PT3H10M, false") ? "success" : "fail"));
//
//		schTask.cutShortTime(Duration.ofMinutes(15));
//		System.out.println("ScheduledTaskCut: " + (schTask.toString().contentEquals("go for a run, PT2H55M, false") ? "success" : "fail"));
//		
//		schTask.finish(Duration.ofMinutes(35));
//		System.out.println("ScheduledTaskFinish: " + (schTask.toString().contentEquals("go for a run, PT35M, true") ? "success" : "fail"));
	}

	public static void testPlanner() {
//		// create toDoList for a date and add it to the planner
//		ToDoList todaysList = new ToDoList();
//		todaysList.addTask(new ScheduledToDoTask("go for a run",  Duration.ofMinutes(25)));
//		todaysList.addTask(new ScheduledToDoTask("program",  Duration.ofMinutes(120)));
//		todaysList.addTask(new ScheduledToDoTask("read",  Duration.ofMinutes(45)));
//		PlannerTextDriver.planner = new Planner();
//		PlannerTextDriver.planner.addToDoList(todaysList, LocalDate.now());
//		
//		// select the date and display it
//		PlannerTextDriver.planner.selectDate(LocalDate.now());
//		PlannerTextDriver.displaySchedule();
//		
//		// select an empty date and display it
//		PlannerTextDriver.planner.selectDate(LocalDate.now().plusDays(1L));
//		PlannerTextDriver.displaySchedule();
//		
//		// select the original date and display it again
//		PlannerTextDriver.planner.selectDate(LocalDate.now());
//		PlannerTextDriver.planner.extendCurrentActivity(Duration.ofMinutes(10));
//		PlannerTextDriver.displaySchedule();
//		PlannerTextDriver.planner.cutShortCurrentActivity(Duration.ofMinutes(20));
//		PlannerTextDriver.displaySchedule();
	}
	
	public static void testToDoList() {
//		ToDoList list = new ToDoList();
//		PlannerTextDriver.selectedToDoList = list;
//		Duration hourThirtyFive = Duration.ofHours(1).plus(Duration.ofMinutes(35));
//		ScheduledToDoTask schTask = new ScheduledToDoTask("go for a run", hourThirtyFive);
//		ScheduledToDoTask schTask1 = new ScheduledToDoTask("program", hourThirtyFive.minus(Duration.ofMinutes(25)));
//		list.insertToDoTask(schTask, 0);
//		list.insertToDoTask(schTask1, 0);
//		PlannerTextDriver.displaySchedule();
//		System.out.println(PlannerTextDriver.getTaskString(list.getCurrentTask()));
//		list.moveTask(1, 0);
//		PlannerTextDriver.displaySchedule();
	}
}
