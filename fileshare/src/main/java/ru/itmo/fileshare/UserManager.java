package ru.itmo.fileshare;

public class UserManager {
	static boolean validateUser(Long id, String secret) {
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
}
