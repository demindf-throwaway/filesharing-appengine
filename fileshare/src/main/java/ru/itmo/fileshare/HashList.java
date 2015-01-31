package ru.itmo.fileshare;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class HashList {
	static final int HASH_SIZE = 20;

	@Id Long id;
	byte[] chunkHashes;
	
	public boolean verify() {
		if (chunkHashes == null) {
			return false;
		}
		if (chunkHashes.length == 0 || chunkHashes.length % HASH_SIZE != 0) {
			return false;
		}
		return true;
	}
}
