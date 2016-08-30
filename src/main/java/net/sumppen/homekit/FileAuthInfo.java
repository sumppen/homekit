package net.sumppen.homekit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitServer;

/**
 * This is a simple implementation that should never be used in actual production. The mac, salt, and privateKey
 * are being regenerated every time the application is started. The user store is also not persisted. This means pairing
 * needs to be re-done every time the app restarts.
 *
 * @author Andy Lintner
 */
public class FileAuthInfo implements HomekitAuthInfo {

	private static final String PIN = "031-45-154";

	private String mac;
	private BigInteger salt;
	private byte[] privateKey;
	private String pin;
	private final ConcurrentMap<String, byte[]> userKeyMap = new ConcurrentHashMap<>();

	public FileAuthInfo() throws InvalidAlgorithmParameterException {
		Properties props;
		String name = "server.properties";
		try {
			props = loadParams(name);
			mac = props.getProperty("mac");
			salt = new BigInteger(props.getProperty("salt"));
			privateKey = props.getProperty("privateKey").getBytes();
			pin = props.getProperty("pin");
		} catch (IOException e) {
		}
		System.out.println("The PIN for pairing is "+pin);
	}

	private void saveParams(Properties props, String name) throws IOException {
        File f = new File(name);
        OutputStream out = new FileOutputStream( f );
        props.store(out, "Stored");
    }

	private String createPin() {
		return PIN;
	}

	public Properties loadParams(String name) throws IOException, InvalidAlgorithmParameterException {
		Properties props = new Properties();
		InputStream is = null;

		try {
			File f = new File(name);
			is = new FileInputStream( f );
			props.load( is );
		} catch (IOException e) {
			props.setProperty("mac",HomekitServer.generateMac());
			props.setProperty("salt", HomekitServer.generateSalt().toString());
			props.setProperty("privateKey",new String(HomekitServer.generateKey()));
			props.setProperty("pin",createPin());
			saveParams(props, name);
		}
		return props;
	}

	@Override
	public String getPin() {
		return pin;
	}

	@Override
	public String getMac() {
		return mac;
	}

	@Override
	public BigInteger getSalt() {
		return salt;
	}

	@Override
	public byte[] getPrivateKey() {
		return privateKey;
	}

	@Override
	public void createUser(String username, byte[] publicKey) {
		userKeyMap.putIfAbsent(username, publicKey);
		System.out.println("Added pairing for "+username);
	}

	@Override
	public void removeUser(String username) {
		userKeyMap.remove(username);
		System.out.println("Removed pairing for "+username);
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		return userKeyMap.get(username);
	}

}