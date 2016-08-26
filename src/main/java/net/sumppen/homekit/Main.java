package net.sumppen.homekit;

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
	
    public static void main( String[] args )
    {
		try {
			HomekitServer homekit = new HomekitServer(PORT);
			HomekitRoot bridge = homekit.createBridge(new MockAuthInfo(), "Test Bridge", "TestBridge, Inc.", "G6", "111abe234");
			for(Port port : Port.values()) {
				bridge.addAccessory(new OutputPort(port));
			}
			bridge.start();
		} catch (Exception e) {
			e.printStackTrace();
		}    }
}
