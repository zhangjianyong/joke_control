package com.doumiao.joke.service.alipay;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class AlipayOauth {
	private static final Log log = LogFactory.getLog(AlipayOauth.class);

	/**
	 * 构造快捷登录接口
	 * 
	 * @param encrypt_key
	 *            防钓鱼时间戳
	 * @param exter_invoke_ip
	 *            买家本地电脑的IP地址
	 * @return 表单提交HTML信息
	 */
	@SuppressWarnings("unchecked")
	public String authorizeUrl(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("service", "alipay.auth.authorize");
		params.put("target_service", "user.auth.quick.login");
		params.put("partner", AlipayConfig.partner);
		params.put("return_url", AlipayConfig.return_url);
		params.put("_input_charset", AlipayConfig.input_charset);
		try {
			// 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数
			// 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
			String strUrl = AlipayConfig.alipay_gateway
					+ "?service=query_timestamp&partner="
					+ AlipayConfig.partner;
			StringBuffer result = new StringBuffer();
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new URL(strUrl).openStream());
			List<Node> nodeList = doc.selectNodes("//alipay/*");
			for (Node node : nodeList) {
				if (node.getName().equals("is_success")
						&& node.getText().equals("T")) {
					List<Node> nodeList1 = doc
							.selectNodes("//response/timestamp/*");
					for (Node node1 : nodeList1) {
						result.append(node1.getText());
					}
				}
			}
			params.put("anti_phishing_key", result.toString());
		} catch (Exception e) {
			log.error(e, e);
			return null;
		}
		params.put("exter_invoke_ip", getIpAddr(request));
		return AlipaySubmit.buildGetUrl(params, AlipayConfig.alipay_gateway,
				new String[] { "anti_phishing_key", "return_url" });
	}

	public Map<String, String> userInfo(HttpServletRequest request) {
		Map<String, String> params = requestParamMap(request);
		boolean verify = AlipayNotify.verify(params);
		if(!verify){
			log.error("alipay verify faild");
			return null;
		}
		return params;
	}

	/**
	 * 获得支付宝用户的基本信息
	 * 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getUserByUid(String uid) throws Exception {
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "user_query");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("user_id", uid);
		String url = AlipaySubmit.buildGetUrl(sParaTemp,
				AlipayConfig.alipay_gateway, null);
		SAXReader reader = new SAXReader();
		Document doc = reader.read(new URL(url).openStream());
		List<Node> nodeList = doc.selectNodes("//alipay/*");
		for (Node node : nodeList) {
			if ("is_success".equals(node.getName())
					&& "T".equals(node.getText())) {
				List<Node> nodeList1 = doc.selectNodes("//response/user/*");
				Map<String, String> resp = new HashMap<String, String>();
				for (Node node1 : nodeList1) {
					resp.put(node1.getName(), node1.getText());
				}
				return resp;
			}
		}

		return null;
	}

	/**
	 * 获得支付宝账户通的绑定url
	 * 
	 * @param egouUserId
	 *            易购用户ID
	 * @param egouUserName
	 *            易购的用户名
	 * @param isBindFromAlipay
	 *            是否从支付宝方面绑定
	 * @param alipayId
	 *            支付宝的ID
	 * @return
	 */
	public String getAccountAssetBindUrl(long egouUserId, String egouUserName,
			boolean isBindFromAlipay, String alipayId) {
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.user.account.asset.bind");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("notify_url", AlipayConfig.notify_url);// 服务器一部通知页面路径
		sParaTemp.put("b_user_id", String.valueOf(egouUserId));
		sParaTemp.put("b_user_name", egouUserName);
		sParaTemp.put("provider_name", "易购网");
		sParaTemp.put("bind_from", isBindFromAlipay ? "alipay" : "provider");
		if (isBindFromAlipay) {
			sParaTemp.put("user_id", alipayId);
		}
		// sParaTemp.put("b_user_grade", "");用户等级，以后可以增加
		return AlipaySubmit.buildGetUrl(sParaTemp, AlipayConfig.alipay_gateway,
				new String[] { "notify_url", "b_user_name", "provider_name" });
	}

	/**
	 * 账户通资产推送
	 * 
	 * @param egouUserId
	 *            易购用户ID
	 * @param alipayId
	 *            支付宝ID
	 * @param categoryName
	 *            资产名字 返现 易币
	 * @param amount
	 *            资产数量词 如2元 3个
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> accountAssetPush(long egouUserId,
			String alipayId, String categoryName, String amount) {
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.user.account.asset.push");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("user_id", alipayId);
		sParaTemp.put("b_user_id", String.valueOf(egouUserId));
		sParaTemp.put("category_name", categoryName);
		sParaTemp.put("status", "VALID");
		sParaTemp.put("amount", amount);
		String strUrl = AlipaySubmit.buildGetUrl(sParaTemp,
				AlipayConfig.alipay_gateway, new String[] { "category_name",
						"amount" });
		Map<String, String> resp = new HashMap<String, String>();
		resp.put("is_success", "F");
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);
			SAXReader reader = new SAXReader();
			Document doc = reader.read(conn.getInputStream());
			List<Node> nodeList = doc.selectNodes("//alipay/*");
			for (Node node : nodeList) {
				resp.put(node.getName(), node.getText());
			}
		} catch (Exception e) {
		}

		return resp;
	}

	/**
	 * 快捷绑定接口
	 * 
	 * @param egouUserId
	 * @param egouUserName
	 * @param alipayId
	 * @param token
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> accountBindPush(long egouUserId,
			String egouUserName, String alipayId, String token) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.user.asset.quick.bind");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("timestamp", sdf.format(new Date()));
		sParaTemp.put("user_id", alipayId); // 支付宝账号ID
		sParaTemp.put("b_user_id", String.valueOf(egouUserId));
		sParaTemp.put("b_user_name", egouUserName);
		sParaTemp.put("token", token);
		String strUrl = AlipaySubmit.buildGetUrl(sParaTemp,
				AlipayConfig.alipay_gateway, new String[] { "timestamp",
						"b_user_name" });
		Map<String, String> resp = new HashMap<String, String>();
		resp.put("is_success", "F");
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);
			SAXReader reader = new SAXReader();
			Document doc = reader.read(conn.getInputStream());
			List<Node> nodeList = doc.selectNodes("//alipay/*");
			for (Node node : nodeList) {
				resp.put(node.getName(), node.getText());
			}
		} catch (Exception e) {
		}

		return resp;
	}

	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("x-forwarded-for");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public Map<String, String> requestParamMap(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter
				.hasNext();) {
			String name = iter.next();
			String[] values = requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		return params;
	}
}
