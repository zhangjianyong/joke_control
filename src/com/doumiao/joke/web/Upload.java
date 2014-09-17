package com.doumiao.joke.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.vo.Result;

@Controller
public class Upload {
	@Resource
	private ObjectMapper objectMapper;

	private static final Log log = LogFactory.getLog(Upload.class);
	String[] types = { "gif", "png", "jpg", "jpeg", "bmp" };

	@ResponseBody
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public Result upload(MultipartHttpServletRequest request,
			HttpServletResponse response) {
		MultipartFile mf = request.getFile("pic");
		int size = Config.getInt("pic_upload_max_size", 1000);
		if(mf.getSize()/1024>size){
			return new Result(false,"faild","图片不能大于"+size+"kb",null);
		}
		FileMeta fileMeta = null;
		String fileName = mf.getOriginalFilename();
		String picType = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (ArrayUtils.indexOf(types, picType) == -1) {
			picType = "jpg";
		}
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		long millis = c.getTimeInMillis();
		String name = "/" + year + "/" + month + "/" + millis + "." + picType;
		File file = new File(Config.get("pic_fetch_save_path") + name);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		fileMeta = new FileMeta();
		fileMeta.setFileName(name);
		fileMeta.setFileSize(mf.getSize() / 1024 + " Kb");
		fileMeta.setFileType(mf.getContentType());
		try {
			fileMeta.setBytes(mf.getBytes());
			FileCopyUtils.copy(mf.getBytes(),
					new FileOutputStream(Config.get("pic_fetch_save_path")
							+ name));
		} catch (IOException e) {
			log.error(e, e);
			return new Result(false,"faild","图片上传失败",null);
		}
		return new Result(true,"success","上传成功",fileMeta);
	}

	// @RequestMapping(value = "/get/{value}", method = RequestMethod.GET)
	// public void get(HttpServletResponse response, @PathVariable String value)
	// {
	// FileMeta getFile = files.get(Integer.parseInt(value));
	// try {
	// response.setContentType(getFile.getFileType());
	// response.setHeader("Content-disposition", "attachment; filename=\""
	// + getFile.getFileName() + "\"");
	// FileCopyUtils.copy(getFile.getBytes(), response.getOutputStream());
	// } catch (IOException e) {
	// log.error(e,e);
	// }
	// }
}

@JsonIgnoreProperties({ "bytes" })
class FileMeta {
	private String fileName;
	private String fileSize;
	private String fileType;
	private byte[] bytes;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}
