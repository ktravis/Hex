package util;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPListener implements Runnable {
	public static final int DEFAULT_PORT = 8080;
	public static final int PACKET_SIZE = 1024;
	
	int port = DEFAULT_PORT;
	DatagramSocket socket;
	DatagramPacket packet;
	byte[] buffer;
	private boolean listening = false;
	
	public UDPListener () {
		buffer = new byte[PACKET_SIZE];
		packet = new DatagramPacket(buffer, buffer.length);
	}
	public UDPListener (int p) {
		buffer = new byte[PACKET_SIZE];
		packet = new DatagramPacket(buffer, buffer.length);
		port = p;
	}
	
	public void setPort(int p) { port = p; }
	public void connect() { 
		do {
			try {
				socket = new DatagramSocket(port);
				break;
			} catch (BindException e) {
				port += 1;
				System.out.println(port);
				//e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		} while (true);
		System.out.println("Socket open on port: " + String.valueOf(port));
	}
	
	public void listen() {
		try {
			while (!socket.isClosed()) {
				socket.receive(packet);
				String data = new String(buffer, 0, packet.getLength());
				if (data.contains("!exit"))	break;
				System.out.print(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Exiting...");
		listening = false;
		close();
	}
	
	public void close() {
		if (socket == null) return;
		socket.disconnect();
		socket.close();
		buffer = new byte[PACKET_SIZE];
		
	}
	
	public byte[] getPacket() {
		return buffer;
	}
	
	public boolean isListening() {
		return listening;
	}
	
	@Override
	public void run() {
		if (socket == null) connect();
		listening = true;
		listen();
	}
	
	public static void main (String[] args) {
		UDPListener l = new UDPListener();
		(new Thread(l)).start();
		boolean lis = true;
		while(lis) {
			System.out.println("1");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lis = l.isListening();
		}
		System.exit(0);
	}
	
}
