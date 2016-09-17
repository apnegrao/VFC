package mobihoc.session;

import java.util.*;
import java.io.*;

public class UserAgent implements Serializable, Comparable<UserAgent> {

	private static final long serialVersionUID = 1L;
	
	private String _nickname;
	
	public UserAgent(String nickname) {
		_nickname = nickname;
	}
	
	public String toString() {
		return "UserAgent nickname=" + _nickname;
	}
	
	public String getNickname() {
		return _nickname;
	}
	
	public int compareTo(UserAgent ua) {
		return _nickname.compareTo(ua.getNickname());
	}

	public boolean equals(Object o) {
		return compareTo((UserAgent)o) == 0;
	}
}
