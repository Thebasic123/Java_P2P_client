import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Timer;


public class UDPConnection {
	private DatagramSocket UDPSocket;
	
	public UDPConnection(int portNumber){
		try {
			this.UDPSocket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	//first four bytes(int) in byte array is 0, packet is request packet
	//first four bytes(int) in byte array is 1, packet is response packet
	
	//pass in peer's successors' port numbers to make packets
	public void sendPing(int destinationPort,int firstPredecessor,int secondPredecessor) throws IOException{
		int senderPort = this.UDPSocket.getLocalPort();
		byte[] sendData = new byte[256];
		int[] data = {0, senderPort, firstPredecessor, secondPredecessor };

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);

        sendData = byteBuffer.array();
		//data is sending socket's port number
		//sendData = ByteBuffer.allocate(4).putInt(senderPort).array();
		
		//get IPaddress for localhost
		byte[] ip={127,0,0,1};
		InetAddress IPAddress= InetAddress.getByAddress(ip);
		DatagramPacket sendPacket = new DatagramPacket
				(sendData,sendData.length,IPAddress,destinationPort);
		//send to packets to two successors
		this.UDPSocket.send(sendPacket);
	}
	//check first int in data 
	public void receiveMessage(Peer user) throws IOException{
		byte[] buffer = new byte[256];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		try {
	        this.UDPSocket.receive(packet);
	    } catch (SocketTimeoutException e) {
	    	//do nothing
	    }
		//get data from packet
		byte[] data = packet.getData();
		int checksum = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt();
		//if checkum is equal to 0,packet is request packet 
		if(checksum==0){
			//use packet info set predecessors
			int firstPredecessor = ByteBuffer.wrap(Arrays.copyOfRange(data, 8, 12)).getInt();
			int secondPredecessor = ByteBuffer.wrap(Arrays.copyOfRange(data, 12, 16)).getInt();
			user.setFirstPredecessor(firstPredecessor);
			user.setSecondPredecessor(secondPredecessor);
			int portFrom = ByteBuffer.wrap(Arrays.copyOfRange(data, 4, 8)).getInt();
			portFrom = portFrom-50000;
			//print out that peer has received ping message from
			//predecessor
			System.out.println("A ping request message was received from Peer "+portFrom+".");
			//then send response ping message to source port(predecessor)
			portFrom=portFrom+50000;
			int receiverPeer = this.UDPSocket.getLocalPort();
			byte[] responseData = new byte[256];
			int[] response = {1, receiverPeer, 0, 0 };

	        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
	        IntBuffer intBuffer = byteBuffer.asIntBuffer();
	        intBuffer.put(response);

	        responseData = byteBuffer.array();
			//responseData = ByteBuffer.allocate(4).putInt(receiverPeer).array();
			byte[] ip={127,0,0,1};
			InetAddress IPAddress= InetAddress.getByAddress(ip);
			DatagramPacket responsePacket = new DatagramPacket
					(responseData,responseData.length,IPAddress,portFrom);
			this.UDPSocket.send(responsePacket);
		}else{
			int portFrom = ByteBuffer.wrap(Arrays.copyOfRange(data, 4, 8)).getInt();
			portFrom=portFrom-50000;
			//print out that peer has received response ping message from successors
			System.out.println("A ping response message was received from Peer "+portFrom+".");
		}
	}
}