package com.doumiao.joke.web.login;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.doumiao.joke.coder.DESCoder;
import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.lang.CookieUtils;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.service.MemberService;
import com.doumiao.joke.vo.Member;
import com.doumiao.joke.vo.Result;
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
	public String bind(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "t", required = false) String target) throws IOException {
		try {
			String refferer = request.getHeader("Referer");
			target = target == null ? refferer : target;
			if (StringUtils.isBlank(target)) {
				target = Config.get("system_website_url", "");
			}
			String authUrl = new Oauth().getAuthorizeURL(request);
			request.getSession().setAttribute("qq_target", target);
			return "redirect:" + authUrl;
		} catch (Exception e) {
			log.error(e, e);
			return "redirect:/error";
		}
	}

	@RequestMapping("/qqbindafter")
	public String bindAfter(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String openId = null, token = null;
		// 查取登录成功后的目标地址
		String target = (String) request.getSession().getAttribute("qq_target");
		if (StringUtils.isBlank(target)) {
			target = Config.get("system_website_url", "");
		}
		// 取第三方平台的用户信息
		UserInfoBean userInfoBean = null;
		int tt = 0;
		while (tt < 10) {
			try {
				AccessToken tokenObj = (new Oauth()).getAccessTokenByRequest(request);
				token = tokenObj.getAccessToken();
				openId = new OpenID(token).getUserOpenID();
				UserInfo qzoneUserInfo = new UserInfo(token, openId);
				userInfoBean = qzoneUserInfo.getUserInfo();
				break;
			} catch (QQConnectException qe) {
				tt++;
				if (log.isDebugEnabled()) {
					log.debug("qq login retry " + tt);
				}
			} catch (Exception e) {
				log.error("qq oauth error", e);
				request.setAttribute("result", new Result(false, "qq_oauth_faild", "qq登录授权错误", null));
				return "redirect:/error";
			}
		}
		if (tt >= 10) {
			request.setAttribute("result", new Result(false, "qq_timeout", "qq登录超时", null));
			return "redirect:/error";
		}
		// 查看该QQ是否绑定过平台账号
		Map<String, String> params = new HashMap<String, String>(3);
		params.put("openId", openId);
		params.put("token", token);
		// 用户存在登录,不存在注册
		Member u = new Member();
		u.setNick(userInfoBean.getNickname());
		try {
			int memberId = jdbcTemplate.queryForInt(
					"SELECT member_id as id FROM uc_thirdplat_binding  WHERE plat=? and open_id=?", Plat.QQ.toString(),
					openId);
			u.setId(memberId);
		} catch (EmptyResultDataAccessException erdae) {
			// 用户不存在则注册
			u = memberService.bindThirdPlat(u, Plat.QQ, openId, token, params);
		}

		String domain = Config.get("cookie_domain", "");
		String key = Config.get("system_cookie_key", "");
		String charset = Config.get("system_charset", "utf-8");
		Map<String, Object> loginCookie = new HashMap<String, Object>(2);
		int ctime = Config.getInt("cookie_time", 1);
		loginCookie.put("id", u.getId());
		loginCookie.put("nick", URLEncoder.encode(u.getNick(), "UTF-8"));
		String userJson = objectMapper.writeValueAsString(loginCookie);
		CookieUtils.createCookie(response, domain, "user", userJson, "/", Integer.MAX_VALUE, false);
		try {
			String des = DESCoder.encrypt(userJson, key, Charset.forName(charset));
			CookieUtils.createCookie(response, domain, "_user", des, "/", ctime * 60 * 60, false);
			return "redirect:" + target;
		} catch (Exception e) {
			log.error(e, e);
			return "redirect:/error";
		}
	}
}
