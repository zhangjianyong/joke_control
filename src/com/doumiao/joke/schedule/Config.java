package com.doumiao.joke.schedule;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Config {
	private static HashMap<String, String> config = new HashMap<String, String>();

	public static void set(String key, String value) {
		config.put(key, value);
	}

	public static String get(String key) {
		return config.get(key);
	}
	
	public static String get(String key,String defaultValue) {
		String v = config.get(key);
		if(StringUtils.isBlank(v)){
			return defaultValue;
		}
		return v;
	}
	
	public static int getInt(String key,int defaultValue) {
		String v = config.get(key);
		if(StringUtils.isBlank(v)){
			return defaultValue;
		}
		return Integer.parseInt(v);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,String> getConfig(){
		return (Map<String,String>)config.clone();
	}
}
