package com.doumiao.joke.web;

import java.awt.image.BufferedImage;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.doumiao.joke.vo.Result;
import com.google.code.kaptcha.Producer;

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

	private static final Log log = LogFactory.getLog(IdentifyCode.class);

	@RequestMapping("/code")
	public String code(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		String capText = captchaProducer.createText().toLowerCase();
		ServletOutputStream out = null;
		try {
			request.getSession().setAttribute("code", capText);
			BufferedImage bi = captchaProducer.createImage(capText);
			out = response.getOutputStream();
			ImageIO.write(bi, "jpg", out);
			out.flush();
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
		return null;
	}

	@RequestMapping("/checkCode")
	@ResponseBody
	public Result codeCheck(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "code", required = true) String code)
			throws Exception {
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
		if(StringUtils.equals((String)request.getSession().getAttribute("code"),code)){
			return new Result(false,null,null,null);
		}
		return new Result(true,null,null,null);
	}
}
