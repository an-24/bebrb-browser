package org.bebrb.client;

public class Host {
	public final boolean security;
	public final String domain;
	public final int port;
	public final String path;
	
	public Host() {
		this.domain = "";
		this.port = 80;
		this.security = true;
		this.path = "";
	}

	public Host(String domain, int port, boolean security, String path) {
		this.domain = domain;
		this.port = port;
		this.security = security;
		this.path = path;
	}

	public String getLocation() {
		return toString()+path;
	}
	
	public String toString() {
		return domain+(port>0 && port!=80?":"+port:"");
	}
}
