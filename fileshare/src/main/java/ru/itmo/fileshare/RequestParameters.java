package ru.itmo.fileshare;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class RequestParameters extends HashMap<String, FileItem> {
	private static final long serialVersionUID = 1L;

	public RequestParameters(HttpServletRequest request)
			throws FileUploadException {
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		for (FileItem item: items) {
			put(item.getFieldName(), item);
		}
	}

	public String getString(String name) {
		if (name == null) {
			return null;
		}
		FileItem item = get(name);
		if (item == null) {
			return null;
		}
		if (item.isFormField()) {
			return item.getString();
		}
		return null;
	}

	public Long getLong(String name) {
		String number = getString(name);
		if (number == null) {
			return null;
		}
		try {
			return Long.parseLong(number);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public Boolean getBoolean(String name) {
		String b = getString(name);
		if (b == null) {
			return null;
		}
		return Boolean.parseBoolean(b);
	}
}
