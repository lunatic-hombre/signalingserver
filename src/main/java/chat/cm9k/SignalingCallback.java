package chat.cm9k;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import com.jsoniter.output.JsonStream;

import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class SignalingCallback implements WebSocketConnectionCallback {

private static Set<String> clients = new HashSet<>();

    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        String path = exchange.getRequestURI();
        System.out.println("Client connected: " + path);
        channel.getReceiveSetter().set(createChannel(exchange));
        channel.resumeReceives();
        channel.flush();
    }

    private AbstractReceiveListener createChannel(WebSocketHttpExchange exchange) {
        return new AbstractReceiveListener() {
            
        	@Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                String messageString = message.getData();
                String command = parseCommand(messageString);
                System.out.println("Command=" + command + " Message=" + messageString);
                switch(command) {
                	case "HELLO": handleHelloCommand(channel, messageString);
                		break;
                	case "OFFER": handleOfferCommand(channel, messageString);
            			break;
                	case "ANSWER": handleAnswerCommand(channel, messageString);
            			break;
                	case "WHO": handleWhoCommand(channel);
                		break;
                	default:
                		throw new InvalidParameterException("Invalid command: " + command);
                }
            }
            
			@Override
            protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) {
                System.out.println("Socket closed");
            }
        };
    }
	private String parseCommand(String messageString) {
		String [] pieces = messageString.split(" ");
		return pieces[0];
	}

    private void handleHelloCommand(WebSocketChannel channel, String messageString) {
        sendToAllPeers(channel, messageString);
		clients.add(messageString);
	}

    private void handleOfferCommand(WebSocketChannel channel, String messageString) {
        sendToAllPeers(channel, messageString);
	}

    private void handleAnswerCommand(WebSocketChannel channel, String messageString) {
        sendToAllPeers(channel, messageString);
	}

    private void handleWhoCommand(WebSocketChannel channel) {
    	sendToAllPeers( channel, JsonStream.serialize(clients));
	}
    
	private void sendToAllPeers(WebSocketChannel channel, String messageString) {
		for (WebSocketChannel peer : channel.getPeerConnections()) {
            WebSockets.sendText(messageString, peer, null);
        }
	}

}
