package ru.itmo.fileshare;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

public class SecurityManager {
	// Max limit on list of hashes size based on datastore maximum blob size
	public static final long MAX_HASHLIST_SIZE = 512 * 1024;
	
	public static class SecurityError extends Exception {
		private static final long serialVersionUID = 1L;
		
		SecurityError() { super(); }
		SecurityError(String message) { super(message); }
		SecurityError(String message, Throwable cause) { super(message, cause); }
		SecurityError(Throwable cause) { super(cause); }
	}
	
	public static boolean checkUser(Long id, String secret) {
		if (id == null || secret == null) {
			return false;
		}
		try {
			User user = Database.getUser(id);
			return user.privateToken.equals(secret);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static void checkRequest(String name, RequestParameters parameters)
			throws SecurityError {
		if (name == null || parameters == null) {
			throw new NullPointerException();
		}
		RequestChecker checker = requestCheckers.get(name);
		if (checker == null) {
			throw new SecurityError("Unknown method");
		}
		checker.check(parameters);
	}
	
	public static boolean checkPasswordStrength(String password, boolean isAdmin) {
		return true;
	}

// Private:
	private static interface RequestChecker {
		void check(RequestParameters parameters) throws SecurityError;
	}

	private static final Map<String, RequestChecker> requestCheckers = new HashMap<>();
	
	private static void registerRequestChecker(String name, RequestChecker checker) {
		requestCheckers.put(name,  checker);
	}
	
	static {
		registerRequestChecker("AddFile", new RequestChecker() {
			@Override
			public void check(RequestParameters p)
					throws SecurityError {
				if (!checkUser(p.getLong("userId"), p.getString("userSecret"))) {
					throw new SecurityError("User is not authorized");
				}
				checkFile(
					p.getString("filename"),
					p.getLong("totalSize"),
					p.getLong("pieceSize"),
					p.get("chunkHashes"));
				if (!checkPasswordStrength(p.getString("password"), false)
						|| !checkPasswordStrength(p.getString("adminPassword"), true)) {
					throw new SecurityError("Weak password");
				}
				
			}
		});
		
		registerRequestChecker("RemoveFile", new RequestChecker() {
			@Override
			public void check(RequestParameters p)
					throws SecurityError {
				if (!checkUser(p.getLong("userId"), p.getString("userSecret"))) {
					boolean correctPassword = false;
					String adminPassword = p.getString("adminPassword");
					if (adminPassword != null) {
						FileEntity file = Database.getFile(p.getLong("fileId"));
						if (file == null) {
							throw new SecurityError("File does not exist");
						}
						if (file.verifyAdminPassword(adminPassword)) {
							correctPassword = true;
						}
					}
					if (!correctPassword) {
						throw new SecurityError("User is not authorized");
					}
				}
			}			
		});
		
		registerRequestChecker("GetFileInfo", new RequestChecker() {
			@Override
			public void check(RequestParameters p) throws SecurityError {
				Long fileId = p.getLong("fileId");
				if (fileId == null) {
					throw new SecurityError("fileId parameter is required");
				}
				checkFileAccess(fileId, p);
			}
		});
		
		registerRequestChecker("SubscribeToFile", new RequestChecker() {
			@Override
			public void check(RequestParameters p)
					throws SecurityError {
				if (!checkUser(p.getLong("userId"), p.getString("userSecret"))) {
					throw new SecurityError("User is not authorized");
				}
				Long fileId = p.getLong("fileId");
				if (fileId == null) {
					throw new SecurityError("fileId parameter is required");
				}
				checkFileAccess(fileId, p);
			}
		});
		
		registerRequestChecker("UnsubscribeFromFile", new RequestChecker() {
			@Override
			public void check(RequestParameters p)
					throws SecurityError {
				if (!checkUser(p.getLong("userId"), p.getString("userSecret"))) {
					throw new SecurityError("User is not authorized");
				}
			}			
		});
	}
	
	private static void checkFileAccess(Long fileId, RequestParameters p)
			throws SecurityError {
		FileEntity file = Database.getFile(fileId);
		if (file == null) {
			throw new SecurityError("File does not exist");
		}
		// If no password then file is for everyone
		if (file.password == null) {
			return;
		}
		// Owner can always access his files
		if (checkUser(p.getLong("userId"), p.getString("userSecret"))
				&& p.getLong("userId") == file.owner.getKey().getId()) {
			return;
		}
		// If passwords match, then allow access
		if (file.verifyPassword(p.getString("password"))) {
			return;
		}
		// If admin passwords match, then allow access
		if (file.verifyAdminPassword(p.getString("adminPassword"))) {
			return;
		}
		// This person can't be allowed to view this file
		throw new SecurityException("Access forbiden: you must be the owner of file or provide correct password");
	}

	private static void checkFile(String filename, Long totalSize, Long pieceSize, FileItem chunkHashes)
			throws SecurityError {
		if (filename == null || totalSize == null || pieceSize == null || chunkHashes == null || chunkHashes.isFormField()) {
			throw new SecurityError("Bad file parameters");
		}
		if (totalSize <= 0 || pieceSize <= 0) {
			throw new SecurityError("File size must be greater than zero");
		}
		if (chunkHashes.getSize() > MAX_HASHLIST_SIZE) {
			throw new SecurityError("chunkHashes parameter is too large");
		}
		if (!(new HashList(chunkHashes.get())).verify()) {
			throw new SecurityError("chunkHashes doesn't contain valid hashes");
		}
	}

}















