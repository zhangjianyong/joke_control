package com.doumiao.joke.lang;

import java.util.Date;
import java.util.List;

public class Article {
	private int id;
	private String title;
	private String summary;
	private String pic;
	private int type;
	private int down;
	private int up;
	private int websiteId;
	private String websitePid;
	private Date createTime;
	List<String> contents;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getDown() {
		return down;
	}
	public void setDown(int down) {
		this.down = down;
	}
	public int getUp() {
		return up;
	}
	public void setUp(int up) {
		this.up = up;
	}
	public int getWebsiteId() {
		return websiteId;
	}
	public void setWebsiteId(int websiteId) {
		this.websiteId = websiteId;
	}
	public String getWebsitePid() {
		return websitePid;
	}
	public void setWebsitePid(String websitePid) {
		this.websitePid = websitePid;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public List<String> getContents() {
		return contents;
	}
	public void setContents(List<String> contents) {
		this.contents = contents;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
}
