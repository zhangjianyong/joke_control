package com.doumiao.joke.annotation;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.doumiao.joke.lang.CookieUtils;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.vo.Result;

public class RequiredLoginAnnotationInterceptor extends
		HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		HandlerMethod handler2 = (HandlerMethod) handler;
		RequiredLogin login = handler2.getMethodAnnotation(RequiredLogin.class);
		if (null == login) {
			return true;
		}
		String _user = CookieUtils.readCookie(request, "_user");
		if (null == _user) {
			if (login.value() == ResultTypeEnum.page) {
				response.sendRedirect(Config.get("system_website_url")+"?login=true&to="+StringUtils.defaultIfBlank(
						request.getRequestURL(), "/"));
			} else if (login.value() == ResultTypeEnum.json) {
				response.setCharacterEncoding("utf-8");
				response.setContentType("text/html;charset=UTF-8");
				OutputStream out = response.getOutputStream();
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(out,
						"utf-8"));
				Result msg = new Result(false, "login_no", "请登录", "");
				pw.println(new ObjectMapper().writeValueAsString(msg));
				pw.flush();
				pw.close();
			}
			return false;
		}
		return true;
	}
}