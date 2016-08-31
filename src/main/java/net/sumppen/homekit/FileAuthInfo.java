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
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

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
	
	private final Logger log = Logger.getLogger(FileAuthInfo.class);

	public FileAuthInfo() throws InvalidAlgorithmParameterException {
		Properties props;
		String name = "server.properties";
		try {
			props = loadParams(name);
			if(props.isEmpty()) {
				props.setProperty("mac",HomekitServer.generateMac());
				props.setProperty("salt", HomekitServer.generateSalt().toString());
				props.setProperty("privateKey",DatatypeConverter.printHexBinary(HomekitServer.generateKey()));
				props.setProperty("pin",createPin());
				saveParams(props, name);
			}
			mac = props.getProperty("mac");
			salt = new BigInteger(props.getProperty("salt"));
			privateKey = DatatypeConverter.parseHexBinary(props.getProperty("privateKey"));
			pin = props.getProperty("pin");
		} catch (IOException e) {
		}
		System.out.println("The PIN for pairing is "+pin);
	}
	
	private void saveParams(Properties props, String name) throws IOException {
		log.info("Saving properties "+name);
        File f = new File(name);
        OutputStream out = new FileOutputStream( f );
        props.store(out, "Stored");
    }

	private String createPin() {
		return PIN;
	}

	public Properties loadParams(String name) throws IOException {
		Properties props = new Properties();
		InputStream is = null;

		try {
			File f = new File(name);
			is = new FileInputStream( f );
			props.load( is );
		} catch (IOException e) {
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
		log.info("Create user: "+username);
		if(userKeyMap.putIfAbsent(username, publicKey) == null) {
			try {
				saveUsers();
			} catch (IOException e) {
				log.error("Failed to save users",e);
			}
		}
		System.out.println("Added pairing for "+username);
	}

	private void saveUsers() throws IOException {
		Properties props = new Properties();
		for(String userName : userKeyMap.keySet()) {
			props.setProperty(userName, DatatypeConverter.printHexBinary(userKeyMap.get(userName)));
		}
		saveParams(props, "users.properties");
	}

	@Override
	public void removeUser(String username) {
		userKeyMap.remove(username);
		try {
			saveUsers();
		} catch (IOException e) {
			log.error("Failed to save users",e);
		}
		System.out.println("Removed pairing for "+username);
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		if(userKeyMap.isEmpty()) {
			try {
				loadUsers();
			} catch (IOException e) {
			}
		}
		return userKeyMap.get(username);
	}

	private void loadUsers() throws IOException {
		Properties props = loadParams("users.properties");
	    Enumeration e = props.propertyNames();

	    while (e.hasMoreElements()) {
	      String name = (String) e.nextElement();
	      userKeyMap.putIfAbsent(name, DatatypeConverter.parseHexBinary(props.getProperty(name)));
		}
	}

}