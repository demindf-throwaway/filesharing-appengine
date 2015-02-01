package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

/*
 * Parameters:
 *   userId
 *   userSecret
 *   fileId
 * Returns:
 *   null
 */
public class UnsubscribeFromFile extends FileshareServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getMethodName() {
		return "UnsubsribeFromFile";
	}

	@Override
	protected Map<?, ?> processRequest(RequestParameters p)
			throws ServletException, IOException, RequestException {
		Long userId = p.getLong("userId");
		if (userId == null) {
			throw new RequestException("userId is required");
		}
		Long fileId = p.getLong("fileId");
		if (fileId == null) {
			throw new RequestException("fileId parameter is required");
		}
		Database.disconnect(userId, fileId);
		// TODO: notify other peers
		return null;
	}
}
