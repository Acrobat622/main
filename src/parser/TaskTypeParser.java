package parser;

public class TaskTypeParser {
	private String taskType;

	public TaskTypeParser(String userInput) throws Exception {
		DateTimeParser dateTime = new DateTimeParser(userInput);
		int numberOfTimeInput = dateTime.getNumberOfTime();
		String taskType = null;

		if (numberOfTimeInput == 2) {
			taskType = "time task";
		} else if (numberOfTimeInput == 1) {
			taskType = "deadline";
		} else if(numberOfTimeInput == 0) {
			taskType = "floating task";
		}
		
		assert taskType != null :"error in detection detect too many time! or user key more than 2 times";
		setTaskType(taskType);
	}

	/**
	 * set the task type into this class
	 * @param detectedTaskType
	 */
	public void setTaskType(String detectedTaskType) {
		taskType = detectedTaskType;
	}
	
	/**
	 * Determine the type of task.
	 * @return the task type.
	 */
	public String getTaskType() {
		return taskType;
	}
}

