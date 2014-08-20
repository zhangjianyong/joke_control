package com.doumiao.joke.web.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.lang.CookieUtils;
import com.doumiao.joke.lang.Member;
import com.doumiao.joke.service.MemberService;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;

@Controller
public class QQLogin {
	private static final Log log = LogFactory.getLog(QQLogin.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private MemberService memberService;

	@Resource
	private ObjectMapper objectMapper;

	@RequestMapping("/qqbind")
	public String bind(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "t", required = false) String target)
			throws IOException {
		try {
			String sessionId = request.getSession().getId();
			String referer = request.getHeader("Referer");
			target = (target == null ? referer : target);
			jdbcTemplate
					.update("insert into uc_thirdplat_redirect(session,target,plat) values(?,?,?) on duplicate key update target=?",
							sessionId, target, "qq", target);
			String authUrl = new Oauth().getAuthorizeURL(request);
			return "redirect:" + authUrl;
		} catch (Exception e) {
			log.error(e, e);
			return "redirect:/qqbindfaild?t=" + target;
		}
	}

	@RequestMapping("/qqbindafter")
	public String bindAfter(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String openId = null, token = null;
		String sessionId = request.getSession().getId();
		// 查取登录成功后的目标地址
		String target = "/";
		try {
			target = jdbcTemplate
					.queryForObject(
							"select target from uc_thirdplat_redirect where session = ?",
							String.class, sessionId);
		} catch (EmptyResultDataAccessException erdae) {
		} catch (Exception e) {
			log.error(e, e);
		}

		// 取第三方平台的用户信息
		UserInfoBean userInfoBean = null;
		try {
			AccessToken tokenObj = (new Oauth())
					.getAccessTokenByRequest(request);
			token = tokenObj.getAccessToken();
			if (StringUtils.isBlank(token)) {
				return "redirect:/qqbindfaild?t=" + target;
			}
			// long tokenExpireIn = accessTokenObj.getExpireIn();
			openId = new OpenID(token).getUserOpenID();
			UserInfo qzoneUserInfo = new UserInfo(token, openId);
			userInfoBean = qzoneUserInfo.getUserInfo();
			if (userInfoBean.getRet() != 0) {
				request.setAttribute("error_msg", userInfoBean.getMsg());
				return "redirect:/qqbindfaild?t=" + target;
			}
			userInfoBean.getNickname();
		} catch (QQConnectException e) {
			log.error(e, e);
			return "redirect:/qqbindfaild?t=" + target;
		}

		// 查看该QQ是否绑定过平台账号
		Map<String, String> params = new HashMap<String, String>(3);
		params.put("openId", openId);
		params.put("token", token);
		int memberId = 0;
		try {
			memberId = jdbcTemplate
					.queryForInt(
							"SELECT member_id as id FROM uc_thirdplat_binding  WHERE plat=? and open_id=? and token=?",
							Plat.QQ.toString(), openId, token);
		} catch (EmptyResultDataAccessException erdae) {
		} catch (DataAccessException e) {
			log.error(e, e);
			return "redirect:/qqbindfaild?t=" + target;
		}

		// 用户存在登录,不存在注册
		Member u = new Member();
		u.setNick(userInfoBean.getNickname());
		if (memberId == 0) {
			u = memberService.bindThirdPlat(u, Plat.QQ, openId, token, params);
			if (u == null) {
				return "redirect:/qqbindfaild?t=" + target;
			}
		} else {
			u.setId(memberId);
		}

		try {
			String website = jdbcTemplate
					.queryForObject(
							"select value from joke_config where `key` = 'system_website_url'",
							String.class);
			String domain = website.replace("http://", "").replace("www", "");

			Map<String, Object> loginCookie = new HashMap<String, Object>(3);
			loginCookie.put("id", memberId);
			loginCookie.put("nick", u.getNick());
			String json = objectMapper.writeValueAsString(loginCookie);
			CookieUtils.createCookie(response, domain, "user", json, "/",
					Integer.MAX_VALUE, false);

		} catch (Exception e) {
			log.error(e, e);
			return "redirect:/qqbindfaild?t=" + target;
		}
		return "redirect:" + target;
	}

	@RequestMapping("/qqbindfaild")
	public String qqbindfaild(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return "/thirdplat/qq_login_faild";
	}

}
