package org.bebrb.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Platform;

import org.bebrb.client.NetConsole.NetPoint;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;

public class Client {
	private OnResponse response;
	private String host;
	private int port;
	private OnError error;
	private Thread thread = null;
	
	public static final NetConsole console = new NetConsole();  

	public Client(String host, int port, OnResponse response, OnError error) {
		this.response = response;
		this.error = error;
		this.host = host;
		this.port = port;
	}

	public void send(final Command cmd) {

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String data = "";
					try(Socket sock = new Socket()){
						sock.connect(new InetSocketAddress(Inet4Address
								.getByName(host), port));
						PrintWriter out = new PrintWriter(new OutputStreamWriter(
								sock.getOutputStream()));

						String q = CommandFactory.toJson(cmd);

						NetPoint point = console.push(new NetConsole.NetPoint(q));
						
						out.write(q);
						out.flush();
						sock.shutdownOutput();

						// sync. wait response
						BufferedReader in = new BufferedReader(
								new InputStreamReader(sock.getInputStream()));
						String s;
						while ((s = in.readLine()) != null) {
							data += s;
						}
						
						point.finish(data);
					};
					try {
						response.replyСame(data);
					} catch (Exception ex) {
						throw new ExecException(ex.getMessage(), ex);
					}
				} catch (final Exception e) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							error.errorCame(e);
						}
					}); 
				}

			}
		});
		thread.start();
	}

	public void interrupt() {
		if(thread!=null && !thread.isInterrupted()) {
			thread.interrupt();
			thread = null;
		}
		
	}
	
	public interface OnResponse {
		public void replyСame(String message) throws Exception;
	}
	
	public interface OnError {
		public void errorCame(Exception ex);
	}
	
	@SuppressWarnings("serial")
	public class ExecException extends Exception {
		public ExecException(Exception ex) {
			super(ex);
		}

		public ExecException(String message, Exception ex) {
			super(message,ex);
		}
	}


}
