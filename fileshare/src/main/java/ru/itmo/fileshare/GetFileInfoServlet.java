package ru.itmo.fileshare;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;

/*
 * Parameters:
 *   fileId
 *   (opt) password
 *   (opt) adminPassword
 *   (opt) userId
 *   (opt) userSecret
 * Returns:
 *   fileId
 *   filename
 *   dateAdded
 *   totalSize
 *   pieceSize
 *   ownerId
 *   chunkHashes (in base64)
 */
public class GetFileInfoServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getMethodName() {
		return "GetFileInfo";
	}

	@Override
	protected Map<?, ?> processRequest(RequestParameters p)
			throws ServletException, IOException, RequestException {
		Long fileId = p.getLong("fileId");
		if (fileId == null) {
			throw new RequestException("fileId parameter is required");
		}
		FileEntity file = Database.getFile(fileId);
		if (file == null) {
			throw new RequestException("File does not exist");
		}
		Map<String, Object> result = new HashMap<>();
		result.put("fileId", file.id.toString());
		result.put("filename", file.name);
		result.put("dateAdded", dateToString(file.dateAdded));
		result.put("totalSize", file.totalSize);
		result.put("pieceSize", file.pieceSize);
		result.put("ownerId", file.owner.getKey().getId());
		result.put("chunkHashes", bytesToString(file.chunkHashes.get().chunkHashes));
		return result;
	}

	private String dateToString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
	}
	
	private String bytesToString(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}
}
