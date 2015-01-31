package ru.itmo.fileshare;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class User {
	@Id Long id;
	String privateToken; // User uses this token to prove his identity
	// TODO: add channel information
	List<Ref<FileEntity>> files = new ArrayList<>();
}
