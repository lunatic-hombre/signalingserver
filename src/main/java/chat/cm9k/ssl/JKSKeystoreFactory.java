package chat.cm9k.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class JKSKeystoreFactory implements KeystoreFactory {

    @Override
    public KeyStore createKeystore(Path path, String format, char[] password) {
        try {
            return getKeystore(path, format, password);
        } catch (RuntimeException e) {
            // cannot retrieve existing, create new one
            try {
                final KeyStore keyStore = KeyStore.getInstance(format);
                keyStore.load(null, password);
                return keyStore;
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void saveKeystore(Path path, char[] password, KeyStore keyStore) {
        try (OutputStream out = Files.newOutputStream(path)) {
            keyStore.store(out, password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public KeyStore getKeystore(Path path, String format, char[] password) {
        try {
            try (InputStream is = Files.newInputStream(path)) {
                KeyStore loadedKeystore = KeyStore.getInstance(format);
                loadedKeystore.load(is, password);
                return loadedKeystore;
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public KeyManager[] getKeyManager(Path path, String format, char[] password) {
        try {
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final KeyStore keystore = getKeystore(path, format, password);
            keyManagerFactory.init(keystore, password);
            return keyManagerFactory.getKeyManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }

}
