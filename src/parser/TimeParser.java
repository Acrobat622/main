package parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

/**
 * 1) 10-11pm 2) 1am-10 3) HH/HH:MM for ___hours/hr/hrs/hour 4)
 * before/past/after midnight/noon 5) HH/HH:MM/HH.MM/HH,MM 0'clock 6) HH[:.,]MM
 * pm/am/a/p/a.m./p.m. 7) HH/HH:MM/HH.MM/HH,MM in the morning/in morn/ in the
 * afternoon/in the night/ at night/at afternoon/at morning/at
 * morn/morning/afternoon/morn/night 8) hhmm
 *
 * @author A0112823R
 */
public class TimeParser {

    private final String TIME_TO_TIME_KEYWORD = "\\b(((\\d+[.:,](\\d+)|\\d+)(\\s|)(am|pm|a.m.|p.m.|a|p|)(\\s|)(to|-)(\\s|)(\\d+[.:,](\\d+)|\\d+)(\\s|)(am|pm|a.m.|p.m.|a|p|)))\\b";
    private final String HOURS_APART_KEYWORD = "\\b(\\d+[.:,]\\d+|\\d+)((\\s|)(am|pm||a.m.|p.m.|a|p|)) for \\d+(\\s|)(hour|hours|hr|hrs)\\b";
    private final String TIME_WITH_OCLOCK_KEYWORD = "\\b(\\d+[:.,]\\d+|\\d+)(\\s|)o('|�)clock\\b";
    private final String TWELVE_HOUR_KEYWORD = "(\\d+[.,:]\\d+|\\d+)((\\s|)(am|pm|a.m.|p.m.|a|p))\\b";
    private final String BEFORE_NOON_BEFORE_MIDNIGHT_KEYWORD = "(\\b(before midnight|before noon)\\b)";
    private final String MORNING_AFTERNOON_NIGHT_KEYWORD = "(\\b(\\d+[.:,](\\d+)|\\d+)(\\s|)( (in (the |)|)(morning|morn)\\b| (in (the |)|)afternoon\\b| (in (the |)|)night\\b| at (the |)night\\b| at (the |)afternoon\\b"
            + "| at (the |)morning\\b| at (the |)morn\\b))";
    private final String TWENTY_FOUR_HH_KEYWORD = "(\\b\\d{1,2}[:.,]\\d{2}\\b) | (\\b\\d{4}\\b)";
    private final String PAST_NOON_PAST_MIDNIGHT_KEYWORD = "(\\b(past midnight|past noon|after noon|after midnight)\\b)";
    private final String TO_BE_REMOVED_KEYWORD = "(before midnight|before noon|"
            + "in afternoon|in night|in (morning|morn)|at afternoon|at night|at (morning|morn)|in the afternoon|in the night|in the (morning|morn)|at the afternoon|at the night|at the (morning|morn)|o'clock|past noon|past"
            + "midnight|after noon|after midnight|noon|midnight|\\s|afternoon|night|morning|morn|-|to|at|from|hours|hour|hrs|hr|(@ |due on |on |at |from |to |by |due |o�clock))";
    private int index;
    private String userInputLeft;
    private ArrayList<String> storageOfTime = new ArrayList<String>();

    public TimeParser(String userInput) throws Exception {
        extractTime(userInput, userInput);
    }

    public void extractTime(String userInput, String leftOverUserInput)
            throws Exception {

        userInput = removeThoseHashTag(userInput);
        userInput = switchAllToLowerCase(userInput);

        leftOverUserInput = removeThoseHashTag(leftOverUserInput);
        leftOverUserInput = switchAllToLowerCase(leftOverUserInput);

        userInputLeft = leftOverUserInput;

        goThroughTimeFormat(userInput);
    }

    public int getPosition() {
        return index;
    }

    /**
     * get the input left
     * 
     * @return the input left after removing all the time detected
     */
    public String getInputLeft() {

        return userInputLeft;
    }

    /**
     * get the time list detected
     * 
     * @return storage of time : list of times detected
     */
    public ArrayList<String> getTimeList() {
        return storageOfTime;
    }

    /**
     * indication of ~~ means that user want it to be in description ~~ means
     * escaped char
     * 
     * @param userInput
     * @return user input without ~
     */
    private String removeThoseHashTag(String userInput) {

        EscapedTextParser escapedTextParser = new EscapedTextParser(userInput);
        ArrayList<String> escapedTextList = escapedTextParser
                .getListOfEscapedText();

        for (int i = 0; i < escapedTextList.size(); i++) {

            userInput = userInput.replaceAll(escapedTextList.get(i), " ");

        }

        return userInput;
    }

