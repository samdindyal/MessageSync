package MessageSync.server;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.Calendar;

import MessageSync.common.MSLibrary;

public class MSServerRuntime {

	private DatagramSocket socket;
	private DatagramPacket request, reply;

	private byte buffer[], dataIn[], dataOut[];

	private String messageOfTheDay[] =  new String[]{
		"The highway speed limit is 100.",
		"I like to drink ShareTea.",
		"My favourite drink is Coffee.",
		"We are going to ace 706",
		"The assignment is due at 4PM.",
		"Carolyn is the president of WICS",
		"Daniel is the president of VSAR"
	};

	public MSServerRuntime() {

		try {
			socket = new DatagramSocket(5000);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] listen(int attempt) {
		try {
			buffer = new byte[16];
			request = new DatagramPacket(buffer, buffer.length);
			socket.setSoTimeout(5000);
			socket.receive(request);
			dataIn = request.getData();

			for (int i = 0; i < dataIn.length; i++)
				System.out.println(dataIn[i]);

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

	public void reply(String response, int attempt) {
		try {
			reply = new DatagramPacket(response.getBytes(), 16, request.getAddress(), request.getPort());
			socket.send(reply);
		} catch(Exception e){
			if (attempt <= 5)
			{
				System.err.println("Could not send packet. Attempt: " + attempt);
				attempt++;
				reply(response, attempt);	
			}	
			else
				System.err.println("Maximum attempts reached.");
		}
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

	public void sendMessage(String message)
	{
		String[] messageSegments = MSLibrary.breakMessage(message);

		for (int i = 0; i < messageSegments.length && !MSLibrary.getPacketType(dataIn).equals("FIN"); i++)
		{
				
			dataOut = MSLibrary.prepareDATAPacket(8, messageSegments[i], i%2);
			dataIn = listen(1);

			if (!MSLibrary.getPacketType(dataIn).equals("ACK")
				|| MSLibrary.getSequenceBit(dataIn) != i%2)
			{
				i--;
				continue;
			}
		}

		dataOut = MSLibrary.preparePacket(5, 8, 0, message);
			sendPacket(dataOut, 1);
	}

	public void normalRun() {
		if (MSLibrary.getPacketType(dataIn = listen(1)).equals("SYN"))
		{
			System.out.println("SYN PACKET RECEIVED");
			String message = messageOfTheDay[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];

			// Send SYNACK
			dataOut = MSLibrary.preparePacket(1, 8, 0, message);
			sendPacket(dataOut, 1);

			if (MSLibrary.getPacketType(dataIn = listen(1)).equals("REQUEST"))
				sendMessage(message);
		}
	}

	public static void main (String[] args){
		MSServerRuntime rt = new MSServerRuntime();
		rt.normalRun();
	}
}