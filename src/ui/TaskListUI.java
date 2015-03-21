package ui;

import java.util.ArrayList;

import application.Task;
import application.TaskPrinter;

public class TaskListUI {
	ArrayList<Task> taskList;
	
	public TaskListUI(ArrayList<Task> taskList) {
		this.taskList = taskList;
	}
	
	public void showTask() {
		TaskPrinter tp = new TaskPrinter();
		ArrayList<ArrayList<String>> taskListMatrix = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> dataFields = new ArrayList<String>();
		dataFields.add("No.");
		dataFields.add("Description");
		dataFields.add("StartDateTime");
		dataFields.add("EndDateTime");
		dataFields.add("Deadline");
		dataFields.add("Status");

		taskListMatrix.add(dataFields);

		int taskNumber = 1;
		
		for (Task task : taskList) {
			ArrayList<String> taskInformation = new ArrayList<String>();
			taskInformation.add((Integer.toString(taskNumber++)));
			taskInformation.add(task.getDescription());
			taskInformation.add(task.getStartDateTime());
			taskInformation.add(task.getEndDateTime());
			taskInformation.add(task.getDeadline());
			taskInformation.add(task.getStatus());
			taskListMatrix.add(taskInformation);
		}
		
		tp.print(taskListMatrix);
	}
}
