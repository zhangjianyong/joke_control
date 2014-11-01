package com.doumiao.joke.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.WealthType;

@Component
public class AcountSchedule {
	private static final Log log = LogFactory.getLog(AcountSchedule.class);
	@Resource
	private DataSource dataSource;

	//@Scheduled(fixedDelay = 60000)
	protected void dealTmpAccountLog() {
		if (log.isDebugEnabled()) {
			log.debug("deal account tmp log start");
		}
		Connection con = null;
		try {
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			__dealDelayAccountLog(con);
			con.commit();
		} catch (Exception e) {
			log.error(e, e);
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException e1) {
					log.error(e1, e1);
				}
			}
		} finally {
			JdbcUtils.closeConnection(con);
		}
		if (log.isDebugEnabled()) {
			log.debug("deal account tmp log end");
		}
	}

	private void __dealDelayAccountLog(Connection con) throws SQLException {
		final int BUF_SIZE = 500;
		Map<Integer, Map<Account, Integer>> account_map = new HashMap<Integer, Map<Account, Integer>>();
		PreparedStatement stmt_uc_insert = null;
		PreparedStatement stmt_uc_update = null;
		PreparedStatement stmt_uc_account = null;
		Statement stmt = null, stmt_delete = null;
		ResultSet rs = null, rs_account = null;
		try {
			stmt_uc_insert = con
					.prepareStatement("insert into uc_account_log "
							+ "(member_id, wealth_type, account, wealth, serial_number, sub_serial_number, remark, wealth_time, status, operator, wealth_balance) "
							+ "values (?,?,?,?,?,?,?,?,?,?,?)");
			stmt_uc_account = con
					.prepareStatement("select s1, s2, s3, s4, s5, s6, s7 from uc_account where member_id = ?");
			stmt_uc_update = con
					.prepareStatement("update uc_account set s1 = ?, s2 = ?, s3 = ?, s4 = ?, s5 = ?, s6 = ?, s7 = ? where member_id = ?");
			stmt = con.createStatement();
			stmt_delete = con.createStatement();
			rs = stmt
					.executeQuery("select * from uc_account_log_tmp order by id asc");
			List<Integer> log_ids = new ArrayList<Integer>(BUF_SIZE);
			int i = 0;
			while (rs.next()) {
				int tmpId = rs.getInt("id");
				log_ids.add(tmpId);
				int memberId = rs.getInt("member_id");
				WealthType wealthType = WealthType.valueOf(rs
						.getString("wealth_type"));
				int wealth = rs.getInt("wealth");
				int delayHours = rs.getInt("delay_hours");
				Timestamp createTime = rs.getTimestamp("create_time");
				AccountLogStatus status = AccountLogStatus.valueOf(rs
						.getString("status"));
				Map<Account, Integer> money = account_map.get(memberId);
				if (money == null) {
					stmt_uc_account.setLong(1, memberId);
					rs_account = stmt_uc_account.executeQuery();
					rs_account.next();
					money = new HashMap<Account, Integer>(2);
					money.put(Account.S1, rs_account.getInt("s1"));
					money.put(Account.S2, rs_account.getInt("s2"));
					money.put(Account.S3, rs_account.getInt("s3"));
					money.put(Account.S4, rs_account.getInt("s4"));
					money.put(Account.S5, rs_account.getInt("s5"));
					money.put(Account.S6, rs_account.getInt("s6"));
					money.put(Account.S7, rs_account.getInt("s7"));
					rs_account.close();
					account_map.put(memberId, money);
				}
				String account = rs.getString("account");
				Account accountE = Account.valueOf(account);
				int balance_before = money.get(accountE);
				int balance = balance_before;

				if (status.equals(AccountLogStatus.REJECT)) {
				} else if (status.equals(AccountLogStatus.PAYED)) {
					balance += wealth;
				} else if (status.equals(AccountLogStatus.UNPAY)) {
					Calendar now = Calendar.getInstance();
					now.add(Calendar.HOUR, -delayHours);
					if (createTime.after(now.getTime())) {
						continue;
					} else {
						balance += wealth;
						status = AccountLogStatus.PAYED;
					}
				} else {
					log.error("uc_account_log_tmp is error,tmplogid:" + tmpId);
					continue;
				}
				if (balance < 0) {
					log.warn("balance less than 0,memberId:" + memberId);
					break;
				}
				money.put(accountE, balance);
				int col = 0;
				stmt_uc_insert.setInt(++col, memberId);
				stmt_uc_insert.setString(++col, wealthType.name());
				stmt_uc_insert.setString(++col, account);
				stmt_uc_insert.setInt(++col, wealth);
				stmt_uc_insert.setString(++col, rs.getString("serial_number"));
				stmt_uc_insert.setString(++col,
						rs.getString("sub_serial_number"));
				stmt_uc_insert.setString(++col, rs.getString("remark"));
				stmt_uc_insert.setTimestamp(++col, createTime);
				stmt_uc_insert.setString(++col, status.name());
				stmt_uc_insert.setString(++col, rs.getString("operator"));

				stmt_uc_insert.setInt(++col, balance);
				stmt_uc_insert.addBatch();
				if (++i >= BUF_SIZE) {
					__dealDelayAccountLog_execute_batch(con, account_map,
							stmt_uc_insert, stmt_uc_update, stmt_delete,
							log_ids);
					i = 0;
				}
			}

			if (i > 0) {
				__dealDelayAccountLog_execute_batch(con, account_map,
						stmt_uc_insert, stmt_uc_update, stmt_delete, log_ids);
			}

		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeResultSet(rs_account);
			JdbcUtils.closeStatement(stmt);
			JdbcUtils.closeStatement(stmt_delete);
			JdbcUtils.closeStatement(stmt_uc_insert);
			JdbcUtils.closeStatement(stmt_uc_account);
			JdbcUtils.closeStatement(stmt_uc_update);
		}
	}

	private void __dealDelayAccountLog_execute_batch(Connection con,
			Map<Integer, Map<Account, Integer>> account_map,
			PreparedStatement stmt_uc_insert, PreparedStatement stmt_uc_update,
			Statement stmt_delete, List<Integer> log_ids) throws SQLException {
		int[] uc_batch_result = stmt_uc_insert.executeBatch();
		String ids_joined = StringUtils.join(log_ids, ',');
		if (log.isDebugEnabled()) {
			log.debug("__dealDealyAccountLog_execute_batch: "
					+ Arrays.toString(uc_batch_result)
					+ ", ids will be deleted: " + ids_joined);
		}
		int effect_count = stmt_delete
				.executeUpdate("delete from uc_account_log_tmp where id in ("
						+ ids_joined + ")");
		if (log_ids.size() != effect_count) {
			throw new RuntimeException("delete effect count ne ids.size");
		}
		log_ids.clear();
		if (account_map.size() > 0) {
			for (Entry<Integer, Map<Account, Integer>> entry : account_map
					.entrySet()) {
				Map<Account, Integer> ps = entry.getValue();
				stmt_uc_update.setInt(1, ps.get(Account.S1));
				stmt_uc_update.setInt(2, ps.get(Account.S2));
				stmt_uc_update.setInt(3, ps.get(Account.S3));
				stmt_uc_update.setInt(4, ps.get(Account.S4));
				stmt_uc_update.setInt(5, ps.get(Account.S5));
				stmt_uc_update.setInt(6, ps.get(Account.S6));
				stmt_uc_update.setInt(7, ps.get(Account.S7));
				stmt_uc_update.setLong(8, entry.getKey());
				stmt_uc_update.addBatch();
			}
			stmt_uc_update.executeBatch();
			account_map.clear();
		}
		con.commit();
	}
}
