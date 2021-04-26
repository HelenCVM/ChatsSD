package ec.ups.server;


import ec.ups.client.ChatClientIF;


/**
 * A class used by the server program to keep
 * details of connected clients ordered
 * @author Daragh Walshe 	B00064428
 * RMI Assignment 2		 	April 2015
 *
 */
public class Chatter {

	public String name;
	public ChatClientIF client;
	
	//constructor
	public Chatter(String name, ChatClientIF client){
		this.name = name;
		this.client = client;
	}

	
	//getters and setters
	public String getName(){
		return name;
	}
	public ChatClientIF getClient(){
		return client;
	}
	
	
}