    /**
     * go through the list of time format
     * 
     * @param userInput
     * @throws Exception
     *             : fail to parse time and IllegalArgumentException : key in
     *             invalid 12 and 24 hour format
     */
    private void goThroughTimeFormat(String userInput) throws Exception {

        spotHourApartKeyword();
        spotBeforeMidnightOrNoonKeyword(userInput);
        spotMorningAfternoonNightKeyword(userInput);
        spotPastMidnightOrNoonKeyword(userInput);
        spotHHOclockKeyword(userInput);
        spotTimeToTimeKeyword();
        spotTwelveHourFormat(userInput);
        spotTwentyFourHHKeyword(userInput);

    }

    /**
     * detect HH:MM/HH with pm and am behind.
     * 
     * @param storageOfTime
     * @throws IllegalArgumentException
     *             : invalid 12 hour format > 12
     * @return storage of time containing the time detected.
     */
    private void spotTwelveHourFormat(String userInput)
            throws IllegalArgumentException {

        Pattern timeDetector = Pattern.compile(TWELVE_HOUR_KEYWORD);
        Matcher matchedWithTime = timeDetector.matcher(userInputLeft);
        Matcher matchedForIndex = timeDetector.matcher(userInput);

        while (matchedWithTime.find()) {
            String time = matchedWithTime.group();

            testValidTime(time);

            userInputLeft = userInputLeft.replaceAll(time, "");

            if (matchedForIndex.find()) {
                int indexNext = matchedForIndex.start();

                time = removeUnwantedParts(time);
                time = changeToHourFormat(time);

                assert checkValid24HourTime(time) == true : "Wrong convertion of time";

                storageOfTime.add(time);
                setThePositionForTime(indexNext);

            }
        }
    }

    /**
     * spot time with past midnight, past noon,
     * 
     * @param userInput
     */
    private void spotPastMidnightOrNoonKeyword(String userInput) {
        Pattern timeDetector = Pattern.compile(PAST_NOON_PAST_MIDNIGHT_KEYWORD);
        Matcher matchedWithTime = timeDetector.matcher(userInputLeft);
        Matcher matchedForIndex = timeDetector.matcher(userInput);

        while (matchedWithTime.find()) {

            userInputLeft = userInputLeft.replaceAll(
                    PAST_NOON_PAST_MIDNIGHT_KEYWORD, "");
            String time = matchedWithTime.group();

            if (matchedForIndex.find()) {
                int indexNext = matchedForIndex.start();

                if (time.contains("noon")) {
                    storageOfTime.add("12:01");
                } else if (time.contains("midnight")) {
                    storageOfTime.add("00:01");
                }

                index = indexNext;
                setThePositionForTime(indexNext);
            }
        }
    }

    /**
     * get 24 hour format time HH.MM/HH,MM
     * 
     * @param userInput
     * @throws IllegalArgumentException
     *             : invalid 24 hour > 23
     */
    private void spotTwentyFourHHKeyword(String userInput)
            throws IllegalArgumentException {
        Pattern timeDetector = Pattern.compile(TWENTY_FOUR_HH_KEYWORD);
        Matcher containTime = timeDetector.matcher(userInputLeft);
        Matcher toGetIndex = timeDetector.matcher(userInput);

        while (containTime.find()) {
            String time = containTime.group();
            testValidTime(time);
            userInputLeft = userInputLeft.replaceAll(time, "");

            if (toGetIndex.find()) {

                int indexNext = toGetIndex.start();
                time = removeUnwantedParts(time);
                time = changeToHourFormat(time);

                assert checkValid24HourTime(time) == true : "Wrong convention of time";

                storageOfTime.add(time);
                setThePositionForTime(indexNext);
            }
        }
    }

    /**
     * detect time + o'clock and have a default morning
     * 
     * @param userInput
     */
    private void spotHHOclockKeyword(String userInput) {
        Pattern timeDetector = Pattern.compile(TIME_WITH_OCLOCK_KEYWORD);
        Matcher containTime = timeDetector.matcher(userInputLeft);
        Matcher toGetIndex = timeDetector.matcher(userInput);

        while (containTime.find()) {
            String time = containTime.group();

            testValidTime(time);
            userInputLeft = userInputLeft.replaceAll(time, "");

            if (toGetIndex.find()) {

                int indexNext = toGetIndex.start();
                time = removeUnwantedParts(time);
                time = changeToHourFormat(time);

                assert checkValid24HourTime(time) == true : "Wrong convention of time";

                storageOfTime.add(time);
                setThePositionForTime(indexNext);
            }
        }
    }

