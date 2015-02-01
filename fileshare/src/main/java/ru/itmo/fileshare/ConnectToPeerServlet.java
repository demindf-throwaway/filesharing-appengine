package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

/*
 * Parameters:
 *   userId
 *   userSecret
 *   peerId
 * Returns:
 *   null
 */
public class ConnectToPeerServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getMethodName() {
		return "ConnectToPeer";
	}

	@Override
	protected Map<?, ?> processRequest(RequestParameters parameters)
			throws ServletException, IOException, RequestException {
		// TODO: send connection message via channel to other peer
		return null;
	}

}
