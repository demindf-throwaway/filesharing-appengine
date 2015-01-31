package ru.itmo.fileshare;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;

class RequestException extends Exception {
	private static final long serialVersionUID = 1L;
	
	RequestException() { super(); }
	RequestException(String message) { super(message); }
	RequestException(String message, Throwable cause) { super(message, cause); }
	RequestException(Throwable cause) { super(cause); }
}

public abstract class FileshareServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected Map<String, FileItem> prepareParameters(HttpServletRequest request) throws FileUploadException {
		Map<String, FileItem> parameters = new HashMap<>();
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		for (FileItem item: items) {
			parameters.put(item.getFieldName(), item);
		}
		return parameters;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResponse = new JSONObject();
		try {
			Map<?, ?> result = processRequest(prepareParameters(request));
			jsonResponse.put("status", "OK");
			jsonResponse.put("result", result);
		} catch (RequestException | FileUploadException e) {
			jsonResponse.put("status", "ERROR");
			jsonResponse.put("result", null);
			jsonResponse.put("error_description", e.getMessage());
		}
		PrintWriter writer = response.getWriter();
		jsonResponse.writeJSONString(writer);
	}

	protected abstract Map<?, ?> processRequest(Map<String, FileItem> parameters)
		throws ServletException, IOException, RequestException;
	
	protected String getString(FileItem item) {
		if (item == null) {
			return null;
		}
		if (item.isFormField()) {
			return item.getString();
		}
		return null;
	}
	
	protected Long getLong(FileItem item) {
		String number = getString(item);
		if (number == null) {
			return null;
		}
		try {
			return Long.parseLong(number);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
