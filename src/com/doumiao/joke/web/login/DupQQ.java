package com.doumiao.joke.web.login;

import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.doumiao.joke.service.AccountService;

@Controller
public class DupQQ {

	@Resource
	private AccountService accountService;

	@RequestMapping("/dup")
	public void bind() throws IOException {
		accountService.dupQQ();
	}
}
