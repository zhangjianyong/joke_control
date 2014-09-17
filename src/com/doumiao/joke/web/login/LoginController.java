package com.doumiao.joke.web.login;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.doumiao.joke.lang.CookieUtils;
import com.doumiao.joke.schedule.Config;

@Controller
public class LoginController {
	@Resource
	private JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String loginOut(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="t",required=false) String target) {
		String website = Config.get("system_website_url","");
		String domain = Config.get("cookie_domain","");
		CookieUtils.deleteCookie(response, domain, "_user");
		CookieUtils.deleteCookie(response, domain, "user");
		if(StringUtils.isBlank(target)){
			target=website;
		}
		return "redirect:"+target;
	}
}