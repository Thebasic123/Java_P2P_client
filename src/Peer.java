import java.net.DatagramSocket;
import java.net.SocketException;


public class Peer {
	
	private int identity;//range 0-255
	private int port;//identity + 50000
	private int firstSuccessor;//two successors' identities
	private int secondSuccessor;
	private int firstSuccessorPort;
	private int secondSuccessorPort;
	private int firstPredecessor;//nearest predecessor
	private int secondPredecessor;//far predecessor
	
	public Peer(int identity,int first,int second){
		this.identity=identity;
		this.port=identity+50000;
		this.firstSuccessor=first;
		this.secondSuccessor=second;
		this.firstSuccessorPort=first+50000;
		this.secondSuccessorPort=second+50000;
		this.firstPredecessor=0;
		this.secondPredecessor=0;
		
	}
	public int getIdentity(){
		return this.identity;
	}
	public int getPort(){
		return this.port;
	}
	public int getFirstPredecessor(){
		return this.firstPredecessor;
	}
	public int getSecondPredecessor(){
		return this.secondPredecessor;
	}
	public int getFirstPredecessorPort(){
		return this.firstPredecessor+50000;
	}
	public int getSecondPredecessorPort(){
		return this.secondPredecessor+50000;
	}
	public int getFirstSuccessor(){
		return this.firstSuccessor;
	}
	public int getSecondSuccessor(){
		return this.secondSuccessor;
	}
	public int getFirstSuccessorPort(){
		return this.firstSuccessorPort;
	}
	public int getSecondSuccessorPort(){
		return this.secondSuccessorPort;
	}
	public void setFirstPredecessor(int firstPredecessor){
		this.firstPredecessor = firstPredecessor;
	}
	public void setSecondPredecessor(int secondPredecessor){
		this.secondPredecessor = secondPredecessor;
	}
	//when successor get changed, successor's port number also get changed
	public void setFirstSuccessor(int newFirst){
		this.firstSuccessor=newFirst;
		int port = newFirst+50000;
		this.firstSuccessorPort=port;

	}
	public void setSecondSuccessor(int newSecond){
		this.secondSuccessor=newSecond;
		int port = newSecond +50000;
		this.secondSuccessorPort=port;

	}
	 
		
	
}