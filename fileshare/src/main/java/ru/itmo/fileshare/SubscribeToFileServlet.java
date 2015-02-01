package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

/*
 * Parameters:
 *   userId
 *   userSecret
 *   fileId
 *   peerType (seed/leech)
 *   (opt) password
 *   (opt) adminPassword
 * Returns:
 *   null
 */
public class SubscribeToFileServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getMethodName() {
		return "SubscribeToFile";
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
		String strPeerType = p.getString("receiveUpdates");
		if (strPeerType == null) {
			throw new RequestException("peerType parameter is required");
		}
		PeerList.PeerType peerType = PeerList.PeerType.fromString(strPeerType);
		if (peerType == null) {
			throw new RequestException("Invalid peerType value");
		}		
		// TODO: notify other peers
		Database.connect(userId, fileId, peerType);
		return null;
	}
}
