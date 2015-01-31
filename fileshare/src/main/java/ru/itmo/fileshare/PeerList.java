package ru.itmo.fileshare;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class PeerList {
	@Id Long id;
	
	List<Element> peers = new ArrayList<>();
	
	static enum PeerType {
		seed, // Seeds don't receive updates when peer comes online/offline
		leech // Leechers track who is online
	}
	
	static public class Element {
		Ref<User> user;
		PeerType peerType;
		
		Element(Ref<User> user, PeerType peerType) {
			this.user = user;
			this.peerType = peerType;
		}
	}
	
	int findId(Long id) {
		if (id == null) {
			return -1;
		}
		for (int i = 0; i < peers.size(); i++) {
			Element e = peers.get(i);
			if (id == e.user.getKey().getId()) {
				return i;
			}
		}
		return -1;
	}
}
