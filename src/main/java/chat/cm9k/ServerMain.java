package chat.cm9k;

import chat.cm9k.ssl.JKSKeystoreFactory;
import chat.cm9k.ssl.KeystoreFactory;
import chat.cm9k.ssl.KeystoreSSLContextFactory;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.HttpString;
import org.xnio.Options;
import org.xnio.Sequence;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static chat.cm9k.Handlers.mappingDelegate;
import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

public class ServerMain {

    private static final Path RESOURCE_FOLDER = Paths.get("resources");
    private static final String WELCOME_FILE = "index.html";
    private static final int CACHE_TIME = (int) TimeUnit.DAYS.toSeconds(365);
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            final boolean prod = args.length > 0 && args[0].equals("prod");
            final HttpHandler handler = path()
                    .addPrefixPath("/connect", websocket(new SignalingCallback()))
                    .addPrefixPath("/", mappingDelegate(ServerMain::rewriteNonFileRequests, createResourceHandler()));
            final HttpHandler encodingHandler = new EncodingHandler(new ContentEncodingRepository()
                    .addEncodingHandler("gzip", new GzipEncodingProvider(), 100, Predicates.maxContentSize(5))
                    .addEncodingHandler("deflate", new DeflateEncodingProvider(), 50, Predicates.maxContentSize(5)))
				    .setNext(handler);
            final Undertow.Builder builder = Undertow.builder()
                    .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                    // TLS 1.3 causes infinite loop in SSL handshake https://issues.jboss.org/browse/UNDERTOW-1422
                    .setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of("TLSv1.2"))
                    .setHandler(encodingHandler);
            final Undertow server = prod
                    ? builder.addHttpsListener(443, "0.0.0.0", getSslContext()).build()
                    : builder.addHttpListener(PORT, "0.0.0.0").build();
            System.out.println("Starting server on port " + PORT);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HttpServerExchange rewriteNonFileRequests(HttpServerExchange exchange) {
        addAnyOriginHeaders(exchange);
        return exchange.getRequestPath().indexOf('.') < 0
                ? exchange.setRelativePath(WELCOME_FILE)
                : exchange;
    }

    private static ResourceHandler createResourceHandler() {
        return new ResourceHandler(new FileResourceManager(RESOURCE_FOLDER.toFile(), 0, true))
                .setCachable(exchange -> !exchange.getRelativePath().equals('/' + WELCOME_FILE))
                .setCacheTime(CACHE_TIME)
                .addWelcomeFiles(WELCOME_FILE);
    }

    private static void addAnyOriginHeaders(HttpServerExchange exchange) {
        exchange.getResponseHeaders()
                .add(HttpString.tryFromString("Access-Control-Allow-Origin"), "*")
                .add(HttpString.tryFromString("Access-Control-Allow-Headers"), "*")
                .add(HttpString.tryFromString("Access-Control-Expose-Headers"), "*");
    }

    private static SSLContext getSslContext() {
        try {
            final KeystoreFactory keys = new JKSKeystoreFactory();
            final char[] pwd = Files.lines(Paths.get("keystore.secret")).findFirst().get().toCharArray();
            final KeyManager[] keyManagers = keys.getKeyManager(Paths.get("keystore.jks"), "JKS", pwd);
            return new KeystoreSSLContextFactory().getContext(keyManagers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
