package com.doumiao.joke.web;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;
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

	/**
	 * 发放平台积分
	 * 
	 * @param request
	 * @param response
	 * @param json
	 * @return #Result
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/pay", method = RequestMethod.POST)
	public synchronized Result pay(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "accountLog", required = false) String accountLog) {
		try {
			List<AccountLog> _scoreLogs = ListUtils.EMPTY_LIST;
			if (StringUtils.isNotBlank(accountLog)) {
				List<Map<String, Object>> accountLogs = objectMapper.readValue(
						accountLog, List.class);
				int count = accountLogs.size();
				_scoreLogs = new ArrayList<AccountLog>(count);
				String[] sn = SerialNumberGenerator
						.generate(accountLogs.size());
				for (int i = 0; i < count; i++) {
					Map<String, Object> l = accountLogs.get(i);
					// 组装并验证数据合法性
					AccountLog log = new AccountLog();
					log.setMemberId((Integer) l.get("u"));
					log.setAccount(Account.valueOf((String) l.get("a")));
					log.setWealthType(WealthType.valueOf((String) l.get("t")));
					log.setWealth((Integer) l.get("w"));
					log.setStatus(AccountLogStatus.valueOf((String) l.get("s")));
					log.setSerialNumber(sn[0]);
					log.setSubSerialNmumber(sn[i + 1]);
					log.setRemark(StringUtils.defaultIfBlank(
							(String) l.get("r"), null));
					log.setOperator(StringUtils.defaultIfBlank(
							(String) l.get("o"), null));
					log.setWealthTime((Date) l.get("wt"));
					_scoreLogs.add(log);
				}
			}

			if (_scoreLogs.size() > 0) {
				accountService.batchPay(_scoreLogs);
			} else {
				return new Result(false, "param.error", "系统错误", null);
			}
			return new Result(true, "success", "交易成功", null);
		} catch (Exception e) {
			log.error(e, e);
			return new Result(false, "faild", "交易失败", e.getMessage());
		}
	}

	/**
	 * 对换集分宝
	 * 
	 * @param request
	 * @param response
	 * @param uid
	 * @param account
	 * @param wealth
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/exchange")
	public Result alipayExchange(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "uid") final int uid,
			@RequestParam(value = "account") final String account,
			@RequestParam(value = "wealth") final int wealth,
			@RequestParam(value = "plat") final String plat) {
		if (StringUtils.isBlank(account)) {
			return new Result(false, "faild", "账号不能为空", null);
		}
		if (wealth <= 0) {
			return new Result(false, "faild", "兑换积分不能小于等于零", null);
		}
		Plat p = Plat.valueOf(plat);
		try {
			// 生成中奖流水
			String[] serialNumber = SerialNumberGenerator.generate(2);
			AccountLog log = new AccountLog(uid, WealthType.THIRDPLAT_EXCHANGE,
					Account.S2, -wealth, serialNumber[0], serialNumber[1],
					null, AccountLogStatus.PAYED, "system", null);
			ThirdPlatAccountLog _log = new ThirdPlatAccountLog(uid, p, account,
					wealth, serialNumber[0], serialNumber[1], null,
					AccountLogStatus.UNPAY, "system");
			accountService.exchange(_log, log);
		} catch (Exception e) {
			return new Result(false, "faild", e.getMessage(), null);
		}
		return new Result(true, "success", "对换成功", null);
	}
}
