package net.sumppen.homekit;

import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

import net.sumppen.homekit.OutputPort.Port;

/**
 * Java Homekit application
 *
 */
public class Main 
{
	private static final int PORT = 9123;
	public static Logger log = Logger.getLogger(Main.class);

	public static void main( String[] args )
	{
		try {
			//byte[] addr = {(byte) 192,(byte) 168,10,(byte) 137};
			HomekitServer homekit = new HomekitServer(PORT);
			HomekitRoot bridge = homekit.createBridge(new MockAuthInfo(), "Lindberg Bridge", "Sumppen Inc.", "G6", "111abel234");
			for(Port port : Port.values()) {
				OutputPort accessory = new OutputPort(port);
				if(port.isInUse()) {
					log.info("Adding accessory: "+accessory.getId());
					bridge.addAccessory(accessory);
				}
			}
			bridge.start();
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}
}
