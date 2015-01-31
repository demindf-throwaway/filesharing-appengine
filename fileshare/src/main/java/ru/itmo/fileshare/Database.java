package ru.itmo.fileshare;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;



public class Database {
	static {
		ObjectifyService.register(FileEntity.class);
		ObjectifyService.register(HashList.class);
		ObjectifyService.register(PeerList.class);
		ObjectifyService.register(User.class);
	}
	
	// TODO: Подумать: здесь могут возникать плохие спецэффекты из-за многопоточности
	static Long putFile(FileEntity file, HashList chunkHashes) throws DatabaseException {
		if (file == null || !file.verify()) {
			throw new DatabaseException("Invalid file");
		}
		if (file.id == null && chunkHashes == null) {
			throw new DatabaseException("Files must have chunk hashes");
		}
		if (!chunkHashes.verify() || chunkHashes.id != null) {
			throw new DatabaseException("Invalid hashes");
		}
		Ref<PeerList> newPeerListRef = null;
		
		if (file.id != null) {
			// Probably trying to change existing entity
			FileEntity oldFile = ofy().load().entity(file).now();
			// Ensure that file indeed exists
			if (oldFile != null) {
				if (chunkHashes != null) {
					// Rewrite existing chunks
					chunkHashes.id = oldFile.chunkHashes.getKey().getId();
				}
				// Don't change old peerList
				newPeerListRef = oldFile.peerList;
			}
		}
		if (newPeerListRef == null) {
			// Initialize empty PeerList
			newPeerListRef = Ref.create(ofy().save().entity(new PeerList()).now());
		}
		file.peerList = newPeerListRef;
		if (chunkHashes != null) {
			// Update chunkHashes
			Key<HashList> newChunkHashesKey = ofy().save().entity(chunkHashes).now();
			// (If there was old file, Ref will be the same)
			// Also if there was an old file then right now it is in inconsistent state!
			// Watch out. This place is a source of concurrent modification bugs.
			file.chunkHashes = Ref.create(newChunkHashesKey);
		}		
		Key<FileEntity> fileKey = ofy().save().entity(file).now();
		return fileKey.getId();
	}
	
	static FileEntity getFile(Long id) {
		return ofy().load().type(FileEntity.class).id(id).now();
	}

	static void deleteFile(Long id) {
		ofy().delete().type(FileEntity.class).id(id).now();
	}
	
	static Long putUser(User user) {
		return ofy().save().entity(user).now().getId();
	}
	
	static User getUser(Long id) {
		return ofy().load().type(User.class).id(id).now();
	}
	
	static void deleteUser(Long id) {
		ofy().delete().type(User.class).id(id).now();
	}
	
	static void connect(final Long userId, final Long fileId, final PeerList.PeerType peerType) {
		if (userId == null || fileId == null) {
			throw new NullPointerException();
		}
		ofy().transact(new VoidWork() {	
			@Override
			public void vrun() {
				User user = ofy().load().type(User.class).id(userId).now();
				FileEntity file = ofy().load().type(FileEntity.class).id(fileId).now();
				PeerList peerList = file.peerList.get();
				if (findId(fileId, user.files) >= 0) {
					// Already connected
					return;
				}
				if (peerList.findId(userId) >= 0) {
					// Already connected (and database is probably inconsistent)
					return;
				}
				user.files.add(Ref.create(file));
				peerList.peers.add(new PeerList.Element(Ref.create(user), peerType));
				ofy().save().entities(user, peerList);
			}
		});
	}
	
	static void disconnect(final Long userId, final Long fileId) {
		if (userId == null || fileId == null) {
			throw new NullPointerException();
		}
		ofy().transact(new VoidWork() {
			@Override
			public void vrun() {
				User user = ofy().load().type(User.class).id(userId).now();
				FileEntity file = ofy().load().type(FileEntity.class).id(fileId).now();
				PeerList peerList = file.peerList.get();
				int userPos = findId(fileId, user.files);
				if (userPos == -1) {
					// Already disconnected
					return;
				}
				int peerPos = peerList.findId(userId);
				if (peerPos <= -1) {
					// Database is probably in inconsistent state
					return;
				}
				user.files.remove(userPos);
				peerList.peers.remove(peerPos);
				ofy().save().entities(user, peerList);
			}
		});
	}
	
	static <T> int findId(Long id, List<Ref<T>> refs) {
		if (id == null) {
			return -1;
		}
		for (int i = 0; i < refs.size(); i++) {
			Ref<T> ref = refs.get(i);
			if (id == ref.getKey().getId()) {
				return i;
			}
		}
		return -1;
	}

}
