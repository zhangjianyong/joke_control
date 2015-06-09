package com.doumiao.joke.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.enums.WealthType;
import com.doumiao.joke.lang.SerialNumberGenerator;
import com.doumiao.joke.web.DealAccount;

@Service
public class AccountService {

	@Resource
	private JdbcTemplate jdbcTemplate;
	private static final Log log = LogFactory.getLog(DealAccount.class);

	/**
	 * 批量平台积分支付
	 * 
	 * @see #pay()
	 * @param logs
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void batchPay(List<Map<String, Object>> accountLogs)
			throws Exception {
		if (accountLogs == null || accountLogs.size() == 0) {
			return;
		}
		int count = accountLogs.size();
		List<AccountLog> _scoreLogs = new ArrayList<AccountLog>(count);
		String[] sn = SerialNumberGenerator.generate(accountLogs.size());
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
			log.setRemark(StringUtils.defaultIfBlank((String) l.get("r"), null));
			log.setOperator(StringUtils.defaultIfBlank((String) l.get("o"),
					null));
			log.setWealthTime((Date) l.get("wt"));
			_scoreLogs.add(log);
		}
		for (AccountLog log : _scoreLogs) {
			pay(log);
		}
	}

	/**
	 * 平台积分支付
	 * 
	 * @param accountLog
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void pay(AccountLog accountLog) throws Exception {

		int balance = jdbcTemplate.queryForInt("select "
				+ accountLog.getAccount().name()
				+ " from uc_account where member_id = ?",
				accountLog.getMemberId());

		balance += accountLog.getWealth();
		if (balance < 0) {
			log.info("balance less than 0,memberId:" + accountLog.getMemberId());
			throw new Exception("余额不足");
		}
		try {
			jdbcTemplate
					.update("insert into uc_account_log "
							+ "(member_id, wealth_type, account, wealth, serial_number, sub_serial_number, remark, status, operator, wealth_balance, wealth_time) "
							+ "values (?,?,?,?,?,?,?,?,?,?,?)", accountLog
							.getMemberId(), accountLog.getWealthType().name(),
							accountLog.getAccount().name(), accountLog
									.getWealth(), accountLog.getSerialNumber(),
							accountLog.getSubSerialNmumber(), accountLog
									.getRemark(),
							accountLog.getStatus().name(), accountLog
									.getOperator(), balance, null);

			jdbcTemplate.update("update uc_account set "
					+ accountLog.getAccount().name()
					+ "= ? where member_id = ?", balance,
					accountLog.getMemberId());
		} catch (Exception e) {
			log.error(e, e);
			throw new Exception("数据库错误");
		}
	}

	/**
	 * 第三方积分支付
	 * 
	 * @see #pay()
	 * @param logs
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void payThirdScore(ThirdPlatAccountLog accountLog)
			throws Exception {
		try {
			// 检查是否有该第三方平台账号
			jdbcTemplate
					.queryForInt(
							"select id from uc_thirdplat_account where member_id = ? and plat = ?",
							accountLog.getMemberId(), accountLog.getPlat()
									.name());
		} catch (EmptyResultDataAccessException erdae) {
			// 如不存在,则添加账号
			jdbcTemplate
					.update("insert into uc_thirdplat_account(member_id, plat, account, total, create_time) values (?,?,?,?,?)",
							accountLog.getMemberId(), accountLog.getPlat()
									.name(), accountLog.getAccount(), 0, null);// null是creat_time字段当插入一条新记录时自动生成与update_time一样的值
		} catch (Exception e) {
			log.error(e, e);
			throw new Exception("数据库错误");
		}
		try {
			// 插入第三方支付流水
			jdbcTemplate
					.update("insert into uc_thirdplat_account_log(member_id, plat, account, wealth, status, serial_number, sub_serial_number, remark, operator, create_time) values (?,?,?,?,?,?,?,?,?,?)",
							accountLog.getMemberId(), Plat.ALIPAY.name(),
							accountLog.getAccount(), accountLog.getWealth(),
							AccountLogStatus.UNPAY// 必须为未支付，第三方积分统一发放
									.name(), accountLog.getSerialNumber(),
							accountLog.getSubSerialNmumber(),
							accountLog.getRemark(), accountLog.getOperator(),
							null);// null是creat_time字段当插入一条新记录时自动生成与update_time一样的值
		} catch (Exception e) {
			log.error(e, e);
			throw new Exception("数据库错误");
		}
	}

	/**
	 * 第三方积分兑换
	 * 
	 * @see #pay()
	 * @param logs
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void exchange(ThirdPlatAccountLog log, AccountLog _log)
			throws Exception {
		pay(_log);
		payThirdScore(log);
	}

	/**
	 * 第三方积分打款失败后,退回积分
	 * 
	 * @param ll
	 * @param logId
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void reject(AccountLog ll, int logId) throws Exception {
		jdbcTemplate.update(
				"update uc_thirdplat_account_log set status = ? where id=?",
				AccountLogStatus.REJECT.name(), logId);
		pay(ll);
	}

	/**
	 * 第三方积分打款成功后,修改更新打款状态及支付总额
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public void afterPay(int logId, int wealth, String account, int memberId) {
		// 打款成功或已打过款,更新打款状态
		jdbcTemplate.update(
				"update uc_thirdplat_account_log set status = ? where id=?",
				AccountLogStatus.PAYED.name(), logId);
		// 打款成功更新最后支付时间及总额
		jdbcTemplate
				.update("update uc_thirdplat_account set total = total + ?, account = ? where member_id = ? and plat = ?",
						wealth, account, memberId, Plat.ALIPAY.name());
	}

	/**
	 * qq平台的重复账户处理
	 */
	@Transactional(timeout = 100000, rollbackForClassName = {
			"RuntimeException", "Exception" }, propagation = Propagation.REQUIRED)
	public void dupQQ() {
		List<String> openids = jdbcTemplate
				.queryForList(
						"SELECT open_id FROM joke.uc_thirdplat_binding u GROUP BY open_id HAVING count(*) > 1",
						String.class);
		if (openids != null && openids.size() > 0) {
			for (String id : openids) {
				List<Integer> mids = jdbcTemplate
						.queryForList(
								"SELECT member_id FROM joke.uc_thirdplat_binding u where open_id = ? and plat = ?",
								new Object[] { id, Plat.QQ.name() },
								Integer.class);
				if (mids != null && mids.size() > 1) {
					Integer l_mid = null;
					Map<Integer, Map<String, Object>> accounts = new HashMap<Integer, Map<String, Object>>(
							mids.size());
					for (Integer mid : mids) {
						Map<String, Object> account = jdbcTemplate.queryForMap(
								"select * from uc_account where member_id = ?",
								mid);
						accounts.put(mid, account);
						if (l_mid == null || mid < l_mid) {
							l_mid = mid;
						}
					}
					int s1 = 0;
					int s2 = 0;
					for (Map<String, Object> account : accounts.values()) {
						s1 += (Integer) account.get("s1");
						s2 += (Integer) account.get("s2");
					}

					for (Integer mid : mids) {
						if (mid != l_mid) {
							jdbcTemplate
									.update("update uc_account_log set member_id = ? where member_id = ?",
											l_mid, mid);
							jdbcTemplate
									.update("update uc_thirdplat_account_log set member_id = ? where member_id = ?",
											l_mid, mid);
							jdbcTemplate
									.update("delete from uc_thirdplat_account where member_id = ?",
											mid);
							jdbcTemplate
									.update("delete from uc_account where member_id = ?",
											mid);
							jdbcTemplate.update(
									"delete from uc_member where id = ?", mid);
							jdbcTemplate
									.update("delete from uc_thirdplat_binding where member_id = ?",
											mid);
						}
					}

					jdbcTemplate
							.update("update uc_account set s1 = ?,s2 = ? where member_id = ?",
									s1, s2, l_mid);
				}
			}
		}
	}
}
