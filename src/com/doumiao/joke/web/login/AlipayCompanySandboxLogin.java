package com.doumiao.joke.web.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.alipay.api.request.AlipayPointBudgetGetRequest;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipayPointBudgetGetResponse;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.doumiao.joke.lang.Function;
import com.doumiao.joke.service.MemberService;

@Controller
public class AlipayCompanySandboxLogin {
	private static final Log log = LogFactory
			.getLog(AlipayCompanySandboxLogin.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private MemberService memberService;

	@Resource
	private ObjectMapper objectMapper;

	private String appkey = "top.1023017526";
//	private String appSecret = "sandbox0308f3ed366c3916c6948a13f";
	private String privatekey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMiAec6fsssguUoRN3oEVEnQaqBLZjeafXAxCbKH3MTJaXPmnXOtqFFqFtcB8J9KqyFI1+o6YBDNIdFWMKqOwDDWPKqtdo90oGav3QMikjGYjIpe/gYYCQ/In/oVMVj326GmKrSpp0P+5LNCx59ajRpO8//rnOLd6h/tNxnfahanAgMBAAECgYEAusouMFfJGsIWvLEDbPIhkE7RNxpnVP/hQqb8sM0v2EkHrAk5wG4VNBvQwWe2QsAuY6jYNgdCPgTNL5fLaOnqkyy8IobrddtT/t3vDX96NNjHP4xfhnMbpGjkKZuljWKduK2FAh83eegrSH48TuWS87LjeZNHhr5x4C0KHeBTYekCQQD5cyrFuKua6GNG0dTj5gA67R9jcmtcDWgSsuIXS0lzUeGxZC4y/y/76l6S7jBYuGkz/x2mJaZ/b3MxxcGQ01YNAkEAzcRGLTXgTMg33UOR13oqXiV9cQbraHR/aPmS8kZxkJNYows3K3umNVjLhFGusstmLIY2pIpPNUOho1YYatPGgwJBANq8vnj64p/Hv6ZOQZxGB1WksK2Hm9TwfJ5I9jDu982Ds6DV9B0L4IvKjHvTGdnye234+4rB4SpGFIFEo+PXLdECQBiOPMW2cT8YgboxDx2E4bt8g9zSM5Oym2Xeqs+o4nKbcu96LipNRkeFgjwXN1708QuNNMYsD0nO+WIxqxZMkZsCQHtS+Jj/LCnQZgLKxXZAllxqSTlBln2YnBgk6HqHLp8Eknx2rUXhoxE1vD9tNmom6PiaZlQyukrQkp5GOMWDMkU=";
	private String apiurl = "https://openapi.alipaydev.com/gateway.do";

	@RequestMapping("/alipay_company_sandbox_login")
	public String login(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			String url = "http://openauth.alipaydev.com/oauth2/authorize.htm";
			List<String[]> params = new ArrayList<String[]>();
			params.add(new String[] { "client_id", appkey });// String|是|客户端标识符,
																// 等同与appkey
																// params.add(new
																// String[] {
																// "redirect_uri",
			// "http://control.yixiaoqianjin.com/alipay_company_sandbox_callback"
			// });// String|否|url授权的回调地址,为空时用应用的callback_url
//			params.add(new String[] { "scope", "p" });// String|否|空或者p|访问请求的作用域，需要支付授权时传p
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

	@RequestMapping("/alipay_company_sandbox_callback")
	public String callback(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "code") String code) {
		AlipayClient client = new DefaultAlipayClient(apiurl, appkey,
				privatekey, "json");
		AlipaySystemOauthTokenRequest req = new AlipaySystemOauthTokenRequest();
		req.setGrantType("authorization_code");
		req.setCode(code);
		try {
			AlipaySystemOauthTokenResponse alipayResponse = client.execute(req);
			request.getSession().setAttribute("alipay_company_sandbox_token",
					alipayResponse.getAccessToken());
		} catch (AlipayApiException e) {
			log.error(e, e);
			return "redirect:/error";
		}
		return "redirect:/alipay_company_view";
	}

	@RequestMapping("/alipay_company_sandbox_view")
	public String view(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Object token = request.getSession().getAttribute(
				"alipay_company_sandbox_token");
		if (token == null) {
			return "redirect:/alipay_company_sandbox_login";
		}
		AlipayClient client = new DefaultAlipayClient(apiurl, appkey,
				privatekey, "json");
		AlipayPointBudgetGetRequest req = new AlipayPointBudgetGetRequest();
		AlipayPointBudgetGetResponse res = client.execute(req, (String) token);
		request.setAttribute("pointAmount", res.getBudgetAmount());
		return "/alipay_company_sandbox_view";
	}
}