    /**
     * to prevent case sensitive, switch all to lower case
     * 
     * @param userInput
     * @return the user input all in lower case.
     */
    private String switchAllToLowerCase(String userInput) {
        userInput = userInput.replaceAll("\\s+", " ");
        userInput = " " + userInput.toLowerCase() + " ";
        return userInput;
    }

    /**
     * detect the start at __ am/pm for __ hour
     * 
     */
    private void spotHourApartKeyword() throws IllegalArgumentException {

        Pattern containTime = Pattern.compile(HOURS_APART_KEYWORD);
        Matcher toGetIndex = containTime.matcher(userInputLeft);

        while (toGetIndex.find()) {
            String time = toGetIndex.group();
            userInputLeft = userInputLeft.replaceAll(time, "");

            String startTime = detectStartTime(time);
            testValidTime(startTime);
            startTime = changeToHourFormat(startTime);
            int hhInTime = getHH(startTime);

            String numberOfHour = detectNumberOfHour(time);
            int numberOfHours = Integer.parseInt(numberOfHour);

            hhInTime = addTimeWithHours(numberOfHours, hhInTime);
            String hhTimeInString = toString(hhInTime);
            String minTime = getMinutes(startTime);

            time = hhTimeInString + ":" + minTime;

            assert checkValid24HourTime(time) == true : "Wrong convention of time";

            storageOfTime.add(startTime);
            storageOfTime.add(time);
        }

    }

    /**
     * convert the integer of HH to string
     * 
     * @param hhInTime
     * @return HH in string
     */
    private String toString(int hhInTime) {
        String hhTimeInString;

        if (hhInTime < 10) {
            hhTimeInString = "0" + hhInTime;
        } else {
            hhTimeInString = "" + hhInTime;
        }

        return hhTimeInString;
    }

    /**
     * add time with the number of hour
     * 
     * @param numberOfHours
     * @param hhInTime
     * @return HH in time
     */
    private int addTimeWithHours(int numberOfHours, int hhInTime) {

        hhInTime = hhInTime + numberOfHours;

        if (hhInTime > 23) {
            hhInTime = hhInTime - 24;
        }
        return hhInTime;
    }

    /**
     * detect time with or without pm/am
     * 
     * @param time
     * @return time with or without am/pm
     */
    private String detectStartTime(String time) {
        String digit1 = "(\\d+[.:,](\\d+)|\\d+)((\\s|)(am|pm|a.m.|p.m.|a|p|))";

        Pattern containTime = Pattern.compile(digit1);
        Matcher matchedWithTime = containTime.matcher(time);

        if (matchedWithTime.find()) {
            time = matchedWithTime.group();
        }
        return time;
    }

    /**
     * detect digit represent the number of hours
     * 
     * @param time
     * @return number of hour
     */
    private String detectNumberOfHour(String time) {
        String digit = "(\\d+)";
        String numberOfHour = "";
        Pattern timeDetector1 = Pattern.compile(digit);
        Matcher matchedWithTime1 = timeDetector1.matcher(time);

        while (matchedWithTime1.find()) {
            numberOfHour = matchedWithTime1.group();
        }

        return numberOfHour;
    }

    /**
     * detect the time like 6 in morning or 6 in afternoon or 6 in night
     * 
     */
    private void spotMorningAfternoonNightKeyword(String userInput) {
        Pattern timeDetector = Pattern.compile(MORNING_AFTERNOON_NIGHT_KEYWORD);
        Matcher matchedWithTime = timeDetector.matcher(userInputLeft);
        Matcher matchedForIndex = timeDetector.matcher(userInput);

        while (matchedWithTime.find()) {
            String time = matchedWithTime.group();
            userInputLeft = userInputLeft.replaceAll(time, "");

            if (matchedForIndex.find()) {
                int indexNext = matchedForIndex.start();

                if (time.contains("morning") || time.contains("morn")) {

                    time = removeUnwantedParts(time);
                    testValidTime(time);
                    time = changeToHourFormat(time + "am");

                } else if (time.contains("afternoon") || time.contains("night")) {
                    time = removeUnwantedParts(time);
                    testValidTime(time);
                    time = changeToHourFormat(time + "pm");
                }

                storageOfTime.add(time);
                assert checkValid24HourTime(time) == true : "Wrong convertion of time";
                setThePositionForTime(indexNext);
            }
        }
    }

