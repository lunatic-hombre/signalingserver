package chat.cm9k;

import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import net.wessendorf.websocket.SimpleWebSocketClient;
import net.wessendorf.websocket.WebSocketHandlerAdapter;

public class SimpleClient{
	
	public static void main(String [] args) throws URISyntaxException {
		new SimpleClient().run();
	}
public void run() throws URISyntaxException {
	final SimpleWebSocketClient client = new SimpleWebSocketClient("ws://localhost:8888/echo");
	client.setWebSocketHandler(new WebSocketHandlerAdapter() {
		@Override
		public void onOpen() {
			//client.sendText("Hello"); // ship it!!
		}
	
		@Override
		public void onMessage(String message) {
			System.out.println("onMessage: " + message);
			String command = SignalingCallback.parseCommand(message);
			switch(command) {
				case "HELLO": //ignore
					break;
				case "OFFER":
					System.out.println("OFFER received");
					break;
				case "ANSWER":
					System.out.println("OFFER received");
					break;
				case "WHO":
					System.out.println("OFFER received");
					break;
				default:
					throw new InvalidParameterException("Invalid command: " + command);
			}
		}
	});

	client.connect();
	client.sendText("HELLO pipokoeie");
}
}