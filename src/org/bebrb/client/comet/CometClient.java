package org.bebrb.client.comet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.bebrb.client.Host;

public class CometClient {
	private CometListener listiner;
	private Host host;
	private Thread thread;
	private Logger log = Logger.getLogger("bebrb");

	public CometClient(Host host, CometListener listiner) {
		this.listiner = listiner;
		this.host = host;
	}
	
	public void start() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!thread.isInterrupted()) {
					waitMessage();
				}
			}
		});
		thread.start();
		
	}

	private void waitMessage() {
		try {
			try(Socket sock = new Socket()){
				sock.connect(new InetSocketAddress(Inet4Address
							.getByName(host.domain), host.port));
				InputStreamReader in = new InputStreamReader(sock.getInputStream());
				log.info("comet thread start to "+host);
				
				String data = "";
				while (true) {
					int b = in.read();
					if(b<0) return; //server close socked
					char ch = (char)b;
					if(ch == '\n' && in.read() == '\r') {
						listiner.onMessage(new Message(data));
						data = "";
					} else;
					data+=ch;
				}
			}	
		} catch (UnknownHostException e) {
			log.severe("comet error: "+e.getMessage());
			thread.interrupt();
		} catch (IOException e) {
		}
	}

}
