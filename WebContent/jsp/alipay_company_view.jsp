<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html><html lang=zh>
<head>
	<title>${config.system_website_name}支付宝账号</title>
</head>
<body>
	错误信息:${error }<br>
	用户id:${config.alipay_company_user_id }<br>
	token:${config.alipay_company_token }<br>
	授权过期时间(s):${config.alipay_company_expires }<br>
	刷新token:${config.alipay_company_refresh_token }<br>
	集分宝余额:${pointAmount/100 }元<br>
	采购集分宝余额:${budgetAmount/100}元<a href="https://jf.alipay.com/aop/purchase.htm" target="_blank">采购</a><br>
	已发放的集分宝:${payed }元<br>
	未发放的集分宝:${info.s }元&nbsp;&nbsp;未发放笔数:${info.c }笔&nbsp;&nbsp;<a href="/alipaypoint" target="_blank">发放</a><br>
	今日签到人数:${day.m }&nbsp;&nbsp;今日签到次数:${day.c }&nbsp;&nbsp;今日签到成本:${day.s }<br>
	
	<table style="background-color: #63b8fd;width:98%;">
		<tr style="background-color: #e5ecfe;">
			<td>用户ID</td>
			<td>账户</td>
			<td>金额(分)</td>
			<td>获得时间</td>
			<td>父流水号</td>
			<td>子流水号</td>
		</tr>
		<c:forEach var="l" items="${logs }" varStatus="s">
			<c:if test="${s.index%2==0}"><tr style="background-color: #f9f9f9;"></c:if>
			<c:if test="${s.index%2==1}"><tr style="background-color: #f2fdff;"></c:if>
			<td>${l.member_id }</td>
			<td>${l.account }</td>
			<td>${l.wealth }</td>
			<td>${l.create_time }</td>
			<td>${l.serial_number }</td>
			<td>${l.sub_serial_number }</td></tr>
		</c:forEach>
	</table>
</body>
</html>