package com.doumiao.joke.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Error {
	@RequestMapping(value = "/error")
	public String error(HttpServletRequest request, HttpServletResponse response) {
		return "/error";
	}

}