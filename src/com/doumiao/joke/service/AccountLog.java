package com.doumiao.joke.service;

import java.util.Date;

import com.doumiao.joke.enums.Account;
import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.WealthType;

public class AccountLog {
	private int meberId;
	private WealthType wealthType;
	private Account account;
	private int wealth;
	private String serialNumber;
	private String subSerialNmumber;
	private String remark;
	private AccountLogStatus status;
	private String operator;
	private Date wealthTime;

	public AccountLog() {
	};

	public AccountLog(int meberId, WealthType wealthType, Account account,
			int wealth, String serialNumber, String subSerialNmumber,
			String remark, AccountLogStatus status, String operator,
			Date wealthTime) {
		super();
		this.meberId = meberId;
		this.wealthType = wealthType;
		this.account = account;
		this.wealth = wealth;
		this.serialNumber = serialNumber;
		this.subSerialNmumber = subSerialNmumber;
		this.remark = remark;
		this.status = status;
		this.operator = operator;
		this.wealthTime = wealthTime;
	}

	public int getMeberId() {
		return meberId;
	}

	public void setMeberId(int meberId) {
		this.meberId = meberId;
	}

	public WealthType getwealthType() {
		return wealthType;
	}

	public void setwealthType(WealthType wealthType) {
		this.wealthType = wealthType;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public int getWealth() {
		return wealth;
	}

	public void setWealth(int wealth) {
		this.wealth = wealth;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getSubSerialNmumber() {
		return subSerialNmumber;
	}

	public void setSubSerialNmumber(String subSerialNmumber) {
		this.subSerialNmumber = subSerialNmumber;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public AccountLogStatus getStatus() {
		return status;
	}

	public void setStatus(AccountLogStatus status) {
		this.status = status;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Date getWealthTime() {
		return wealthTime;
	}

	public void setWealthTime(Date wealthTime) {
		this.wealthTime = wealthTime;
	}

}
