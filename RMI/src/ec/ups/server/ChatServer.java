 package ec.ups.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import ec.ups.client.ChatClientIF;

public class ChatServer extends UnicastRemoteObject implements ChatServerIF{

	String line = "---------------------------------------------\n";
	private Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;
	
	protected ChatServer() throws RemoteException {
		super();
		chatters= new Vector<Chatter>(10,1);
	}
	
	public static void main(String[] args) {
		startRMIRegistry();	
		String hostName = "localhost";
		String serviceName = "GroupChatService";
		
		if(args.length == 2){
			hostName = args[0];
			serviceName = args[1];
		}
		
		try{
			ChatServerIF hello = new ChatServer();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);
			System.out.println("Grupo de char RMI Activo..");
		}
		catch(Exception e){
			System.out.println("Server problemas para iniciar");
		}	
	}
	
	/**
	 * 
	 */
	public static void startRMIRegistry() {
		try{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready");
		}
		catch(RemoteException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Return a message to client
	 */
	public String sayHello(String ClientName) throws RemoteException {
		System.out.println(ClientName + " enviar el mensage");
		return "Hola " + ClientName + " desde el servidor de chat grupal";
	}
	


	@Override
	public void updateChat(String userName, String chatMessage) throws RemoteException {
		// TODO Auto-generated method stub
		String message =  userName + " : " + chatMessage + "\n";
		//sendToAll(message);
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(message);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void passIDentity(RemoteRef ref) throws RemoteException {
		// TODO Auto-generated method stub
		try{
			System.out.println(line + ref.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void registerListener(String[] details) throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " has joined the chat session");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'sRMI service : " + details[2]);
		registerChatter(details);
	}
	/**
	 * 
	 */
	private void registerChatter(String[] details){		
		try{
			ChatClientIF nextClient = ( ChatClientIF )Naming.lookup("rmi://" + details[1] + "/" + details[2]);
			
			chatters.addElement(new Chatter(details[0], nextClient));
			
			nextClient.messageFromServer("[Server] : Hello " + details[0] + " you are now free to chat.\n");
			
			sendToAll("[Server] : " + details[0] + " has joined the group.\n");
			
			updateUserList();		
		}
		catch(RemoteException | MalformedURLException | NotBoundException e){
			e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	
	private void updateUserList() {
		String[] currentUsers = getUserList();	
		for(Chatter c : chatters){
			try {
				c.getClient().updateUserList(currentUsers);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}
	/**
	 * 
	 */
	private String[] getUserList(){
		// generate an array of current users
		String[] allUsers = new String[chatters.size()];
		for(int i = 0; i< allUsers.length; i++){
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}
	
/**
 * 
 */
	public void sendToAll(String newMessage){	
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(newMessage);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void leaveChat(String userName) throws RemoteException {
		// TODO Auto-generated method stub
		for(Chatter c : chatters){
			if(c.getName().equals(userName)){
				System.out.println(line + userName + " left the chat session");
				System.out.println(new Date(System.currentTimeMillis()));
				chatters.remove(c);
				break;
			}
		}		
		if(!chatters.isEmpty()){
			updateUserList();
		}			
	}

	@Override
	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException {
		// TODO Auto-generated method stub
		Chatter pc;
		for(int i : privateGroup){
			pc= chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	}

	
}
