package chat.cm9k;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import com.jsoniter.output.JsonStream;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class SignalingCallback implements WebSocketConnectionCallback {

	private static final String CLIENT_ID = "client-id";

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
                    case "ICE": handleIceCommand(channel, messageString);
                        break;
	                case "TALK": handleTalkCommand(channel, messageString);
		                break;
                	case "WHO": handleWhoCommand(channel);
                		break;
                	default:
                		throw new InvalidParameterException("Invalid command: " + command);
                }
            }
            
			@Override
            protected void onClose(WebSocketChannel channel, StreamSourceFrameChannel frameChannel) {
                System.out.println("Socket closed");
                sendToAllPeers(channel, "BYE " + channel.getAttribute(CLIENT_ID));
            }
        };
    }
	public static String parseCommand(String message) {
		String [] parts = message.split(" ");
		assert(parts.length >= 3);
		return parts[0];
	}
	public static String parseFromClientId(String message) {
		String [] parts = message.split(" ");
		assert(parts.length >= 3);
		return parts[1];
	}

	public static String parseToClientId(String message) {
		String [] parts = message.split(" ");
		assert(parts.length >= 4);
		return parts[2];
	}

    private void handleHelloCommand(WebSocketChannel channel, String messageString) {
    	// Remember the client-id. We will use it to determine whom to send messages
    	channel.setAttribute(CLIENT_ID, parseFromClientId(messageString));
        sendToAllPeersExceptFromClientId(channel, messageString);
		clients.add(messageString);
	}

    private void handleOfferCommand(WebSocketChannel channel, String messageString) {
        sendOnlyToFromClientId(channel, messageString);
	}

    private void handleAnswerCommand(WebSocketChannel channel, String messageString) {
        sendOnlyToToClientId(channel, messageString);
	}

    private void handleIceCommand(WebSocketChannel channel, String messageString) {
        sendOnlyToFromClientId(channel, messageString);
	}

	private void handleTalkCommand(WebSocketChannel channel, String messageString) {
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

	private void sendToAllPeersExceptFromClientId(WebSocketChannel channel, String messageString) {
		for (WebSocketChannel peer : channel.getPeerConnections()) {
			String channelClientId = (String) peer.getAttribute(CLIENT_ID);
			String messageClientId = parseFromClientId(messageString);
			if ( ! messageClientId.equals(channelClientId)) {
				WebSockets.sendText(messageString, peer, null);
			}
        }
	}

	private void sendOnlyToFromClientId(WebSocketChannel channel, String messageString) {
		for (WebSocketChannel peer : channel.getPeerConnections()) {
			String channelClientId = (String) peer.getAttribute(CLIENT_ID);
			String messageClientId = parseFromClientId(messageString);
			if ( messageClientId.equals(channelClientId)) {
				WebSockets.sendText(messageString, peer, null);
			}
        }
	}

	private void sendOnlyToToClientId(WebSocketChannel channel, String messageString) {
		for (WebSocketChannel peer : channel.getPeerConnections()) {
			String channelClientId = (String) peer.getAttribute(CLIENT_ID);
			String messageClientId = parseToClientId(messageString);
			if ( messageClientId.equals(channelClientId)) {
				WebSockets.sendText(messageString, peer, null);
			}
        }
	}

}
