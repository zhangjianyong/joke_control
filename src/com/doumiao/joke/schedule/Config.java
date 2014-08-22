package com.doumiao.joke.schedule;

import java.util.HashMap;
import java.util.Map;

public class Config {
	public static Map<String, String> config = new HashMap<String, String>();

	public static void set(String key, String value) {
		config.put(key, value);
	}

	public static String get(String key) {
		return config.get(key);
	}
}
