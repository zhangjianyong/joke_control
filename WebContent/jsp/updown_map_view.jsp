<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page language="Java" %>
<%@ page import="org.springframework.web.context.ContextLoader"%>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page import="org.springframework.web.context.WebApplicationContext"%>
<%@ page import="com.doumiao.joke.web.Updown"%>
<%@ page import="java.lang.reflect.Field"%>
<%@ page import="java.lang.reflect.Method"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.Iterator"%>
<%
	//在servletContext中，
	//父容器的key是:WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE，
	//子容器的key是:"org.springframework.web.serrvlet.FrameworkServlet.CONTEXT."+Servlet名字，
	//获取父容器
	WebApplicationContext rootWac=ContextLoader.getCurrentWebApplicationContext();
	//获取servletContext
	ServletContext servletContext = rootWac.getServletContext();
	//获取子容器
	WebApplicationContext subWac=WebApplicationContextUtils.getWebApplicationContext(servletContext,
		"org.springframework.web.servlet.FrameworkServlet.CONTEXT.springmvc" );
	//获取子容器里的bean
	Updown updown = (Updown)subWac.getBean("updown");
	Field field=Updown.class.getDeclaredField("userTodayUpdown");
	field.setAccessible(true);
	@SuppressWarnings("unchecked") 
	Map<Integer, Set<Integer>> userTodayUpdown = (Map<Integer, Set<Integer>>)field.get(updown);
	String uid = request.getParameter("uid");
	int totleSize = userTodayUpdown.size();
	int userArticleSize = 0;
	Set<Integer> articleSet = null;
	if(uid != null){
		articleSet = userTodayUpdown.get(Integer.parseInt(uid));
		if(articleSet!=null){
			userArticleSize = articleSet.size();
		}
	}
	String clear = request.getParameter("clear");
	if(clear != null){
		Method method=Updown.class.getDeclaredMethod("clearUserUpDown", new Class[]{});
        method.setAccessible(true);  
        method.invoke(updown, new Object[]{});
	}
%>
<!DOCTYPE html>
<html lang=zh>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>查看用户当日顶过的文章</title>
</head>
<body>
	<table>
		<tr>
			<td>总大小:</td>
			<td><%=totleSize %></td>
		</tr>
		<tr>
			<td>用户ID:</td>
			<td><%=uid %></td>
		</tr>
		<tr>
			<td>用户今天顶过的文章数:</td>
			<td><%=userArticleSize %></td>
		</tr>
		<tr>
			<td>清理缓存:</td>
			<td><a href="?clear=true">执行</a></td>
		</tr>
	</table>
</body>
</html>