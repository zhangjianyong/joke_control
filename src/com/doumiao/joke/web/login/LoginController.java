package com.doumiao.joke.web.login;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.doumiao.joke.lang.CookieUtils;
import com.doumiao.joke.vo.Result;

@Controller
public class LoginController {
	@Resource
	private JdbcTemplate jdbcTemplate;

	@ResponseBody
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public Result loginOut(HttpServletRequest request,
			HttpServletResponse response) {
		String website = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'system_website_url'",
						String.class);
		String domain = website.replace("http://", "").replace("www", "");
		CookieUtils.deleteCookie(response, domain, "loginuser");
		CookieUtils.deleteCookie(response, domain, "user");
		return new Result(true, "success", "退出登录", "");
	}
}