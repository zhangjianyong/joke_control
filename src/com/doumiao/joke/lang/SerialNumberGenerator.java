package com.doumiao.joke.lang;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SerialNumberGenerator {
	private static int count = 10000;
	private static Calendar lastTime = null;
	private static DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

	synchronized public static String[] generate(int subSize) {
		if(subSize >= 90 || subSize < 0){
			return null;
		}
		String[] serial = new String[subSize+1];
		Calendar curTime = Calendar.getInstance();
		curTime.clear(Calendar.MILLISECOND);
		curTime.set(curTime.get(Calendar.YEAR), curTime.get(Calendar.MONTH),
				curTime.get(Calendar.DATE), curTime.get(Calendar.HOUR_OF_DAY),
				curTime.get(Calendar.MINUTE), curTime.get(Calendar.SECOND));
		if (lastTime == null) {
			lastTime = curTime;
		}
		if (curTime.compareTo(lastTime) == 0) {
			count++;
		} else {
			count = 10001;
			lastTime = curTime;
		}
		serial[0] = format.format(curTime.getTime()) + count;
		for (int i = 1; i < serial.length; i++) {
			serial[i] = serial[0]+(10+i);
		}
		return serial;
	}
}
