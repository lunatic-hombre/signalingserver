package chat.cm9k;

import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class SignalingCallback implements WebSocketConnectionCallback {


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
                System.out.println("Message received: " + messageString);
                for (WebSocketChannel peer : channel.getPeerConnections()) {
                    WebSockets.sendText(messageString, peer, null);
                }
            }
            @Override
            protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) {
                System.out.println("Socket closed");
            }
        };
    }

}