    /**
     * detect start time and end time. able to detect HH:MM or 12hour format
     * with pm or am or none to or - HH:MM or 12hour format pm or am example: 1)
     * 12:30 - 1pm 2) 12pm to 1:30pm
     * 
     * @throws ParseException
     * 
     */
    private void spotTimeToTimeKeyword() throws Exception {

        Pattern timeDetector = Pattern.compile(TIME_TO_TIME_KEYWORD);
        Matcher matchedWithTime = timeDetector.matcher(userInputLeft);

        while (matchedWithTime.find()) {
            String time = matchedWithTime.group();
            String[] timeList = time.split("-|to");

            boolean isBothContainsAmPm = ifBothContainsAmPm(timeList);
            boolean isStartTimeContainsAmPm = ifStartTimeContainsAmPm(timeList);

            if (isAtleastOneAMPM(timeList)) {
                if (isBothContainsAmPm) {
                    addTimeWhenBothHaveMeridim(timeList);
                } else if (isStartTimeContainsAmPm) {
                    addTimeWhenStartTimeMeridiem(timeList);
                } else {
                    addTimeWhenEndTimeMeridiem(timeList);
                }

                if (storageOfTime.size() == 2) {
                    userInputLeft = userInputLeft.replaceAll(time, "");
                }
            }
        }
    }

    private boolean isAtleastOneAMPM(String[] timeList) {

        boolean isAtleastOneAMPM = false;

        if ((timeList[0].contains("a") || timeList[0].contains("p"))
                || (timeList[1].contains("a") || timeList[1].contains("p"))) {
            isAtleastOneAMPM = true;
        }
        return isAtleastOneAMPM;
    }

    /**
     * add the time when both have am/pm
     * 
     * @param timeList
     */
    private void addTimeWhenBothHaveMeridim(String[] timeList) {
        testValidTime(timeList[0]);
        testValidTime(timeList[1]);
        timeList[0] = removeUnwantedParts(timeList[0]);
        timeList[1] = removeUnwantedParts(timeList[1]);
        timeList[0] = changeToHourFormat(timeList[0]);
        timeList[1] = changeToHourFormat(timeList[1]);

        assert checkValid24HourTime(timeList[0]) == true : "Wrong convertion of time";
        assert checkValid24HourTime(timeList[1]) == true : "Wromg convertion of time";

        storageOfTime.add(timeList[0]);
        storageOfTime.add(timeList[1]);
    }

    /**
     * check that both time contain am/pm
     * 
     * @param timeList
     * @return
     */
    private boolean ifBothContainsAmPm(String[] timeList) {
        boolean isBothContainsAmPm = false;
        if ((timeList[0].contains("a") || timeList[0].contains("p"))
                && (timeList[1].contains("a") || timeList[1].contains("p"))) {
            isBothContainsAmPm = true;
        }
        return isBothContainsAmPm;
    }

    /**
     * add start time when the end time contains am/pm
     * 
     * @param timeList
     * @throws ParseException
     */
    private void addTimeWhenEndTimeMeridiem(String[] timeList)
            throws ParseException {

        String timeWithMeridiem = timeList[1];
        String timeWithoutMeridiem = timeList[0];
        timeWithoutMeridiem = timeWithoutMeridiem.trim();
        testValidTime(timeWithMeridiem);
        timeWithMeridiem = changeToHourFormat(timeWithMeridiem);

        if (isNotHHMMFormat(timeWithoutMeridiem)) {
            String timeSetToAM = changeToHourFormat(timeWithoutMeridiem + "am");

            String timeSetToPM = changeToHourFormat(timeWithoutMeridiem + "pm");

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Calendar calendarOfMeridiemKeyed = getCalendar(timeWithMeridiem,
                    dateFormat);

            Calendar calendarMaunalSetToAm = getCalendar(timeSetToAM,
                    dateFormat);

            Calendar calendarMaunalSetToPm = getCalendar(timeSetToPM,
                    dateFormat);

            String timeSelected = getSelectedStartTime(timeSetToAM,
                    timeSetToPM, calendarOfMeridiemKeyed,
                    calendarMaunalSetToAm, calendarMaunalSetToPm);

            assert checkValid24HourTime(timeWithMeridiem) == true : "Wrong convertion of time";
            assert checkValid24HourTime(timeSelected) == true : "Wromg convertion of time";

            addToStorageOfTime(timeWithMeridiem, timeSelected);
        }
    }

