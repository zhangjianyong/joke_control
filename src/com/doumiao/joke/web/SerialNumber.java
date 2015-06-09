package com.doumiao.joke.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.doumiao.joke.lang.SerialNumberGenerator;
import com.doumiao.joke.vo.Result;

@Controller
public class SerialNumber {

	@RequestMapping("/sngenerate")
	public synchronized Result generate(HttpServletRequest request,
			HttpServletResponse response, @RequestParam int count) {
		return new Result(true,"success","成功",SerialNumberGenerator.generate(count));
	}
}
