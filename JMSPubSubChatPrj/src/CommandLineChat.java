import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CommandLineChat implements javax.jms.MessageListener {
	
	public static final String TOPIC= "topic/ZAPubSubChatTopic";
	
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
		try {
			System.out.println(((TextMessage) message).getText());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public static void main(String[] args) throws JMSException, NamingException, IOException {
		// TODO Auto-generated method stub
		
		if(args.length!=1)
			System.out.println("Ingresar un usuario antes de comenzar");
		else {
			 String username = args[0];
			 CommandLineChat commandLineChat = new CommandLineChat();
			 Context initialContext = CommandLineChat.getInitialContext();
			 Topic topic = (Topic) initialContext.lookup(CommandLineChat.TOPIC);
			 TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) initialContext.lookup("ConnectionFactory");
			 TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
			 
			 commandLineChat.subscribe(topicConnection, topic, commandLineChat);
			 commandLineChat.publish(topicConnection, topic, username);
			 
		}
	}
	
	public void subscribe(TopicConnection topicConnection, Topic topic, CommandLineChat commandLineChat) throws JMSException {
		
		TopicSession subscribeSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber topicSubscriber = subscribeSession.createSubscriber(topic);
		topicSubscriber.setMessageListener(commandLineChat);
		
	}
	
	public void publish(TopicConnection topicConnection, Topic topic, String username) throws JMSException, IOException {
	
		TopicSession publishSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicPublisher topicPublisher = publishSession.createPublisher(topic);
		topicConnection.start();
		BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String messageToSend = reader.readLine();
			if(messageToSend.equalsIgnoreCase("exit")) {
				
				topicConnection.close();
				System.exit(0);
				
			}
			else {
				TextMessage message = publishSession.createTextMessage();
				message.setText("["+username + "]: "+ messageToSend);
				topicPublisher.publish(message);
			}
		}
		
	}
		
	
	public static Context getInitialContext() throws JMSException, NamingException{
		
		
		
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming");
		props.setProperty("java.naming.provider.url", "localhost:1099");
		
		Context context = new InitialContext(props);
		
		return context;
	}

	
	
	
	
	

}