    /**
     * add if time selected is not empty
     * 
     * @param timeWithMeridiem
     * @param timeSelected
     */
    private void addToStorageOfTime(String timeWithMeridiem, String timeSelected) {
        if (!timeSelected.equals("")) {
            storageOfTime.add(timeSelected);
            storageOfTime.add(timeWithMeridiem);
        }
    }

    /**
     * to select the start time when only end time have am/pm 1) start time set
     * to am or pm equal to end time with am/pm keyed --> return those not equal
     * 2) two start time set to am or pm before or after end time with am/pm
     * keyed --> get the earliest between start times 3) time in between --> get
     * the latest between two start time
     * 
     * @param timeSetToAM
     * @param timeSetToPM
     * @param calendarOfMeridiemKeyed
     * @param calendarMaunalSetToAm
     * @param calendarMaunalSetToPm
     * @return
     */
    private String getSelectedStartTime(String timeSetToAM, String timeSetToPM,
            Calendar calendarOfMeridiemKeyed, Calendar calendarMaunalSetToAm,
            Calendar calendarMaunalSetToPm) {
        String timeSelected;

        if (calendarOfMeridiemKeyed.equals(calendarMaunalSetToPm)) {
            timeSelected = timeSetToAM;

        } else if (calendarOfMeridiemKeyed.equals(calendarMaunalSetToAm)) {
            timeSelected = timeSetToPM;

        } else if (calendarOfMeridiemKeyed.after(calendarMaunalSetToPm)
                && calendarOfMeridiemKeyed.after(calendarMaunalSetToAm)) {
            timeSelected = timeSetToPM;

        } else if (calendarOfMeridiemKeyed.before(calendarMaunalSetToPm)
                && calendarOfMeridiemKeyed.before(calendarMaunalSetToAm)) {

            timeSelected = timeSetToPM;
        } else {
            timeSelected = timeSetToAM;
        }
        return timeSelected;
    }

    /**
     * add the appropriate end time when only start contain am/pm
     * 
     * @param timeList
     * @throws ParseException
     *             : fail to parse
     */
    private void addTimeWhenStartTimeMeridiem(String[] timeList)
            throws ParseException {

        String timeWithMeridiem = timeList[0];
        String timeWithoutMeridiem = timeList[1];
        timeWithoutMeridiem = timeWithoutMeridiem.trim();
        testValidTime(timeWithMeridiem);
        timeWithMeridiem = changeToHourFormat(timeWithMeridiem);
        timeWithoutMeridiem = changeToHourFormat(timeWithoutMeridiem);

        if (isNotHHMMFormat(timeWithoutMeridiem)) {
            String timeSetToAM = changeToHourFormat(timeWithoutMeridiem + "am");
            String timeSetToPM = changeToHourFormat(timeWithoutMeridiem + "pm");

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Calendar calendarOfMeridiemKeyed = getCalendar(timeWithMeridiem,
                    dateFormat);

            Calendar calendarMaunalSetToAm = getCalendar(timeSetToAM,
                    dateFormat);

            Calendar calendarMaunalSetToPm = getCalendar(timeSetToPM,
                    dateFormat);

            String timeSelected = getSelectedEndTime(timeSetToAM, timeSetToPM,
                    calendarOfMeridiemKeyed, calendarMaunalSetToAm,
                    calendarMaunalSetToPm);

            assert checkValid24HourTime(timeWithMeridiem) == true : "Wrong convertion of time";
            assert checkValid24HourTime(timeSelected) == true : "Wromg convertion of time";

            addToStorageOfTime(timeSelected, timeWithMeridiem);
        }
    }

    /**
     * if it is hhmm, like 2300, we don need to go through this, it will
     * eventually be detected in the subsequent steps
     * 
     * @param timeWithoutMeridiem
     * @return
     */
    private boolean isNotHHMMFormat(String timeWithoutMeridiem) {
        return timeWithoutMeridiem.contains(":")
                || timeWithoutMeridiem.contains(".")
                || timeWithoutMeridiem.contains(",")
                || timeWithoutMeridiem.length() <= 2;
    }

