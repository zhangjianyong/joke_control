package com.doumiao.joke.lang;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class Function {

	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {// nginx
			ip = request.getHeader("x-forwarded-for");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static String template(String templateContent,
			Map<String, Object> params) {
		assert (StringUtils.isNotBlank(templateContent));
		for (String key : params.keySet()) {
			templateContent = templateContent.replaceAll(
					"\\$\\{" + key + "\\}", (String) params.get(key));
		}
		return templateContent;
	}

	public static String joinUrl(String url, List<String[]> params) {
		assert (StringUtils.isNotBlank(url));
		String querystr = "?";
		if (!params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				querystr += params.get(i)[0] + "=" + params.get(i)[1];
				if (i < params.size() - 1) {
					querystr += "&";
				}
			}
			url += querystr;
		}
		return url;
	}

	public static String joinSql(String sql, List<String> options, String order) {
		assert (StringUtils.isNotBlank(sql));
		String where = null;
		if (!options.isEmpty()) {
			where = " where ";
			for (int i = 0; i < options.size(); i++) {
				where += options.get(i) + " ";
				if (i < options.size()) {
					where += "and ";
				}
			}
			sql += where;
		}
		sql += " " + order;
		return sql;
	}
}
