package com.doumiao.joke.schedule;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteUpDownLog {
	private static final Log log = LogFactory.getLog(DeleteUpDownLog.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(cron="0 0 0 ? * *")
	public void list() {
		jdbcTemplate.execute("truncate table joke_article_updown");
		if(log.isInfoEnabled()){
			log.info("truncate table joke_article_updown");
		}
	}
}
