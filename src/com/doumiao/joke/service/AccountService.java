package com.doumiao.joke.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;
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
	public synchronized void batchPay(List<AccountLog> accountLogs)
			throws Exception {
		for (AccountLog log : accountLogs) {
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
			int id = jdbcTemplate
					.queryForInt(
							"select id from uc_thirdplat_account where member_id = ? and plat = ?",
							accountLog.getMemberId(), accountLog.getPlat()
									.name());
			// 账号存在,则更新最后支付时间
			jdbcTemplate
					.update("update uc_thirdplat_account set total = total + ?, update_time = ?, account = ? where id = ?",
							accountLog.getWealth(), null,
							accountLog.getAccount(), id);
		} catch (EmptyResultDataAccessException erdae) {
			// 如不存在,则添加账号
			jdbcTemplate
					.update("insert into uc_thirdplat_account(member_id, plat, account, total, create_time) values (?,?,?,?,?)",
							accountLog.getMemberId(), accountLog.getPlat()
									.name(), accountLog.getAccount(),
							accountLog.getWealth(), null);// null是creat_time字段当插入一条新记录时自动生成与update_time一样的值
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
}
