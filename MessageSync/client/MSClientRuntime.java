package MessageSync.client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import MessageSync.common.MSLibrary;

public class MSClientRuntime {

	private byte requestBytes[], inputBufferBytes[];

	private DatagramSocket socket;
	private DatagramPacket reply, request;

	private InetAddress hostAddress;

	public MSClientRuntime()
	{
		try{
			socket = new DatagramSocket();
			hostAddress =  InetAddress.getByName("localhost");
		}catch(Exception e){}
	}

	public void request(String requestString, int attempt)
	{
		try {
			requestBytes = requestString.getBytes();
			request = new DatagramPacket(requestBytes, requestString.length(), hostAddress, 5000);
			socket.send(request);
			socket.close();
		}catch (Exception e){
			if (attempt <= 5)
			{
				System.err.println("Request failed. Attempt: " + attempt);
				attempt++;
				request(requestString, attempt);
			}
			else
				System.err.println("Maximum attempts reached.");
		}
	}

	public void listen(int attempt)
	{
		inputBufferBytes = new byte[16];
		try {
			reply = new DatagramPacket(inputBufferBytes, inputBufferBytes.length);
			socket.setSoTimeout(5000);
			this.socket.receive(reply);
		} catch (Exception e) {
			if (attempt <= 5)
			{
				System.err.println("Failed to receive. Attempt: " + attempt);
				attempt++;
				listen(attempt);
			}
			else
				System.err.println("Maximum attempts reached.");
		}
	}

	public static void main(String[] args){
		// MSClientRuntime rt = new MSClientRuntime();
		// rt.request("HELLO", 1);
		// rt.listen(1);

		String test = "123456789012345678901234567890";
		
	}
}