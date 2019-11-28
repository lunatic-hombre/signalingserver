package chat.cm9k.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class KeystoreSSLContextFactory implements SSLContextFactory {

    private static final TrustManager ACCEPT_ALL_TRUST_MANAGER = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers(){
            return null;
        }
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // do nothing
        }
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // do nothing
        }
    };

    @Override
    public SSLContext getContext(KeyManager[] keyManagers) {
        try {
            final SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{ACCEPT_ALL_TRUST_MANAGER}, null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }


}
