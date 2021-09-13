package com.deagenstroup.agendaassistant.logic.statistics;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

import com.deagenstroup.agendaassistant.logic.tasks.CompletedToDoTask;
import com.deagenstroup.agendaassistant.logic.taskscheduling.ListCalendar;
import com.deagenstroup.agendaassistant.logic.taskscheduling.ToDoList;

/**
 * Provides time statistics for a ListCalender
 * @author Deagen
 */
public class TimeAnalyzer {
	
	/**
	 * The object which stores the lists of completed tasks that are analyzed by the TimeAnalyzer.
	 */
	private ListCalendar calendar;
	
	public TimeAnalyzer(ListCalendar inCalendar) {
		calendar = inCalendar;
	}
	
	/**
	 * Get the amount of time spent on a category within the specified period of time.
	 * @param startDate The start of the specified period of time.
	 * @param endDate The end of the specified period of time.
	 * @param category The name of the category which is being analyzed.
	 * @return The duration of time spent on the category.
	 */
	public Duration getTimeSpent(LocalDate startDate, LocalDate endDate, String category) {
		LocalDate currentDate = startDate;
		Duration timeSpent = Duration.ofMinutes(0L);
		while(currentDate.compareTo(endDate) <= 0) {
//			System.out.println("current date: " + currentDate + " time so far " + timeSpent);
			ToDoList list = calendar.getToDoList(currentDate);
			if(list == null) {
				currentDate = currentDate.plusDays(1);
				continue;
			}
			ArrayList<CompletedToDoTask> tasks = list.getCompletedTasks();
			for(CompletedToDoTask task : tasks) {
//				System.out.println("category: " + task.getCategory());
				if(task.getCategory().equals(category)) {
					timeSpent = timeSpent.plus(task.getTimeSpent());
				}
			}
			currentDate = currentDate.plusDays(1);
		}
		return timeSpent;
	}
	
	/**
	 * Get the amount of time spent on a category for the specified date.
	 * @param date The date in question.
	 * @param category The name of the category in question.
	 * @return The amount of time spent on activities in the category.
	 */
	public Duration getDailyTimeSpent(LocalDate date, String category) {
		return this.getTimeSpent(date, date, category);
	}

	/**
	 * Get the amount of time spent on a activities of a category within a particular month.
	 * @param date A date belonging to the month in question.
	 * @param category The name of the category in question.
	 * @return The amount of time spent on the activities.
	 */
	public Duration getMonthlyTimeSpent(LocalDate date, String category) {
		LocalDate startDate = date.withDayOfMonth(1);
		LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());
		return this.getTimeSpent(startDate, endDate, category);
	}
	
	/**
	 * Get the amount of time spent on activities of the provided category during the entire
	 * week of the day provided.
	 * @param date The day belonging to the week which is in question.
	 * @param category The name of the category in question.
	 * @return The amount of time spent on the activites of the category in the week.
	 */
	public Duration getWeeklyTimeSpent(LocalDate date, String category) {
		LocalDate startDate = date.minusDays(date.getDayOfWeek().getValue());
		LocalDate endDate = date.plusDays(7-date.getDayOfWeek().getValue());
//		System.out.println(startDate + " - " + endDate);
		return this.getTimeSpent(startDate, endDate, category);
	}
	
	/**
	 * Get the average amount of time spent on activites of a particular category in a week.
	 * @param category The name of the category.
	 * @return The average amount of time spent on the category per week.
	 */
	public Duration getWeeklyAverage(LocalDate date, String category) {
		Duration timeSpent = this.getWeeklyTimeSpent(date, category);
		return timeSpent.dividedBy(7);
	}
	
	public Duration getMonthlyAverage(LocalDate date, String category) {
		Duration timeSpent = this.getMonthlyTimeSpent(date, category);
		return timeSpent.dividedBy(date.lengthOfMonth());
	}
	
}
