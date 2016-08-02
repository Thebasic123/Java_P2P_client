import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class cdht {
	private Peer user;
	private UDPConnection UDP;
	private TCPServer server;
	
	public cdht(String[] args){
		//convert arguments to integers
		int identity = Integer.parseInt(args[0]);
		int first = Integer.parseInt(args[1]);
		int second = Integer.parseInt(args[2]);
		this.user=new Peer(identity,first,second);
		//must create server socket before socket
		this.server=new TCPServer(user.getPort(),user.getFirstSuccessor(),user);
		this.UDP = new UDPConnection(user.getPort());
		
	}
	
	public static void main(String[] args) throws IOException {
		//create a new instance of main class
		 final cdht terminal = new cdht(args);
		//get input string
		final Scanner scanner = new Scanner(System.in);
		
		//ping messages
		int delay = 0;//start checking successors at the beginning 
		int period = 10000;//repeat every 10 secs
		String request = "request\\s(\\d{4})$";
		final Pattern requestPattern = Pattern.compile(request);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			public void run(){//task here
				//run ping message methods
				try {
					// use timer to send ping request repeatedly in certain time period
					terminal.UDP.sendPing(terminal.user.getFirstSuccessorPort(),terminal.user.getIdentity(),terminal.user.getFirstPredecessor());
					terminal.UDP.sendPing(terminal.user.getSecondSuccessorPort(),terminal.user.getFirstSuccessor(),terminal.user.getIdentity());
					Thread.sleep(200);

				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, delay,period);

		(new Thread() {
            @Override
            public void run() {
            	while (true) {
            		try {
						terminal.UDP.receiveMessage(terminal.user);
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            }
        }).start();
		
		(new Thread() {
            @Override
            public void run() {
            	while (scanner.hasNext()) {
	            	try {
	            		String message = scanner.nextLine();
	            		
	                    //use if statement to decided which message should send to successor
	                    Matcher m = requestPattern.matcher(message);
	                    if(m.find()) {//if string is file request and only for first sender
	                    	Socket s = new Socket("localhost", terminal.user.getFirstSuccessorPort());
		                    BufferedWriter out = new BufferedWriter(
		                            new OutputStreamWriter(s.getOutputStream()));
	                    	String fileNumber = m.group(1);
	                    	String localPrint = "File request message for "+fileNumber+" has been sent to my successor.";
		                    System.out.println(localPrint);
	                    	//pass successor same message, let successor decided what to do
		                    //add identity of request peer to message
		                    message = message + " " + terminal.user.getIdentity();
		                    out.write(message);
		                    out.newLine();
		                    out.flush();
		                    s.close();   
	                    }else if (message.equals("quit")){ // if input is quit, peer needs to inform two predecessors 
	                    	if(terminal.user.getFirstPredecessor()==0 || //if peer hasn't known it's predecessor,can't quit successfully
	                    			terminal.user.getSecondPredecessor()==0){
	                    		System.out.println("You haven't found predecessores yet,please try again in few seconds!!!");
	                    		
	                    	}else{
		                    	int identity = terminal.user.getIdentity();
		                    	int firstSuccessor = terminal.user.getFirstSuccessor();
		                    	int secondSuccessor = terminal.user.getSecondSuccessor();
		                    	int firstPredecessor = terminal.user.getFirstPredecessor();
		                    	String firstPreData="quit "+identity+" "+firstSuccessor+" "+secondSuccessor;
		                    	String secondPreData="quit "+identity+" "+firstPredecessor+" "+firstSuccessor;
		                    	Socket s1 = new Socket("localhost", terminal.user.getFirstPredecessorPort());
			                    BufferedWriter out1 = new BufferedWriter(
			                            new OutputStreamWriter(s1.getOutputStream()));
			                    out1.write(firstPreData);
			                    out1.newLine();
			                    out1.flush();
			                    s1.close();  
			                    Socket s2 = new Socket("localhost", terminal.user.getSecondPredecessorPort());
			                    BufferedWriter out2 = new BufferedWriter(
			                            new OutputStreamWriter(s2.getOutputStream()));
			                    out2.write(secondPreData);
			                    out2.newLine();
			                    out2.flush();
			                    s2.close();  
	                    	}
	                    }else{//input something random don't print out
	                    	//do nothing
	                    	System.out.println(message +" is not a valid command !!!");
	                    	
	                    }
	                } catch (UnknownHostException e) {
	                    e.printStackTrace();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
            	}
            }
        }).start();
		//starts TCP server thread
		new Thread(terminal.server).start();;
		
	}
	
}
