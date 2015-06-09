package com.doumiao.joke.schedule;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.web.Updown;

@Component
public class DeleteUpDownLog {
	private static final Log log = LogFactory.getLog(DeleteUpDownLog.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(cron = "0 0 0 ? * *")
	public void list() {
		jdbcTemplate.execute("truncate table joke_article_updown");
		jdbcTemplate.execute("update uc_account set s1=0 where s1>0");
		Updown.clearUserUpDown();
		if (log.isInfoEnabled()) {
			log.info("truncate table joke_article_updown");
		}
	}
}
