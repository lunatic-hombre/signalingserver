package chat.cm9k;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.xnio.Options;
import org.xnio.Sequence;

import static io.undertow.Handlers.websocket;

public class ServerMain {

    public static void main(String[] args) {
        try {
            final Undertow server = Undertow.builder()
                    .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                    // TLS 1.3 causes infinite loop in SSL handshake https://issues.jboss.org/browse/UNDERTOW-1422
                    .setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of("TLSv1.2"))
                    .setHandler(websocket(new SignalingCallback()))
                    .addHttpListener(8080, "127.0.0.1")
                    .build();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
