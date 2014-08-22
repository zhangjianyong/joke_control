package com.doumiao.joke.schedule;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConfigSchedule {
	private static final Log log = LogFactory.getLog(ConfigSchedule.class);
	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(fixedDelay = 60000)
	protected void refreshConfig() {
		if (log.isDebugEnabled()) {
			log.debug("refresh config");
		}
		List<Map<String,Object>> configs = jdbcTemplate.queryForList("select * from joke_config");
		for (Map<String, Object> c : configs) {
			Config.set((String)c.get("key"),(String)c.get("value"));
		}
	}
}
