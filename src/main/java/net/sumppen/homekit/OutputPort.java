package net.sumppen.homekit;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.client.RestTemplate;

import com.beowulfe.hap.HomekitAccessory;
import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.Service;
import com.beowulfe.hap.accessories.Outlet;

public class OutputPort implements Outlet {
	public enum Port {
		PORT_0(0),WATERHEATER(1, true),HEATING_ENTRANCE(2),
		HEATING_LIVINGROOM(3),HEATING_KITCHEN(4),
		HEATING_UPSTAIRS_NORTH(5),HEATING_UPSTAIRS_SOUTH(6),
		PORT_7(7), INVALID(-1);
		
		private final int id;
		private final boolean inUse;
		Port(int id) {
			this(id, false);
		}
		Port(int id, boolean inUse) {
			this.id = id;
			this.inUse = inUse;
		}
		int getId() {
			return id;
		}
		public String getModel() {
			return "p"+id;
		}
		public boolean isInUse() {
			return inUse;
		}
	}

	private static final URI URL = null;

	private final Port port;
	private RestTemplate restTemplate = new RestTemplate();
	
	public OutputPort(Port port) {
		this.port = port;
	}

	public int getId() {
		return port.getId();
	}

	public String getLabel() {
		return port.toString();
	}

	public String getManufacturer() {
		return "Sumppen Inc";
	}

	public String getModel() {
		return port.getModel();
	}

	public String getSerialNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	public void identify() {
		// TODO Auto-generated method stub

	}

	public CompletableFuture<Boolean> getOutletInUse() {
		return CompletableFuture.completedFuture(port.isInUse());
	}

	public CompletableFuture<Boolean> getPowerState() {
		PiFace state = restTemplate.getForObject(URL, PiFace.class);
		return CompletableFuture.completedFuture(state.isOutputActive(port.id));
	}

	public CompletableFuture<Void> setPowerState(boolean newState) throws Exception {
		PiFace state = restTemplate.getForObject(URL, PiFace.class);
		state.setOutputState(port.id,newState);
		Map<String,Integer> map = new HashMap<String, Integer>();
		state = restTemplate.getForObject(URL, PiFace.class,map );
		return null;
	}

	public void subscribeOutletInUse(HomekitCharacteristicChangeCallback arg0) {
		// TODO Auto-generated method stub
		
	}

	public void subscribePowerState(HomekitCharacteristicChangeCallback arg0) {
		// TODO Auto-generated method stub
		
	}

	public void unsubscribeOutletInUse() {
		// TODO Auto-generated method stub
		
	}

	public void unsubscribePowerState() {
		// TODO Auto-generated method stub
		
	}

}
