package com.doumiao.joke.vo;

public class Result {
	private String msg;
	private String code;
	private Object content;
	private boolean success;

	public Result() {
		// TODO Auto-generated constructor stub
	}

	public Result(boolean success, String code, String msg, Object content) {
		super();
		this.msg = msg;
		this.code = code;
		this.content = content;
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
