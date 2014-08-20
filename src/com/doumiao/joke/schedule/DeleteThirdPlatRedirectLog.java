package com.doumiao.joke.schedule;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteThirdPlatRedirectLog {
	private static final Log log = LogFactory
			.getLog(DeleteThirdPlatRedirectLog.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(cron = "0 0 0 ? * *")
	public void list() {
		// jdbcTemplate.execute("delete from uc_thirdplat_redirect where timestampdiff(minute, create_time, current_timestamp) > 3");
		jdbcTemplate.execute("truncate table uc_thirdplat_redirect");
		if (log.isInfoEnabled()) {
			log.info("truncate table uc_thirdplat_redirect");
		}
	}
}
