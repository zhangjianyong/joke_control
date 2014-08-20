package com.doumiao.joke.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.doumiao.joke.enums.Plat;
import com.doumiao.joke.lang.Member;

@Service
public class MemberService {

	@Resource
	private JdbcTemplate jdbcTemplate;

	public Member createMember(final Member u) {
		final String sql = "insert into uc_member(name, nick, email, mobile, password, status, remark) VALUES(?,?,?,?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(
					Connection connection) throws SQLException {
				PreparedStatement ps = (PreparedStatement) connection
						.prepareStatement(sql, new String[] { "id" });
				int i = 0;
				ps.setString(++i, u.getName());
				ps.setString(++i, u.getNick());
				ps.setString(++i, u.getEmail());
				ps.setString(++i, u.getMobile());
				ps.setString(++i, u.getPassword());
				ps.setInt(++i, u.getStatus());
				ps.setString(++i, u.getRemark());
				return ps;
			}
		}, keyHolder);
		int id = keyHolder.getKey().intValue();
		jdbcTemplate.update("insert into uc_account(member_id) VALUES(?)",
				new Object[] { id });
		u.setId(id);
		return u;
	}

	/**
	 * 目前网站只支持第三方登录,所以先创建一个空用户,只有id,而后再绑定第三方
	 * 
	 * @param u
	 *            空用户
	 * @param params
	 *            第三方绑定信息
	 * @return 返回绑定后的用户
	 */
	public Member bindThirdPlat(Member u, Plat plat, String openId,
			String token, Map<String, String> params) {
		u = createMember(u);
		if (u == null) {
			return null;
		}
		jdbcTemplate
				.update("insert into uc_thirdplat_binding(member_id, plat, open_id, token, ext1, ext2, ext3, ext4, ext5) values(?,?,?,?,?,?,?,?,?)",
						u.getId(), plat.toString(), openId, token,
						params.get("ext1"), params.get("ext2"),
						params.get("ext3"), params.get("ext4"),
						params.get("ext5"));
		return u;
	}
}
