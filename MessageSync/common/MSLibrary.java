package MessageSync.common;

import java.lang.Math;

public class MSLibrary {
	public static final int PACKET_SIZE = 16;

	public static int getSequenceBit(byte[] data)
	{
		if (data == null)
			return -1;
		return (0b00000001 & ((int)data[0]));
	}

	public static String getDataType(byte[] data)
	{
		if (data == null)
			return null;

		int dataTypeInt = (0b00011110 & ((int)data[0])) >> 1;

		switch (dataTypeInt) {
			case 0:	return "byte";
			case 1: return "short";
			case 2: return "int";
			case 3: return "long";
			case 4: return "float";
			case 5: return "double";
			case 6: return "boolean";
			case 7: return "char";
			case 8: return "String";
			default: return "HALP";
		}
	}

	public static String getPacketType(byte[] data)
	{
		if (data == null)
			return null;

		int packetTypeInt = (data[0] & 0b11100000) >> 5;

		switch (packetTypeInt) {
			case 0:	return "SYN";
			case 1: return "SYNACK";
			case 2: return "REQUEST";
			case 3: return "DATA";
			case 4: return "ACK";
			case 5: return "FIN";
			default: return "HALP";
		}
	}

	public static int getPacketSignature(byte[] data) {
		return (int)data[0];
	}

	public static String getPacketDataString(byte[] data)
	{
		byte[] bytes = new byte[15];
		for (int i = 0; i < 15; i++)
			bytes[i] = data[i+1];
		return new String(bytes);
	}

	// DATA MUST BE A STRING OF 15 CHARACTERS OR LESS
 	public static byte[] prepareDATAPacket(int identifier, String data, int sequenceBit)
	{
		byte bytes[] = new byte[16];

		data = String.format("%1$-15s", data);

		int signature = (3 << 5) + (identifier << 1) + sequenceBit;
		bytes[0] = (byte)signature;
		
		byte dataBytes[] = data.getBytes();
		for (int i = 1; i < 15; i++)
			bytes[i] = dataBytes[i-1];
		bytes[15] = (byte)sequenceBit;

		return bytes;
	}

	public static byte[] preparePacket(int packetType, int identifier, int sequenceBit, String data)
	{
		byte bytes[] = new byte[16];
		int signature = (packetType << 5) + (identifier << 1) + sequenceBit;

		bytes[0] = (byte)signature;

		int numberOfPackets = (int)(Math.ceil(data.length()/15.0));
		int counter = 0;
		int packetShift = numberOfPackets;


		for (int i = 15; i > 1; i--)
		{
			packetShift = packetShift << (counter * 8);

			bytes[i] = (byte)(0b11111111 & (packetShift));
			counter++;
		}

		return bytes;
	}

	public static int getNumberOfPackets(byte[] data)
	{
		int numberOfPackets = 0;
		for (int i = 1; i < 16; i++)
		{
			numberOfPackets = numberOfPackets << 8;
			numberOfPackets += (int)data[i];
		}
		return numberOfPackets;
	}

	public static String[] breakMessage(String message)
	{
		int numberOfPackets = (int)(Math.ceil(message.length()/15.0));
		String[] messageSegments = new String[numberOfPackets];

		for (int i = 0; i < numberOfPackets; i++)
		{
			if (i == numberOfPackets - 1)
				messageSegments[i] = message.substring(i*15, message.length());
			else
				messageSegments[i] = message.substring(i*15, (i+1)*15);
		}
		return messageSegments;
	}
}



