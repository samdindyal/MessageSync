package MessageSync.client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import MessageSync.common.MSLibrary;

public class MSClientRuntime {

	private byte requestBytes[], inputBufferBytes[], dataOut[], dataIn[];

	private DatagramSocket socket;
	private DatagramPacket reply, request;

	private InetAddress hostAddress;

	private String buffer;

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

	public byte[] listen(int attempt)
	{
		inputBufferBytes = new byte[16];
		try {
			reply = new DatagramPacket(inputBufferBytes, inputBufferBytes.length);
			socket.setSoTimeout(5000);
			this.socket.receive(reply);
			return inputBufferBytes;
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
		return inputBufferBytes;
	}

	public void sendPacket(byte[] data, int attempt) {
		try {
			reply = new DatagramPacket(data, 16, request.getAddress(), request.getPort());
			socket.send(reply);
		} catch(Exception e){
			if (attempt <= 5)
			{
				System.err.println("Could not send packet. Attempt: " + attempt);
				attempt++;
				sendPacket(data, attempt);
			}	
			else
				System.err.println("Maximum attempts reached.");
		}
	}

	public void normalRun() {
		
		buffer = "";
		dataOut = MSLibrary.preparePacket(0, 8, 0, "");
		sendPacket(dataOut, 1);
		dataIn = listen(1);

		if (MSLibrary.getPacketType(dataIn).equals("SYNACK"))
		{
			dataOut = MSLibrary.preparePacket(2, 8, 0, "");
			sendPacket(dataOut, 1);

			dataIn = listen(1);
			int counter = 0;
			while (!MSLibrary.getPacketType(dataIn).equals("FIN"))
			{
				if (MSLibrary.getPacketType(dataIn).equals("DATA") && MSLibrary.getSequenceBit(dataIn) != counter)
				{
					counter = (counter + 1) % 2;
					buffer += MSLibrary.getPacketDataString(dataIn);
					dataOut = MSLibrary.preparePacket(4, 8, counter, "");
					sendPacket(dataOut, 1);
				}
			}
		}

		// public static byte[] preparePacket(int packetType, 
		// 	int identifier, int sequenceBit, String data)
	}

	public static void main(String[] args){
		MSClientRuntime rt = new MSClientRuntime();
		rt.normalRun();

	}
}