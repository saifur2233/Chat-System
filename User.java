package muli_user_chat_system;

import java.util.StringTokenizer;

public class User {

	private String name;
	private String ip;

	public User(String name, String ip) {
		this.name = name;
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		StringTokenizer st = new StringTokenizer(ip, "/");
		return st.nextToken();
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
