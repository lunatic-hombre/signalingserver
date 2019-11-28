package chat.cm9k.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

public interface SSLContextFactory {

    SSLContext getContext(KeyManager[] keyManagers);

}
