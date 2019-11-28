package chat.cm9k;

import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import net.wessendorf.websocket.SimpleWebSocketClient;
import net.wessendorf.websocket.WebSocketHandlerAdapter;

public class SimpleClient implements Runnable {

	public static void main(String [] args) {
		new Thread(new SimpleClient("abcde")).start();
		try {
			Thread.sleep(300 * 1000);
		} catch (InterruptedException ignore) {
		}
		new Thread(new SimpleClient("12345")).start();
	}
	private final String clientId;
	
	public SimpleClient(String clientId) {
		super();
		this.clientId = clientId;
	}
	public String getClientId() {
		return clientId;
	}
	public void run() {
		
		SimpleWebSocketClient client = createWebSocketClient();
		client.setWebSocketHandler(new WebSocketHandlerAdapter() {
			
			@Override
			public void onOpen() {
				//client.sendText("Hello"); // You can immediately send text here
			}
		
			@Override
			public void onMessage(String message) {
				System.out.println("onMessage: " + message);
				String command = SignalingCallback.parseCommand(message);
				switch(command) {
					case "HELLO":
						System.out.println("HELLO received by client " + getClientId() + ", sending OFFER");
						sendOffer(client, message);
						break;
					case "OFFER":
						System.out.println("OFFER received by client " + getClientId() + ", sending ANSWER");
						sendAnswer(client, message);
						break;
					case "ANSWER":
						System.out.println("ANSWER received by client " + getClientId() + ", sending ICE");
						sendIce(client, message);
						break;
					case "ICE":
						System.out.println("ICE received by client " + getClientId() + ", by all means DO connect");
						break;
					case "WHO":
						System.out.println("WHO received by client " + getClientId());
						break;
					default:
						throw new InvalidParameterException("Invalid command received by client " + getClientId() + ": " + command);
				}
			}
		});
	
		client.connect();
		client.sendText("HELLO " + getClientId() + " pipokoeie");
		System.out.println("Client " + getClientId() + " waiting for an hour");
		try {
			Thread.sleep(3600 * 1000);
		} catch (InterruptedException ignore) {
		}
	}
	private SimpleWebSocketClient createWebSocketClient() {
		SimpleWebSocketClient client;
		try {
			client = new SimpleWebSocketClient("ws://localhost:8888/echo");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return client;
	}
	protected void sendOffer(SimpleWebSocketClient client, String message) {
		// TODO We simply prepend OFFER and the two ID's to the message. So this message will grow a bit.
		String fromClientId = SignalingCallback.parseFromClientId(message);
		String newMessage = "OFFER " + fromClientId + " " + getClientId() + " " + message;
		sendProtocolMessage(client, newMessage);
	}
	protected void sendAnswer(SimpleWebSocketClient client, String message) {
		// TODO We simply prepend ANSWER and the two ID's to the message. So this message will grow a bit.
		String toClientId = SignalingCallback.parseToClientId(message);
		String newMessage = "ANSWER " + getClientId() + " " + toClientId + " " + message;
		sendProtocolMessage(client, newMessage);
	}
	protected void sendIce(SimpleWebSocketClient client, String message) {
		// TODO We simply prepend ICE and the two ID's to the message. So this message will grow a bit.
		String fromClientId = SignalingCallback.parseFromClientId(message);
		String newMessage = "ICE " + fromClientId + " " + getClientId() + " " + message;
		sendProtocolMessage(client, newMessage);
	}
	private void sendProtocolMessage(SimpleWebSocketClient client, String newMessage) {
		System.out.println("sending " + newMessage);
		client.sendText(newMessage);
	}
}