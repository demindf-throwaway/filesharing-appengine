package ru.itmo.fileshare;

import java.util.Date;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class FileEntity {
	@Id Long id;

	// Auxiliary info
	String name;
	Date dateAdded;
	long totalSize;
	long pieceSize;	

	// Can be null
	String password;
	// Can be null
	String adminPassword;
	
	// Can be null
	Ref<User> owner;
	Ref<HashList> chunkHashes;
	Ref<PeerList> peerList;
	
	
	public boolean verifyPassword(String password) {
		if (password == null || this.password == null) {
			return false;
		}
		return password.equals(this.password);
	}

	public boolean verifyAdminPassword(String password) {
		if (password == null || this.adminPassword == null) {
			return false;
		}
		return password.equals(this.adminPassword);
	}
	
	void setOwnerId(Long id) {
		owner = Ref.create(Key.create(User.class, id));
	}
	
	Long getOwnerId() {
		return owner.key().getId();
	}
	
	public boolean verify() {
		if (name == null) {
			return false;
		}
		if (dateAdded == null) {
			return false;
		}
		if (totalSize <= 0) {
			return false;
		}
		if (pieceSize <= 0) {
			return false;
		}
		return true;
	}
	
	private FileEntity() {}
	
	private FileEntity(Builder builder) {
		this.name = builder.newName;
		this.dateAdded = builder.newDateAdded;
		this.totalSize = builder.newTotalSize;
		this.pieceSize = builder.newPieceSize;
		this.password = builder.newPassword;
		this.adminPassword = builder.newAdminPassword;
		this.setOwnerId(builder.newOwner);
	}
	
	static public class Builder {
		String newName;
		Date newDateAdded;
		long newTotalSize;
		long newPieceSize;

		String newPassword;
		String newAdminPassword;

		Long newOwner;
		
		public Builder name(String newName) {
			this.newName = newName;
			return this;
		}
		
		public Builder dateAdded(Date newDateAdded) {
			this.newDateAdded = newDateAdded;
			return this;
		}
		
		public Builder totalSize(long newTotalSize) {
			this.newTotalSize = newTotalSize;
			return this;
		}
		
		public Builder pieceSize(long newPieceSize) {
			this.newPieceSize = newPieceSize;
			return this;
		}
		
		public Builder password(String newPassword) {
			this.newPassword = newPassword;
			return this;
		}
		
		public Builder adminPassword(String newAdminPassword) {
			this.newAdminPassword = newAdminPassword;
			return this;
		}
		
		public Builder owner(Long newOwner) {
			this.newOwner = newOwner;
			return this;
		}
		
		public FileEntity build(){
			return new FileEntity(this);
		}
	}
}
