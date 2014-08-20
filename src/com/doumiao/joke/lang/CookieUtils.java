package com.doumiao.joke.lang;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {
	public static void createCookie(HttpServletResponse response,
			String domain, String name, String value, String path, int age,
			boolean secure) {
		Cookie cookie = new Cookie(name, value);
		cookie.setDomain(domain);
		cookie.setMaxAge(age);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	public static String readCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static void deleteCookie(HttpServletResponse response, String domain,String name) {
		Cookie cookie = new Cookie(name, null);
		cookie.setDomain(domain);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}
