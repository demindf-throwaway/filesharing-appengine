package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.json.simple.JSONObject;

import ru.itmo.fileshare.SecurityManager.SecurityError;

class RequestException extends Exception {
	private static final long serialVersionUID = 1L;
	
	RequestException() { super(); }
	RequestException(String message) { super(message); }
	RequestException(String message, Throwable cause) { super(message, cause); }
	RequestException(Throwable cause) { super(cause); }
}

public abstract class FileshareServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected abstract String getMethodName();

	protected abstract Map<?, ?> processRequest(RequestParameters parameters)
			throws ServletException, IOException, RequestException;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			RequestParameters parameters = new RequestParameters(request);
			SecurityManager.checkRequest(getMethodName(), parameters);
			Map<?, ?> result = processRequest(parameters);
			writeOkResponse(response, result);
		} catch (RequestException | FileUploadException | SecurityError e) {
			writeErrorResponse(response, e.getMessage());
		}
	}

// Private
	@SuppressWarnings("unchecked")
	private void writeErrorResponse(HttpServletResponse response, String message)
			throws IOException {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("status", "ERROR");
		jsonResponse.put("result", null);
		jsonResponse.put("error_description", message);
		jsonResponse.writeJSONString(response.getWriter());
	}

	@SuppressWarnings("unchecked")
	private void writeOkResponse(HttpServletResponse response, Map<?, ?> result)
			throws IOException {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("status", "OK");
		jsonResponse.put("result", result);
		jsonResponse.writeJSONString(response.getWriter());
	}
}
