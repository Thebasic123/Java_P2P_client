import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TCPServer implements Runnable {
	private ServerSocket server;
	private int firstSuccessor;
	private Peer user;
	private int quitDone;//counter for quit command, when it is equals to 2, quit directly
	
	
	public TCPServer(int portNumber,int firstSuccessor,Peer user){
		try {
			this.server=new ServerSocket(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.firstSuccessor=firstSuccessor;
		this.user=user;
		quitDone = 0;
	}
	
	public ServerSocket getServer(){
		return this.server;
	}
	
	public void run(){
		try{
			while(true){
				Socket s = server.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                	String request = "request\\s(\\d{4})\\s(\\d*)$";
            		Pattern requestPattern = Pattern.compile(request);
            		Matcher m = requestPattern.matcher(line);
            		String quit = "quit\\s(\\d+)\\s(\\d+)\\s(\\d+)$";
            		Pattern quitPattern = Pattern.compile(quit);
            		Matcher quitMatcher = quitPattern.matcher(line);
                	if(isFileRequest(m)){
                    	String fileString=m.group(1);//get fileNumber in string
                    	int fileNumber = Integer.parseInt(fileString);//get fileNumber
                    	String peerString = m.group(2);//get request peer in string
                    	int requestPeer = Integer.parseInt(peerString);//get peer number
                    	if(hasFile(fileNumber)){ // if peer has file
                    		String output = "File "+fileString+" is here.";
                    		System.out.println(output);
                    		output = "A response message, destined for peer "+requestPeer+", has been sent.";
                    		System.out.println(output);
                    		String sendMessage = "Received a response message from peer "+user.getIdentity()+", which has the file "+fileNumber+".";
                    		Socket send = new Socket("localhost", requestPeer+50000);
    	                    BufferedWriter out = new BufferedWriter(
    	                            new OutputStreamWriter(send.getOutputStream()));
    	                    out.write(sendMessage);
		                    out.newLine();
		                    out.flush();
		                    send.close();  
                    	}else{ // if peer doesn't have file
                    		String output = "File "+fileNumber+" is not stored here.";
                    		System.out.println(output);
                    		output = "File request message has been forwarded to my successor.";
                    		System.out.println(output);
                    		Socket send = new Socket("localhost", this.firstSuccessor+50000);
    	                    BufferedWriter out = new BufferedWriter(
    	                            new OutputStreamWriter(send.getOutputStream()));
    	                    out.write(line);
		                    out.newLine();
		                    out.flush();
		                    send.close();  
                    		
                    	}
                    }else if(isQuitRequest(quitMatcher)){
                    	int sender = Integer.parseInt(quitMatcher.group(1));
                    	int newFirstSuccessor = Integer.parseInt(quitMatcher.group(2));
                    	int newSecondSuccessor = Integer.parseInt(quitMatcher.group(3));
                    	//change peer successors
                    	this.user.setFirstSuccessor(newFirstSuccessor);
                    	this.user.setSecondSuccessor(newSecondSuccessor);
                    	System.out.println("Peer "+sender+" will depart from the network.");
                    	System.out.println("My first successor is now peer "+newFirstSuccessor+".");
                    	System.out.println("My first successor is now peer "+newSecondSuccessor+".");
                    	//send message to tell sender that current peer has changed successors
                    	String sendMessage = "quitDone";
                		Socket send = new Socket("localhost", sender+50000);
	                    BufferedWriter out = new BufferedWriter(
	                            new OutputStreamWriter(send.getOutputStream()));
	                    out.write(sendMessage);
	                    out.newLine();
	                    out.flush();
	                    send.close();  
                    }else if (line.equals("quitDone")){//make sure peer has received response from two predecessors,then quit
                    	quitDone++;
                    	if(quitDone==2){// change everything to 0,cut connections
                    		System.exit(0);

                    	}
                    }else{
                    	System.out.println(line);
                    }
                }
                s.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	public Boolean isFileRequest(Matcher m){
		return m.find();
	}
	public Boolean isQuitRequest(Matcher quitMatcher){
		return quitMatcher.find();
	}
	
	// has file method is an algorithm to check whether file is in current peer or not
	public Boolean hasFile(int fileNumber){
		int hash = (fileNumber+1)%256;
		int currentIdentity = this.server.getLocalPort()-50000;//get local identity number
		//algorithm:
		//1.if file at peer's anti-clockwise side,
		//hash - current peer < 0 
		//2.if file is at current peer
		//hash - current > 0 and hash - next < 0
		//3.if file is at clockwise side next current peer's first successor
		// hash - current > 0 and hash - current > 0 
		// and if current's identity is larger than next(current is at end of the loop)
		// discuss three cases differently
		if(currentIdentity<this.firstSuccessor){ // normal case
			if(hash - currentIdentity<0){
				//current peer doesn't have it, pass to next
				return false;
			}else{
				if(hash-this.firstSuccessor<0){
					//current peer has it
					return true;
				}else{
					return false;
				}
			}
			
		}else{// if current peer is at end of loop
			if(hash - currentIdentity<0){
				//either file is at anti-clockwise side of current peer or
				//clock-wise side of next peer(first peer in loop)
				// current peer just needs to pass request to next
				//except hash is before first peer bust after last peer in loop
				if(hash>=this.firstSuccessor){
					return false;
				}else{
					return true; 
				}
				
			}else{//if hash > current identity, current peer must have request file
				return true;
			}
			
		}
										
	}
	
	
}
