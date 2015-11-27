package MessageSync.server;

import java.net.DatagramSocket;
import java.net.DatagramPacket;

import MessageSync.common.MSLibrary;

public class MSServerRuntime {

	private DatagramSocket socket;
	private DatagramPacket request, reply;

	private byte buffer[], dataIn[];

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
				System.err.println("Could not get packet. Attempt: " + attempt);
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
			reply = new DatagramPacket(response.getBytes(), response.length(), request.getAddress(), request.getPort());
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

	public static void main (String[] args){
		MSServerRuntime rt = new MSServerRuntime();
		rt.listen(1);
		rt.reply("HI", 1);
	}
}