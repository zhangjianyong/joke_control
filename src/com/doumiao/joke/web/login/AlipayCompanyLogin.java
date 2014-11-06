package com.doumiao.joke.web.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayPointBalanceGetRequest;
import com.alipay.api.request.AlipayPointBudgetGetRequest;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipayPointBalanceGetResponse;
import com.alipay.api.response.AlipayPointBudgetGetResponse;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.lang.Function;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.service.MemberService;
import com.doumiao.joke.vo.Result;

@Controller
public class AlipayCompanyLogin {
	private static final Log log = LogFactory.getLog(AlipayCompanyLogin.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private MemberService memberService;

	@Resource
	private ObjectMapper objectMapper;

	@RequestMapping("/alipay_company_login")
	public String login(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "scope", required = false) String scope)
			throws IOException {
		try {
			String url = "https://openauth.alipay.com/oauth2/authorize.htm";
			List<String[]> params = new ArrayList<String[]>();
			params.add(new String[] { "client_id",
					Config.get("alipay_company_appid") });// String|是|客户端标识符,
			// 等同与appkey
			// params.add(new String[] { "redirect_uri",
			// "http://control.yixiaoqianjin.com/alipay_company_callback" });//
			// String|否|url授权的回调地址,为空时用应用的callback_url
			params.add(new String[] { "scope", "p" });// String|否|空或者p|访问请求的作用域，需要支付授权时传p
			// params.add(new String[] { "state", "" });//
			// String|否|维持应用的状态，此参数授权成功后会原样带回.
			// params.add(new String[] { "view", "" });//
			// String|否|空或者wap|授权页面的视图类型,PC上使用时传空,wap版本授权时传入wap.
			url = Function.joinUrl(url, params);
			return "redirect:" + url;
		} catch (Exception e) {
			log.error(e, e);
			return "redirect:/error";
		}
	}

	@RequestMapping("/alipay_company_callback")
	public String callback(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "code", required = false) String code) {
		if (StringUtils.isBlank(code)) {
			request.setAttribute("result", new Result(false, "params.invalid",
					"alipay.company.callback.code.empty", null));
			return "/error";
		}
		AlipayClient client = new DefaultAlipayClient(Config.get("alipay_url"),
				Config.get("alipay_company_appid"),
				Config.get("alipay_company_private_key"), "json");
		AlipaySystemOauthTokenRequest req = new AlipaySystemOauthTokenRequest();
		req.setGrantType("authorization_code");
		req.setCode(code);
		try {
			AlipaySystemOauthTokenResponse alipayResponse = client.execute(req);
			Config.set("alipay_company_expires", alipayResponse.getExpiresIn());
			Config.set("alipay_company_user_id",
					alipayResponse.getAlipayUserId());
			Config.set("alipay_company_token", alipayResponse.getAccessToken());
			Config.set("alipay_company_refresh_token",
					alipayResponse.getRefreshToken());
			request.getSession().setAttribute("alipay_company_user", true);
		} catch (AlipayApiException e) {
			log.error(e, e);
			return "/error";
		}
		return "redirect:/alipay_company_view";
	}

	@RequestMapping("/alipay_company_view")
	public String view(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (request.getSession().getAttribute("alipay_company_user") == null) {
			return "redirect:/alipay_company_login";
		}
		AlipayClient client = new DefaultAlipayClient(Config.get("alipay_url"),
				Config.get("alipay_company_appid"),
				Config.get("alipay_company_private_key"), "json");
		AlipayPointBalanceGetRequest req = new AlipayPointBalanceGetRequest();
		AlipayPointBalanceGetResponse res = client.execute(req,
				Config.get("alipay_company_token"));
		request.setAttribute("pointAmount", res.getPointAmount());
		AlipayPointBudgetGetRequest reqb = new AlipayPointBudgetGetRequest();
		AlipayPointBudgetGetResponse resb = client.execute(reqb,
				Config.get("alipay_company_token"));
		request.setAttribute("budgetAmount", resb.getBudgetAmount());
		request.setAttribute("config", Config.getConfig());

		int unpay = jdbcTemplate
				.queryForInt(
						"select sum(wealth) from `uc_thirdplat_account_log` where status=? and plat=?",
						AccountLogStatus.UNPAY.name(), Plat.ALIPAY.name());

		int payed = jdbcTemplate.queryForInt(
				"select sum(total) from `uc_thirdplat_account` where plat=?",
				Plat.ALIPAY.name());
		request.setAttribute("unpay", unpay);
		request.setAttribute("payed", payed);
		return "/alipay_company_view";
	}
}
