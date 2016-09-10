package net.sumppen.homekit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

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
				props.setProperty("pin",createPin());
				saveParams(props, name);
			}
			mac = props.getProperty("mac");
			salt = new BigInteger(props.getProperty("salt"));
			privateKey = readKey();
			pin = props.getProperty("pin");
		} catch (IOException e) {
		}
		System.out.println("The PIN for pairing is "+pin);
	}
	
	private byte[] readKey() throws InvalidAlgorithmParameterException, IOException {
		byte[] key; 			
		Path keyFile = Paths.get("private.key");
		if(Files.exists(keyFile)) {
			key = Files.readAllBytes(keyFile);
		} else {
			key = HomekitServer.generateKey();
			Files.write(keyFile, key);
		}

		return key;
	}

	private void saveParams(Properties props, String name) throws IOException {
		log.info("Saving properties "+name);
        File f = new File(name);
        OutputStream out = new FileOutputStream( f );
        props.store(out, "Stored");
    }

	private String createPin() {
		Random rnd = new Random();
		String pin = String.format("%03d-%02d-%03d", rnd.nextInt(1000), rnd.nextInt(100), rnd.nextInt(1000));
		return pin;
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
		Path users = Paths.get("users");
		if(Files.notExists(users))
			Files.createDirectory(users);
		if(!Files.isDirectory(users))
			throw new IOException(users.toAbsolutePath()+" exists but is not a directory");
		
		for(String userName : userKeyMap.keySet()) {
			Path user = users.resolve(userName);
			if(!Files.exists(user)) {
				Files.write(user, userKeyMap.get(userName));
			}
		}
	}

	@Override
	public void removeUser(String username) {
		userKeyMap.remove(username);
		try {
			Path user = getUserPath(username);
			if(Files.exists(user)) 
				Files.delete(user);
		} catch (IOException e) {
			log.error("Failed to save users",e);
		}
		System.out.println("Removed pairing for "+username);
	}

	private Path getUserPath(String username) throws IOException {
		Path users = Paths.get("users");
		if(Files.notExists(users))
			Files.createDirectory(users);
		if(!Files.isDirectory(users))
			throw new IOException(users.toAbsolutePath()+" exists but is not a directory");
		Path user = users.resolve(username);
		return user;
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		if(!userKeyMap.containsKey(username)) {
			try {
				loadUser(username);
			} catch (IOException e) {
			}
		}
		return userKeyMap.get(username);
	}

	private void loadUser(String username) throws IOException {
		log.info("Loading "+username);
		Path user = getUserPath(username);
		byte[] bytes;
		if(Files.exists(user)) {
			bytes = Files.readAllBytes(user);
			userKeyMap.putIfAbsent(username, bytes);
			log.info(username+" loaded");
		} else {
			log.info(username+" was not found");
		}
		
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