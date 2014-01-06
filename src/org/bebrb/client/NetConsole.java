package org.bebrb.client;

import java.util.Date;
import java.util.LinkedList;

public class NetConsole {
	private LinkedList<NetPoint> storage = new LinkedList<>();

	public NetPoint push(NetPoint v) {
		storage.addFirst(v);
		v.start = new Date().getTime();
		return v;
	}

	public NetPoint peek() {
		return storage.getFirst();
	}

	public NetPoint pop() {
		return storage.removeFirst();
	}

	public boolean empty() {
		return storage.isEmpty();
	}

	public static class NetPoint {
		private String query;
		private String response;
		private long duration;

		private long start;
		
		public NetPoint(String query) {
			this.query = query;
		}
		
		public String getQuery() {
			return query;
		}
		public void setQuery(String query) {
			this.query = query;
		}
		public String getResponse() {
			return response;
		}
		public void setResponse(String responce) {
			this.response = responce;
		}
		public long getDuration() {
			return duration;
		}

		public void finish(String data) {
			duration = new Date().getTime() - start;
			this.response = data;
		}
	}
}
