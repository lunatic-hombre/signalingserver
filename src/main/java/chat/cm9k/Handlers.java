package chat.cm9k;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.function.UnaryOperator;

public class Handlers {

    public static HttpHandler mappingDelegate(UnaryOperator<HttpServerExchange> exchangeFunction, HttpHandler handler) {
        return new FunctionDelegateHandler(exchangeFunction, handler);
    }

    static class FunctionDelegateHandler implements HttpHandler {

        final UnaryOperator<HttpServerExchange> exchangeFunction;
        final HttpHandler delegate;

        public FunctionDelegateHandler(UnaryOperator<HttpServerExchange> exchangeFunction, HttpHandler delegate) {
            this.exchangeFunction = exchangeFunction;
            this.delegate = delegate;
        }

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            delegate.handleRequest(exchangeFunction.apply(httpServerExchange));
        }
    }

}
