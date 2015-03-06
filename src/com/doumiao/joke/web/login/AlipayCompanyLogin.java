package com.doumiao.joke.web.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.enums.WealthType;
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
		} catch (AlipayApiException e) {
			log.error(e, e);
			return "/error";
		}
		return "redirect:/alipay_company_view";
	}

	@RequestMapping("/alipay_company_view")
	public String view(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		AlipayClient client = new DefaultAlipayClient(Config.get("alipay_url"),
				Config.get("alipay_company_appid"),
				Config.get("alipay_company_private_key"), "json");
		AlipayPointBudgetGetRequest reqb = null;
		AlipayPointBudgetGetResponse resb = null;
		try {
			AlipayPointBalanceGetRequest req = new AlipayPointBalanceGetRequest();
			log.debug("get alipay point start");
			AlipayPointBalanceGetResponse res = client.execute(req,
					Config.get("alipay_company_token"));
			request.setAttribute("pointAmount", res.getPointAmount());
			reqb = new AlipayPointBudgetGetRequest();
			
			resb = client.execute(reqb, Config.get("alipay_company_token"));
			log.debug("get alipay point end");
			if (!resb.isSuccess()) {
				if (resb.getSubCode().equals("aop.invalid-auth-token")) {
					return "redirect:/alipay_company_login";
				} else {
					request.setAttribute(
							"result",
							new Result(false, resb.getSubCode(), resb
									.getSubMsg(), null));
					return "/error";
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			request.setAttribute(
					"result",
					new Result(false, "faild", e.getMessage(), e
							.getStackTrace()));
			return "/error";
		}
		request.setAttribute("budgetAmount", resb.getBudgetAmount());
		request.setAttribute("config", Config.getConfig());

		Map<String, Object> info = jdbcTemplate
				.queryForMap(
						"select sum(wealth)/100 s, count(1) c from `uc_thirdplat_account_log` where `status`=? and plat=?",
						AccountLogStatus.UNPAY.name(), Plat.ALIPAY.name());

		int payed = jdbcTemplate
				.queryForInt(
						"select sum(total)/100 from `uc_thirdplat_account` where plat=?",
						Plat.ALIPAY.name());

		Map<String, Object> day = jdbcTemplate
				.queryForMap(
						"select count(distinct member_id) m,count(1) c,sum(wealth)/100*1.1 s from `uc_account_log` where account = ? and wealth_type = ? and to_days(create_time)=to_days(now())",
						Account.S2.name(), WealthType.DRAW.name());

		List<Map<String, Object>> logs = jdbcTemplate
				.queryForList(
						"select * from `uc_thirdplat_account_log` where `status`=? and plat=? order by create_time asc",
						AccountLogStatus.UNPAY.name(), Plat.ALIPAY.name());

		request.setAttribute("info", info);
		request.setAttribute("payed", payed);
		request.setAttribute("logs", logs);
		request.setAttribute("day", day);
		return "/alipay_company_view";
	}
}
