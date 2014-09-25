<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html><html lang=zh>
<head>
	<title>${config.system_website_name}支付宝账号</title>
</head>
<body>
	用户id:${config.alipay_company_user_id }<br>
	token:${config.alipay_company_token }<br>
	授权过期时间(s):${config.alipay_company_expires }<br>
	刷新token:${config.alipay_company_refresh_token }<br>
	集分宝余额:${pointAmount }<br>
	采购集分宝余额:${budgetAmount }<br>
</body>
</html>