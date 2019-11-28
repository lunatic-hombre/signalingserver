package chat.cm9k.ssl;

import javax.net.ssl.KeyManager;
import java.nio.file.Path;
import java.security.KeyStore;

public interface KeystoreFactory {

    KeyStore createKeystore(Path path, String format, char[] password);

    void saveKeystore(Path path, char[] password, KeyStore keyStore);

    KeyStore getKeystore(Path path, String format, char[] password);

    KeyManager[] getKeyManager(Path path, String format, char[] password);

}
