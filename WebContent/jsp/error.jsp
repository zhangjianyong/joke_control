<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html><html lang=zh>
<head>
	<title>错误!</title>
</head>
<body>
	错误类型:${result.code }<br>
	错误编码:${result.msg }<br>
	<c:if test="${result.code eq'qq_oauth_faild' }">
	<div>返回首页:<a href="/">一笑千金</a></div>
	</c:if>
	<c:if test="${result.code eq 'qq_timeout' }">
	<div>请重试:<a href="/qqbind" class="qq">QQ登录</a></div>
	</c:if>
</body>
</html>