package com.doumiao.joke.web;

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayPointOrderAddRequest;
import com.alipay.api.response.AlipayPointOrderAddResponse;
import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.enums.WealthType;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.service.AccountLog;
import com.doumiao.joke.service.AccountService;

@Controller
public class AlipayPoint {
	private static final Log log = LogFactory.getLog(AlipayPoint.class);
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private AccountService accountService;

	@RequestMapping("/alipaypoint")
	public synchronized void alipayPoint() {
		if (log.isDebugEnabled()) {
			log.debug("deal alipay point log start");
		}
		AlipayClient client = new DefaultAlipayClient(Config.get("alipay_url"),
				Config.get("alipay_company_appid"),
				Config.get("alipay_company_private_key"), "json");
		AlipayPointOrderAddRequest req = new AlipayPointOrderAddRequest();
		List<Map<String, Object>> logs = jdbcTemplate
				.queryForList(
						"select * from uc_thirdplat_account_log where `status`=? and plat=? order by create_time asc",
						AccountLogStatus.UNPAY.name(), Plat.ALIPAY.name());
		for (Map<String, Object> l : logs) {
			long start = System.currentTimeMillis();
			int tryCount = 0;
			boolean success = false;
			while (tryCount < 3 && !success) {
				try {
					int id = (int) l.get("id");
					int memberId = (int) l.get("member_id");
					String account = (String) l.get("account");
					Integer wealth = (Integer) l.get("wealth");
					String sn = (String) l.get("serial_number");
					String ssn = (String) l.get("sub_serial_number");

					req.setUserSymbol(account);
					req.setUserSymbolType("ALIPAY_LOGON_ID");
					req.setPointCount(wealth.longValue());
					req.setMerchantOrderNo(ssn);
					req.setMemo("[" + Config.get("system_website_name")
							+ "]积分兑换");
					req.setOrderTime(new Date());

					// 如果账号有误,退回积分,更新状态为
					AccountLog ll = new AccountLog();
					ll.setMemberId(memberId);
					ll.setAccount(Account.S2);
					ll.setWealthType(WealthType.REFUND);
					ll.setWealth(wealth);
					ll.setStatus(AccountLogStatus.PAYED);
					ll.setSerialNumber(sn);
					ll.setSubSerialNmumber(ssn + "r");
					ll.setOperator("system");

					AlipayPointOrderAddResponse response = client.execute(req,
							Config.get("alipay_company_token"));
					String subCode = response.getSubCode();
					if (response.isSuccess()
							|| "isv.out_biz_no_exist".equals(subCode)) {
						accountService.afterPay(id, wealth, account, memberId);
					} else if ("isp.no_exist_user".equals(subCode)) {
						ll.setRemark("第三方账号不存在(" + account + ")");
						accountService.reject(ll, id);
					} else if ("isp.cif_card_freeze".equals(subCode)) {
						ll.setRemark("第三方账号被冻结(" + account + ")");
						accountService.reject(ll, id);
					} else if ("isp.budgetcore_invoke_error".equals(subCode)
							|| "aop.invalid-auth-token".equals(subCode)) {
						log.error("alipay point break");
						break;
					} else {
						log.error(sn + ":" + response.getSubCode());
					}
					success = true;
					log.debug("point:" + (System.currentTimeMillis() - start));
				} catch(SocketTimeoutException ste){
					tryCount++;
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}
}