    /**
     * get selected end time when only start time have am/pm 1) end time set to
     * am or pm equal to start time with am/pm keyed --> return those not equal
     * 2) two end time set to am or pm before or after start time with am/pm
     * keyed --> get the earliest between end times 3) time in between --> get
     * the latest between two end time
     * 
     * @param timeSetToAM
     * @param timeSetToPM
     * @param calendarOfMeridiemKeyed
     * @param calendarMaunalSetToAm
     * @param calendarMaunalSetToPm
     * @return
     */
    private String getSelectedEndTime(String timeSetToAM, String timeSetToPM,
            Calendar calendarOfMeridiemKeyed, Calendar calendarMaunalSetToAm,
            Calendar calendarMaunalSetToPm) {
        String timeSelected;
        if (calendarOfMeridiemKeyed.equals(calendarMaunalSetToPm)) {

            timeSelected = timeSetToAM;

        } else if (calendarOfMeridiemKeyed.equals(calendarMaunalSetToAm)) {

            timeSelected = timeSetToPM;

        } else if (calendarOfMeridiemKeyed.after(calendarMaunalSetToPm)
                && calendarOfMeridiemKeyed.after(calendarMaunalSetToAm)) {

            timeSelected = timeSetToAM;

        } else if (calendarOfMeridiemKeyed.before(calendarMaunalSetToPm)
                && calendarOfMeridiemKeyed.before(calendarMaunalSetToAm)) {

            timeSelected = timeSetToAM;

        } else {
            timeSelected = timeSetToPM;
        }
        return timeSelected;
    }

    /**
     * get calendar with specific time set on the same day.
     * 
     * @param timeWithMeridiem
     * @param dateFormat
     * @return
     * @throws ParseException
     *             : fail to parse time
     */
    private Calendar getCalendar(String timeWithMeridiem,
            SimpleDateFormat dateFormat) throws ParseException {

        Date timeContainMeridiem = dateFormat.parse(timeWithMeridiem);
        Calendar calendarOfMeridiemKeyed = Calendar.getInstance();
        calendarOfMeridiemKeyed.setTime(timeContainMeridiem);
        return calendarOfMeridiemKeyed;
    }

    /**
     * determine if the start time contain am or pm
     * 
     * @param timeList
     *            contains times
     * @return true if start time contain otherwise false
     */
    private boolean ifStartTimeContainsAmPm(String[] timeList) {
        boolean ifStartTimeContainsAmPm = false;

        if (timeList[0].contains("a") || timeList[0].contains("p")) {
            ifStartTimeContainsAmPm = true;
        }

        return ifStartTimeContainsAmPm;
    }

    /**
     * get the HH of the time in hour format(HH:MM)
     * 
     * @param time
     * @return HH
     */
    private int getHH(String time) {
        int index = getIndex(time);
        int hhInTime = Integer.parseInt(time.substring(0, index));

        return hhInTime;
    }

    /**
     * get the MM of the time in hour format(HH:MM)
     * 
     * @param pmTime1
     * @return HH
     */
    private String getMinutes(String time) {
        int index = getIndex(time);
        assert index != -1;
        String minutes = time.substring(index + 1);

        return minutes;
    }

