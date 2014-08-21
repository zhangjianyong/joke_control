package com.doumiao.joke.web;

import java.sql.Date;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.enums.WealthType;
import com.doumiao.joke.lang.SerialNumberGenerator;
import com.doumiao.joke.service.AccountLog;
import com.doumiao.joke.service.AccountService;
import com.doumiao.joke.service.ThirdPlatAccountLog;
import com.doumiao.joke.vo.Result;

@Controller
public class DealAccount {
	@Resource
	private ObjectMapper objectMapper;

	@Resource
	private AccountService accountService;
	private static final Log log = LogFactory.getLog(DealAccount.class);

	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/pay", method = RequestMethod.POST)
	public synchronized Result dealAccount(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "json") String json) {
		try {
			List<Map<String, Object>> logs = objectMapper.readValue(json,
					List.class);
			List<AccountLog> _logs = new ArrayList<AccountLog>(logs.size());
			for (Map<String, Object> l : logs) {
				// 组装并验证数据合法性
				AccountLog log = new AccountLog();
				log.setMeberId((Integer) l.get("u"));
				log.setAccount(Account.valueOf((String) l.get("a")));
				log.setwealthType(WealthType.valueOf((String) l.get("t")));
				log.setWealth((Integer) l.get("w"));
				log.setStatus(AccountLogStatus.valueOf((String) l.get("s")));
				log.setSerialNumber(StringUtils.defaultIfBlank((String) l.get("sn"), null));
				log.setSubSerialNmumber(StringUtils.defaultIfBlank((String) l.get("ssn"), null));
				log.setRemark(StringUtils.defaultIfBlank((String) l.get("r"), null));
				log.setOperator(StringUtils.defaultIfBlank((String) l.get("o"), null));
				log.setWealthTime((Date) l.get("wt"));
				_logs.add(log);
				
			}
			// 支付
			accountService.batchPay(_logs);
			return new Result(true, "success", "交易成功", null);
		} catch (Exception e) {
			log.error(e, e);
			return new Result(false, "faild", "交易失败", e.getMessage());
		}
	}
	
	/**
	 * 对换集分宝
	 * @param request
	 * @param response
	 * @param uid
	 * @param account
	 * @param wealth
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/alipay_exchange")
	public Result alipayExchange(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "uid") final int uid,
			@RequestParam(value = "account") final String account,
			@RequestParam(value = "wealth") final int wealth) {
		try {
			// 生成中奖流水
			String[] serialNumber = SerialNumberGenerator.generate(3);
			AccountLog log = new AccountLog(uid, WealthType.THIRDPLAT_EXCHANGE,
					Account.S2, -wealth, serialNumber[0], serialNumber[1], null,
					AccountLogStatus.PAY, "system", null);
			ThirdPlatAccountLog _log = new ThirdPlatAccountLog(uid,
					Plat.ALIPAY, account, wealth, serialNumber[0],
					serialNumber[2], null, AccountLogStatus.UNPAY, "system");
			accountService.exchangeJFB(_log, log);
		} catch (Exception e) {
			log.error(e, e);
			return new Result(true, "faild", "对换失败", "");
		}
		return new Result(true, "success", "对换成功", "");
	}
}