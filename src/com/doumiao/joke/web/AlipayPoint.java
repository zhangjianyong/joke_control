package com.doumiao.joke.web;

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
						"select * from uc_thirdplat_account_log where status=? and plat=?",
						AccountLogStatus.UNPAY.name(), Plat.ALIPAY.name());
		for (Map<String, Object> l : logs) {
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
				req.setMemo("[" + Config.get("system_website_name") + "]积分兑换");
				req.setOrderTime(new Date());
				AlipayPointOrderAddResponse response = client.execute(req,
						Config.get("alipay_company_token"));
				if (response.isSuccess()
						|| "isv.out-biz-no-exist" == response.getErrorCode()) {
					// 打款成功或已打过款,更新打款状态
					jdbcTemplate
							.update("update uc_thirdplat_account_log set status = ? where id=?",
									AccountLogStatus.PAYED.name(), id);
					// 打款成功更新最后支付时间及总额
					jdbcTemplate
							.update("update uc_thirdplat_account set total = total + ?, update_time = ?, account = ? where id = ?",
									wealth, null, account, id);
				} else if (response.getSubCode().equals("isp.no_exist_user")) {
					// 如果账号不存在,退回积分,更新状态为
					AccountLog log = new AccountLog();
					log.setMemberId(memberId);
					log.setAccount(Account.S2);
					log.setWealthType(WealthType.REFUND);
					log.setWealth(-wealth);
					log.setStatus(AccountLogStatus.PAYED);
					log.setSerialNumber(sn);
					log.setSubSerialNmumber(ssn + "r");
					log.setRemark("第三方账号错误,积分退回");
					log.setOperator("system");
					accountService.pay(log);
					
					jdbcTemplate
					.update("update uc_thirdplat_account_log set status = ? where id=?",
							AccountLogStatus.REJECT.name(), id);
				}else if(response.getSubCode().equals("isp.budgetcore_invoke_error")){
					break;
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
}
