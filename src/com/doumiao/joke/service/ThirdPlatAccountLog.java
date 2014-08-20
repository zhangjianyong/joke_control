package com.doumiao.joke.service;

import com.doumiao.joke.enums.AccountLogStatus;
import com.doumiao.joke.enums.Plat;

public class ThirdPlatAccountLog {
	private int meberId;
	private Plat plat;
	private String account;
	private int wealth;
	private String serialNumber;
	private String subSerialNmumber;
	private String remark;
	private AccountLogStatus status;
	private String operator;

	public ThirdPlatAccountLog() {
	}

	public ThirdPlatAccountLog(int meberId, Plat plat, String account,
			int wealth, String serialNumber, String subSerialNmumber,
			String remark, AccountLogStatus status, String operator) {
		super();
		this.meberId = meberId;
		this.plat = plat;
		this.account = account;
		this.wealth = wealth;
		this.serialNumber = serialNumber;
		this.subSerialNmumber = subSerialNmumber;
		this.remark = remark;
		this.status = status;
		this.operator = operator;
	}

	public int getMeberId() {
		return meberId;
	}

	public void setMeberId(int meberId) {
		this.meberId = meberId;
	}

	public Plat getPlat() {
		return plat;
	}

	public void setPlat(Plat plat) {
		this.plat = plat;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
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
	};

}
