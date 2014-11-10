package com.doumiao.joke.lang;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SerialNumberGenerator {
	private static int count = 100;
	private static Calendar lastTime = null;
	private static DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssS");

	synchronized public static String[] generate(int subSize) {
		if (subSize >= 90 || subSize < 0) {
			return null;
		}
		String[] serial = new String[subSize + 1];
		Calendar curTime = Calendar.getInstance();
		if (lastTime == null) {
			lastTime = curTime;
		}
		if (curTime.compareTo(lastTime) == 0) {
			count++;
		} else {
			count = 100;
			lastTime = curTime;
		}
		serial[0] = format.format(curTime.getTime()) + count;
		for (int i = 1; i < serial.length; i++) {
			serial[i] = serial[0] + (10 + i);
		}
		return serial;
	}
}