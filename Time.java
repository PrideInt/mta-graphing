/**
 * DO NOT COPY, MODIFY OR DISTRIBUTE
 *
 * - Pride
 */
public class Time {
	public enum DayTime {
		DAY, DAY_RUSH, NIGHT, NIGHT_RUSH, LATE_NIGHT
	}
	/**
	 * We validate inputted time by military standard.
	 * @param time
	 * @return true, if time is a valid time, false otherwise
	 */
	public static boolean validate(String time) {
		if (time.length() > 4) {
			return false;
		}
		String hours = time.substring(0, 1), minutes = time.substring(1, 3);

		if (time.length() > 3) {
			hours = time.substring(0, 2);
			minutes = time.substring(2, 4);
		}

		try {
			int hrs = Integer.valueOf(hours), mins = Integer.valueOf(minutes);

			// First case, if the time is inputted as e.g., 2401, then we will return false because 2401 is not a valid time
			if (hrs == 24 && mins > 0) {
				return false;
			} else if (hrs < 0 || mins < 0) { // Second case, if inputted time's hours and minutes are less than 0, return false
				return false;
			} else if (mins > 59) { // Third case, if hours is valid, but minutes is not e.g., when minutes is 60 or greater, return false
				return false;
			} else if (hrs > 24) { // Fourth case, if hours is invalid, but minutes is, return false
				return false;
			}
		} catch (Exception e) { return false; }

		return true;
	}
}

/**
 * This Day class is for simple use to classify days, validate days or just
 * any use case involving the day. Mostly used to avoid redundancy or easy
 * checks.
 */
class Day {
	public enum DayType {
		WEEKDAY, WEEKEND;
	}
	private static final String[] WEEKDAYS = {
			"monday", "tuesday", "wednesday", "thursday", "friday"
	};
	private static final String[] WEEKENDS = {
			"saturday", "sunday"
	};

	public static boolean validate(String day) {
		return day.equalsIgnoreCase("monday") || day.equalsIgnoreCase("tuesday") || day.equalsIgnoreCase("wednesday") || day.equalsIgnoreCase("thursday") ||
				day.equalsIgnoreCase("friday") || day.equalsIgnoreCase("saturday") || day.equalsIgnoreCase("sunday");
	}
	public static DayType type(String day) {
		for (String weekend : WEEKENDS) {
			if (day.equalsIgnoreCase(weekend)) {
				return DayType.WEEKEND;
			}
		}
		return DayType.WEEKDAY;
	}
}
