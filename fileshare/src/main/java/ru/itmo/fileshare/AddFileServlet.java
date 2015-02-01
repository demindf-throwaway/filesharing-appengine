package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

/*
 * POST parameters:
 *   userId
 *   userSecret
 *   filename
 *   totalSize
 *   pieceSize
 *   chunkHashes
 *   (opt) password
 *   (opt) adminPassword
 * 
 * Returns:
 *   fileId
 */
public class AddFileServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected Map<?, ?> processRequest(RequestParameters p)
			throws ServletException, IOException, RequestException {
		FileEntity file = new FileEntity.Builder()
			.name(p.getString("filename"))
			.dateAdded(new Date())
			.totalSize(p.getLong("totalSize"))
			.pieceSize(p.getLong("pieceSize"))
			.password(p.getString("password"))
			.adminPassword(p.getString("adminPassword"))
			.owner(p.getLong("userId"))
			.build();
		try {
			Long id = Database.putFile(file, new HashList(p.get("chunkHashes").get()));
			if (id == null) {
				throw new ServletException("Database return null id for added file");
			}
			Map<String, String> result = new HashMap<String, String>();
			result.put("fileId", id.toString());
			return new HashMap<String, String>();
			
		} catch (DatabaseException e) {
			throw new ServletException("Logic error: couldn't add file to database", e);
		}
	}

	@Override
	protected String getMethodName() {
		return "AddFile";
	}
}