    /**
     * throw and catch exception for invalid time for both 24 hour and 12 hour
     * format
     * 
     * @param time
     */
    private void testValidTime(String time) throws IllegalArgumentException {
        int timeInHour, timeInMin;
        Logger logger = Logger.getLogger("TimeParser");
        try {
            logger.log(Level.INFO,
                    "going to start processing for test valid time");

            time = removeUnwantedParts(time);
            boolean isTwelveHour = checkTwelveOrTwentyFourFormat(time);
            time = removePMOrAmOrOclock(time);

            if (time.contains(":") || time.contains(".") || time.contains(",")) {

                timeInHour = getHH(time);
                timeInMin = Integer.parseInt(getMinutes(time));
            } else if (time.length() <= 2) {

                timeInHour = Integer.parseInt(time);
                timeInMin = 0;
            } else if (time.length() == 4) {

                timeInHour = Integer.parseInt(time.substring(0, 2));
                timeInMin = Integer.parseInt(time.substring(2));
            } else {
                timeInHour = 0;
                timeInMin = 0;
            }

            getExceptionForInvalidTime(timeInHour, timeInMin, isTwelveHour);
        } catch (IllegalArgumentException e) {
            logger.log(Level.FINER, "Invalid of time!");
            JOptionPane.showMessageDialog(null, e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * get the exception for either 24 hour and 12 hour format
     * 
     * @param timeInHour
     * @param timeInMin
     * @param ifTwelveHour
     * @throws IllegalArgumentException
     *             : invalid 12 (>12) or 24(>23) hour format
     * 
     */
    private void getExceptionForInvalidTime(int timeInHour, int timeInMin,
            boolean ifTwelveHour) throws IllegalArgumentException {
        if (ifTwelveHour) {
            test12HourFormat(timeInHour, timeInMin);
        } else {
            test24HourFormat(timeInHour, timeInMin);
        }
    }

    /**
     * test 12 hour format of 0 <= hour <= 12 and 0 <= minutes <= 60
     * 
     * @param timeInHour
     * @param timeInMin
     * @throws IllegalArgumentException
     *             : beyond 12 for hour or beyond 60 for min
     */
    private void test12HourFormat(int timeInHour, int timeInMin)
            throws IllegalArgumentException {

        if (timeInHour > 12 || timeInHour < 0 || timeInMin < 0
                || timeInMin > 59) {
            throw new IllegalArgumentException(
                    "Time entered in 12-hour format is invalid!");
        }
    }

    /**
     * check if it is 24 hour format or 12 hour format
     * 
     * @param time
     * @return 12 hour format return true and 24 hour format return false
     */
    private boolean checkTwelveOrTwentyFourFormat(String time) {
        boolean isTwelveHour;

        if (time.contains("a") || time.contains("o'clock")
                || time.contains("p") || time.contains("o�clock")) {
            isTwelveHour = true;
        } else {
            isTwelveHour = false;
        }

        return isTwelveHour;
    }

    /**
     * test 24 hour format of 0 <= hour <= 23 and 0 <= minutes <= 60
     * 
     * @param timeInHour
     * @param timeInMin
     * @throws IllegalArgumentException
     *             : beyond 23 for hour or beyond 60 for min
     */
    private void test24HourFormat(int timeInHour, int timeInMin)
            throws IllegalArgumentException {
        if (timeInHour > 23 || timeInHour < 0 || timeInMin < 0
                || timeInMin > 59) {
            throw new IllegalArgumentException("Time entered is invalid!");
        }
    }

    /**
     * This is use to test whether my algo manipulate change to 24 hours time
     * format correctly without exceeding the boundary
     * 
     * @param time
     * @return true if 24 hour format does not exceed boundary, false when 24
     *         hours do exceed boundary
     */
    private boolean checkValid24HourTime(String time) {
        int timeInHour, timeInMin;
        boolean validTime = true;

        time = removePMOrAmOrOclock(time);
        time = removeUnwantedParts(time);
        if (time.length() > 2) {
            timeInHour = getHH(time);
            timeInMin = Integer.parseInt(getMinutes(time));
        } else {
            timeInHour = getHH(time);
            timeInMin = 0;
        }
        if (timeInHour > 23 || timeInHour < 0 || timeInMin < 0
                || timeInMin > 59) {
            validTime = false;
        }
        return validTime;
    }

    /**
     * remove the pm and am from the time
     * 
     * @param time
     * @return time without am or pm
     */
    private String removePMOrAmOrOclock(String time) {
        time = time.replaceAll("\\s+|pm|am|a.m.|p.m.|a|p|o�clock|o'clock", "");
        return time;
    }

    /**
     * set the position of the time based on which time is typed by the user 1st
     * the time typed by the user 1st is the start time
     * 
     * @param indexNext
     *            : the index in the user input
     */
    private void setThePositionForTime(int indexNext) {

        if (storageOfTime.size() == 2 && indexNext < index) {
            String temp = storageOfTime.get(0);
            storageOfTime.set(0, storageOfTime.get(1));
            storageOfTime.set(1, temp);
        }
        index = indexNext;
    }

    /**
     * detect before noon and before midnight
     * 
     */
    private void spotBeforeMidnightOrNoonKeyword(String userInput) {
        Pattern timeDetector = Pattern
                .compile(BEFORE_NOON_BEFORE_MIDNIGHT_KEYWORD);
        Matcher matchedWithTime = timeDetector.matcher(userInputLeft);
        Matcher matchedForIndex = timeDetector.matcher(userInput);

        while (matchedWithTime.find()) {
            userInputLeft = userInputLeft.replaceAll(
                    BEFORE_NOON_BEFORE_MIDNIGHT_KEYWORD, "");
            String time = matchedWithTime.group();

            if (matchedForIndex.find()) {
                int indexNext = matchedForIndex.start();
                if (time.contains("noon")) {
                    storageOfTime.add("11:59");
                } else if (time.contains("midnight")) {
                    storageOfTime.add("23:59");
                }

                setThePositionForTime(indexNext);
            }
        }
    }

    /**
     * change all of the time inputed to hour format(HH:MM)
     * 
     * @param time
     * @return time in hour format (HH:MM)
     */
    private String changeToHourFormat(String time) {

        time = time.trim();

        if (time.contains("a")) {

            time = removePMOrAmOrOclock(time);
            time = switchToAmHour(time);

        } else if (time.contains("p")) {

            time = removePMOrAmOrOclock(time);
            time = switchToPMHour(time);

        } else if (time.contains(".") || time.contains(",")) {

            time = changePuncToSemicolon(time);

        } else if (!time.contains(":") && time.length() <= 2) {

            time = time + ":00";

        } else if (!time.contains(":") && time.length() == 4) {

            time = time.substring(0, 2) + ":" + time.substring(2);
        }

        time = putOneZeroAtFront(time);

        return time;
    }

    /**
     * when the length is equal to 4 --> H:MM (eg: 2:30) Thus, have to put one
     * zero in front to HH:MM (eg: 02:30)
     * 
     * @param time
     * @return HH:MM
     */
    private String putOneZeroAtFront(String time) {
        time = time.trim();
        if (time.length() == 4) {
            time = "0" + time;
        }
        return time;
    }

    /**
     * switch to 24 hour time format of the time contain am
     * 
     * @param time
     * @return time in HH:MM
     */
    private String switchToAmHour(String time) {

        time = time.trim();
        int index = getIndex(time);

        if (time.length() > 2 && time.charAt(0) == '1' && time.charAt(1) == '2') {

            time = "00" + ":" + time.substring(index + 1);

        } else if (time.length() == 2 && time.charAt(0) == '1'
                && time.charAt(1) == '2') {

            time = "00:00";

        } else if (time.length() == 1) {

            time = "0" + time + ":00";

        } else if (time.length() == 2) {

            time = time + ":00";

        } else if (index != -1 && time.length() == 4) {

            time = "0" + time.substring(0, index) + ":"
                    + time.substring(index + 1);

        } else if (index != -1 && time.length() == 5) {

            time = time.substring(0, index) + ":" + time.substring(index + 1);

        }
        return time;
    }

    /**
     * change the . or , to :
     * 
     * @param time
     * @return time with :
     */
    private String changePuncToSemicolon(String time) {
        time = time.replaceAll("\\.|\\,", ":");
        return time;
    }

    /**
     * get the index of the separation of the HH and MM which is either : or .
     * 
     * @param time
     * @return the index of : or . depend which is detect
     */
    private int getIndex(String time) {
        int indexForPunc = -1;

        if (time.contains(":")) {

            indexForPunc = time.indexOf(":");

        } else if (time.contains(".")) {

            indexForPunc = time.indexOf(".");

        } else if (time.contains(",")) {

            indexForPunc = time.indexOf(",");

        }
        return indexForPunc;
    }

    /**
     * if pm is detected behind the time, switch it to hour format
     * 
     * @param time
     * @return hour format for time in pm.
     */
    private String switchToPMHour(String time) {
        int twentyFourhour = 13, twelveHourTime = 1, hourTime;
        String minTime, hourTimeInString = "";

        if (time.contains(":") || time.contains(".") || time.contains(",")) {
            hourTime = getHH(time);
            minTime = getMinutes(time);

        } else {
            hourTime = Integer.parseInt(time);
            minTime = "00";
        }

        while (twentyFourhour != 24) {
            if (hourTime == 12) {
                hourTimeInString = "12";
                break;
            } else if (hourTime == twelveHourTime) {
                hourTimeInString = String.valueOf(twentyFourhour);
                break;
            } else if (hourTime == 0) {
                hourTimeInString = String.valueOf(hourTime);
            }
            twentyFourhour++;
            twelveHourTime++;
        }

        assert hourTimeInString != "";

        time = hourTimeInString + ":" + minTime;

        return time;
    }

    /**
     * remove the conjunction
     * 
     * @param timeWithUnwantedPart
     * @return time free from conjunction
     */
    public String removeUnwantedParts(String timeWithUnwantedPart) {
        String time;
        time = timeWithUnwantedPart.replaceAll(TO_BE_REMOVED_KEYWORD, "");
        return time;
    }

}
