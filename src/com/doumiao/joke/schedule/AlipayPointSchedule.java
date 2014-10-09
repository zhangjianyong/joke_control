package com.doumiao.joke.schedule;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayPointOrderAddRequest;
import com.alipay.api.response.AlipayPointOrderAddResponse;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;

@Component
public class AlipayPointSchedule {
	private static final Log log = LogFactory.getLog(AlipayPointSchedule.class);
	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(fixedDelay = 60000)
	protected void dealThirdplatAccountLog() {
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
				req.setUserSymbol((String) l.get("account"));
				req.setUserSymbolType("ALIPAY_LOGON_ID");
				req.setPointCount(((Integer)l.get("wealth")).longValue());
				req.setMerchantOrderNo((String) l.get("sub_serial_number"));
				req.setMemo("[" + Config.get("system_website_name") + "]积分兑换");
				req.setOrderTime(new Date());
				AlipayPointOrderAddResponse response = client.execute(req,Config.get("alipay_company_token"));
				if (response.isSuccess()
						|| "isv.out-biz-no-exist" == response.getErrorCode()) {
					jdbcTemplate
							.update("update uc_thirdplat_account_log set status = ? where id=?",
									AccountLogStatus.PAYED.name(), id);
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
}
