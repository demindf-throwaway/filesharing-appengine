package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

public class RemoveFileServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected Map<?, ?> processRequest(RequestParameters p)
			throws ServletException, IOException, RequestException {
		boolean success = Database.deleteFile(p.getLong("fileId"));
		if (!success) {
			throw new ServletException("Database can't delete file. It's probably doesn't exist");
		}
		return null;
	}

	@Override
	protected String getMethodName() {
		return "RemoveFile";
	}
}
