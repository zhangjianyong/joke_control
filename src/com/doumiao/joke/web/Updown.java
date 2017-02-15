package com.doumiao.joke.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.WealthType;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.service.AccountService;
import com.doumiao.joke.service.MemberService;
import com.doumiao.joke.vo.Member;
import com.doumiao.joke.vo.Result;

@Controller
public class Updown {

	private static final Log log = LogFactory.getLog(Updown.class);
	private static Map<Integer, Set<Integer>> userTodayUpdown = new ConcurrentHashMap<Integer, Set<Integer>>();
	private static Map<Integer, Date> userLastUpDown = new ConcurrentHashMap<Integer, Date>();
	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private ObjectMapper objectMapper;

	@Resource
	private AccountService accountService;
	
	@Resource
	private MemberService memberService;
	

	@ResponseBody
	@RequestMapping(value = "/updown", method = RequestMethod.POST)
	public Result up(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "type") String type,
			@RequestParam(value = "articleId") int articleId,
			@RequestParam(value = "uid") int uid) {
		
		Member u = memberService.findById(uid);
		if(u==null){
			log.error("opertion is not up or donw");
			return new Result(false, "faild", "用户不存在", "");
		}
		if(u.getStatus()==1){
			log.error("opertion is not up or donw");
			return new Result(false, "faild", u.getRemark(), "");
		}
		String _type = type.toLowerCase();
		if (!_type.equals("down") && !_type.equals("up")) {
			log.error("opertion is not up or donw");
			return new Result(false, "faild", "系统错误", "");
		}

		Set<Integer> set = userTodayUpdown.get(uid);
		if (set == null) {
			try {
				List<Map<String, Object>> articles = jdbcTemplate
						.queryForList(
								"select article_id,create_time from joke_article_updown where member_id = ? order by create_time desc ",
								uid);
				set = new HashSet<Integer>();
				for (int i = 0; i < articles.size(); i++) {
					Map<String, Object> a = articles.get(i);
					set.add((Integer) a.get("article_id"));
					if (i == 0) {
						userLastUpDown.clear();
						userLastUpDown.put(uid, (Date) a.get("create_time"));
					}
				}
				userTodayUpdown.put(uid, set);
			} catch (Exception e) {
				log.error(e, e);
				return new Result(false, "faild", "系统错误", null);
			}
		}

		/* 检查该会员今天是否顶沉过该内容 */
		if (set.contains(articleId)) {
			return new Result(false, "faild", "已投过票", null);
		}

		/* 检查此次操作应距上次10秒钟以上 */
		Calendar cc = Calendar.getInstance();
		cc.add(Calendar.SECOND, -10);
		Date last = userLastUpDown.get(uid);
		if (last != null && cc.getTime().before(last)) {
			return new Result(false, "faild", "慢一点，看完效果再点评吧", null);
		}

		/* 更新最后的评论时间 并加入到评论列表 */
		userLastUpDown.put(uid, new Date());
		set.add(articleId);

		/* 记为一次顶沉操作 */
		jdbcTemplate.update("update joke_article set " + _type + " = " + _type
				+ " +1 where id = ?", articleId);
		jdbcTemplate
				.update("insert into joke_article_updown(article_id,member_id,type) values(?,?,?)",
						articleId, uid, _type);
		/* 检查当日积分是否满额 */
		int scoreUpDownPerTime = Config.getInt("score_up_down_per_time", 1);
		int scoreUpDownMaxPerDay = Config.getInt("score_up_down_max_per_day",
				15);

		/* 如果不满额,发放积分 */
		if (set.size() <= scoreUpDownMaxPerDay) {
			// 生成中奖流水
			List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>(2);
			Map<String, Object> l = new HashMap<String, Object>(1);
			l.put("u", uid);
			l.put("a", Account.S1.name());
			l.put("t", WealthType.UPDOWN.name());
			l.put("w", scoreUpDownPerTime);
			l.put("s", AccountLogStatus.PAYED.name());
			l.put("r", "");
			l.put("o", "system");
			ls.add(l);

			try {
				accountService.batchPay(ls);
			} catch (Exception e) {
				log.error(e, e);
				return new Result(false, "param.error", "系统错误", null);
			}
		}
		return new Result(true, "success", "投票成功", null);
	}

	public static void clearUserUpDown() {
		userTodayUpdown.clear();
		userLastUpDown.clear();
	}
}