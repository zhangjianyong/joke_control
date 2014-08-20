package com.doumiao.joke.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;

@Service
public class AccountService {

	@Resource
	private JdbcTemplate jdbcTemplate;

	/**
	 * 批量积分支付
	 * 
	 * @see #pay()
	 * @param logs
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void batchPay(List<AccountLog> logs) throws Exception {
		for (AccountLog log : logs) {
			pay(log);
		}
	}

	/**
	 * 积分支付
	 * 
	 * @param log
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void pay(AccountLog log) throws Exception {

		int balance = jdbcTemplate.queryForInt("select "
				+ log.getAccount().name()
				+ " from uc_account where member_id = ?", log.getMeberId());

		// 计算本次后账户余额
		if (AccountLogStatus.PAY.equals(log.getStatus())) {
			balance += log.getWealth();
		} else if (AccountLogStatus.UNPAY.equals(log.getStatus())) {
			throw new Exception("uncheck log isn't allowed");
		}

		if (balance < 0) {
			throw new Exception("balance less than 0,memberId:"
					+ log.getMeberId());
		}

		jdbcTemplate
				.update("insert into uc_account_log "
						+ "(member_id, wealth_type, account, wealth, serial_number, sub_serial_number, remark, status, operator, wealth_balance, wealth_time) "
						+ "values (?,?,?,?,?,?,?,?,?,?,?)", log.getMeberId(),
						log.getwealthType().name(), log.getAccount().name(),
						log.getWealth(), log.getSerialNumber(), log
								.getSubSerialNmumber(), log.getRemark(), log
								.getStatus().name(), log.getOperator(),
						balance, null);

		jdbcTemplate.update("update uc_account set " + log.getAccount().name()
				+ "= ? where member_id = ?", balance, log.getMeberId());
	}

	/**
	 * 打集分宝申请
	 * 
	 * @see #pay()
	 * @param logs
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void pay_jfb(ThirdPlatAccountLog log) throws Exception {
		try {
			// 检查是否有该账号
			int id = jdbcTemplate
					.queryForInt(
							"select id from uc_thirdplat_account where member_id = ? and plat=? and account=?",
							log.getMeberId(), Plat.ALIPAY.name(),
							log.getAccount());
			// 账号存在,则更新最后支付时间
			jdbcTemplate
					.update("update uc_thirdplat_account set wealth = wealth+?, update_time = null where id=?",
							log.getWealth(), id);
		} catch (EmptyResultDataAccessException erdae) {
			// 如不存在,则添加账号
			jdbcTemplate
					.update("insert into uc_thirdplat_account(member_id, plat, account, wealth, create_time) values (?,?,?,?,?)",
							log.getMeberId(), Plat.ALIPAY.name(),
							log.getAccount(), log.getWealth(), null);// null是creat_time字段当插入一条新记录时自动生成与update_time一样的值
		}
		// 插入第三方支付流水
		jdbcTemplate
				.update("insert into uc_thirdplat_account_log(member_id, plat, account, wealth, status, serial_number, sub_serial_number, remark, operator, create_time) values (?,?,?,?,?,?,?,?,?,?)",
						log.getMeberId(), Plat.ALIPAY.name(), log.getAccount(),
						log.getWealth(), log.getStatus().name(),
						log.getSerialNumber(), log.getSubSerialNmumber(),
						log.getRemark(), log.getOperator(), null);// null是creat_time字段当插入一条新记录时自动生成与update_time一样的值
	}

	/**
	 * 集分宝支付
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void addPoint(int amount, String alipayName,
			String outBizNo, String alipayUserId, String createTime,
			String dispatchConfigId) throws Exception {

	}

	/**
	 * 积分对换集分宝
	 * 
	 * @see #pay()
	 * @param logs
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized void exchangeJFB(ThirdPlatAccountLog log,
			AccountLog _log) throws Exception {
		pay(_log);
		pay_jfb(log);
	}
}
