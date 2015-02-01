package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

/*
 * Parameters:
 *   fileId
 *   (opt) newPassword  (if missing, password will be reseted)
 *   (opt) newAdminPassword  (if missing, password will be reseted)
 *   (opt) userId
 *   (opt) userSecret
 *   (opt) adminPassword
 * 
 * Returns:
 *   null
 */
public class SetPasswordServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getMethodName() {
		return "SetPassword";
	}

	@Override
	protected Map<?, ?> processRequest(RequestParameters p)
			throws ServletException, IOException, RequestException {
		Long fileId = p.getLong("fileId");
		if (fileId == null) {
			throw new ServletException("fileId parameter is required");
		}
		FileEntity file = Database.getFile(fileId);
		file.password = p.getString("newPassword");
		file.adminPassword = p.getString("newAdminPassword");
		return null;
	}

}
