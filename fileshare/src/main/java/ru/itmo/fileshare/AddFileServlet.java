package ru.itmo.fileshare;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import org.apache.commons.fileupload.FileItem;

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
 * Return:
 *   fileId
 */
public class AddFileServlet extends FileshareServlet {
	private static final long serialVersionUID = 1L;
	// Max limit on list of hashes size based on datastore maximum blob size
	public static final long MAX_HASHLIST_SIZE = 512 * 1024;

	@Override
	protected Map<?, ?> processRequest(Map<String, FileItem> parameters)
			throws ServletException, IOException, RequestException {
		Long userId = getLong(parameters.get("userId"));
		String userSecret = getString(parameters.get("userSecret"));
		String filename = getString(parameters.get("filename"));
		Long totalSize = getLong(parameters.get("totalSize"));
		Long pieceSize = getLong(parameters.get("pieceSize"));
		String password = getString(parameters.get("password"));
		String adminPassword = getString(parameters.get("adminPassword"));
		FileItem chunkHashes = parameters.get("chunkHashes");
		if (!UserManager.validateUser(userId, userSecret)) {
			throw new RequestException("User is not authorized");
		}
		if (!checkFile(filename, totalSize, pieceSize, chunkHashes)) {
			throw new RequestException("Bad file parameters");
		}
		if (!checkPassword(password, false) || !checkPassword(adminPassword, true)) {
			throw new RequestException("Bad password");
		}
		filename = (filename == null) ? "" : filename;
		FileEntity file = new FileEntity.Builder()
			.name(filename)
			.dateAdded(new Date())
			.totalSize(totalSize)
			.pieceSize(pieceSize)
			.password(password)
			.adminPassword(adminPassword)
			.owner(userId)
			.build();
		try {
			Long id = Database.putFile(file, new HashList(chunkHashes.get()));
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
	
	private boolean checkFile(String filename, Long totalSize, Long pieceSize, FileItem chunkHashes) {
		if (filename == null || totalSize == null || pieceSize == null || chunkHashes == null) {
			return false;
		}
		if (totalSize <= 0 || pieceSize <= 0) {
			return false;
		}
		if (chunkHashes.isFormField()) {
			return false;
		}
		if (chunkHashes.getSize() > MAX_HASHLIST_SIZE) {
			return false;
		}
		if (!(new HashList(chunkHashes.get())).verify()) {
			return false;
		}
		return true;
	}

	private boolean checkPassword(String password, boolean isAdmin) {
		return true;
	}
}
