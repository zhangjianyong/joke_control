<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html><html lang=zh>
<head>
	<title>错误!</title>
</head>
<body>
	错误类型:${result.code }<br>
	错误编码:${result.msg }<br>
	错误:${result.content }
</body>
</html>