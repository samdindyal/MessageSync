package MessageSync.client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import MessageSync.common.MSLibrary;

public class MSClientRuntime {

	private byte dataOut[], dataIn[];

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
			dataOut = requestString.getBytes();
			request = new DatagramPacket(dataOut, requestString.length(), hostAddress, 5000);
			socket.send(request);
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


	public byte[] listen(int attempt) {
		try {
			dataIn = new byte[16];
			request = new DatagramPacket(dataIn, dataIn.length);
			socket.setSoTimeout(5000);
			socket.receive(request);
			dataIn = request.getData();
			System.out.println(MSLibrary.getPacketType(dataIn) + " RECEIVED");

		} catch (Exception e){
			if (attempt <= 5)
			{
				System.err.println("Waiting for a packet. Attempt: " + attempt);
				attempt++;
				listen(attempt);
			}
			else
				System.err.println("Maximum attempts reached.");
		}


		return dataIn;
	}

	public void sendPacket(byte[] data, int attempt) {
		try {
			reply = new DatagramPacket(data, data.length, hostAddress, 5000);
			socket.send(reply);
			System.out.println(MSLibrary.getPacketType(data) + " SENT");
		} catch(Exception e){
			e.printStackTrace();
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
			int sequenceBit = 0;
			while (!MSLibrary.getPacketType(dataIn).equals("FIN"))
			{
				if (MSLibrary.getPacketType(dataIn).equals("DATA"))
				{
					sequenceBit = (sequenceBit + 1) % 2;
					buffer += MSLibrary.getPacketDataString(dataIn);
					dataOut = MSLibrary.preparePacket(4, 8, sequenceBit, "");
					sendPacket(dataOut, 1);
					dataIn = listen(1);
				}
			}
			System.out.println(buffer);
		}
	}

	public static void main(String[] args){
		MSClientRuntime rt = new MSClientRuntime();
		rt.normalRun();

	}
}