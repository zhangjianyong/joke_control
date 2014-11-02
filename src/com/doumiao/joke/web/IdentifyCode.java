package com.doumiao.joke.web;

import java.awt.image.BufferedImage;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import sun.misc.BASE64Encoder;

import com.doumiao.joke.vo.Result;
import com.google.code.kaptcha.Producer;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

@Controller
/**
 * 验证码
 *
 */
public class IdentifyCode {
	@Resource
	private Producer captchaProducer;
	@Resource
	private ObjectMapper objectMapper;
	@Resource
	private JdbcTemplate jdbcTemplate;

	private static final Log log = LogFactory.getLog(IdentifyCode.class);

	@ResponseBody
	@RequestMapping("/code")
	public Result code(HttpServletRequest request, HttpServletResponse response) {
		String capText = captchaProducer.createText().toLowerCase();
		// ServletOutputStream out = null;
		try {
			jdbcTemplate
					.update("insert uc_identify_code(k,c) values (?,?) on duplicate key update c = ?",
							System.currentTimeMillis(), capText, capText);
			BufferedImage bi = captchaProducer.createImage(capText);
			ByteOutputStream out = new ByteOutputStream();
			ImageIO.write(bi, "jpg", out);
			String content = BASE64Encoder.class.newInstance().encode(
					out.getBytes());
			out.flush();
			return new Result(true, request.getSession().getId(), "",
					"data:image/jpg;base64," + content);

		} catch (Exception e) {
			log.error(e, e);
		} finally {
			// IOUtils.closeQuietly(out);
		}
		return null;
	}

	@RequestMapping("/checkCode")
	@ResponseBody
	public Result codeCheck(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "code", required = true) String code,
			@RequestParam(value = "key", required = true) String key)
			throws Exception {
		String capText = null;
		try {
			capText = jdbcTemplate.queryForObject(
					"select c from uc_identify_code where k = ?", String.class,
					key);
		} catch (EmptyResultDataAccessException e) {
		} catch (Exception e) {
			log.error(e, e);
			return new Result(false, "faild", "系统错误", null);
		}
		if (capText == null || !StringUtils.equals(capText, code)) {
			log.warn("identify code check faild:"+code+"("+capText+")");
			return new Result(false, "faild", "验证码错误", null);
		}
		return new Result(true, "success", "验证通过", null);
	}
}
