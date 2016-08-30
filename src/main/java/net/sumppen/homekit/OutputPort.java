package net.sumppen.homekit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Switch;

public class OutputPort implements Switch {
	public enum Port {
		PORT_0(0),WATERHEATER(1,"water heater", true),HEATING_ENTRANCE(2),
		HEATING_LIVINGROOM(3),HEATING_KITCHEN(4),
		HEATING_UPSTAIRS_NORTH(5),HEATING_UPSTAIRS_SOUTH(6),
		PORT_7(7);
		
		private final int id;
		private final boolean inUse;
		private String description;
		
		Port(int id) {
			this(id,null, false);
		}
		Port(int id, String description, boolean inUse) {
			this.id = id;
			this.inUse = inUse;
			this.description = description;
		}
		public String getModel() {
			return "p"+id;
		}
		public boolean isInUse() {
			return inUse;
		}
		@Override
		public String toString() {
			if(description != null)
				return description;
			return super.toString();
		}
	}

	static final String URL = "http://192.168.10.10:8000";

	private static final int OFFSET = 10;

	private final Port port;
	private RestOperations restTemplate = new RestTemplate();
	
	public RestOperations getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestOperations restTemplate) {
		this.restTemplate = restTemplate;
	}

	public OutputPort(Port port) {
		this.port = port;
	}

	public int getId() {
		return (port.id + OFFSET);
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

	public CompletableFuture<Boolean> getSwitchState() {
		PiFace state = restTemplate.getForObject(URL, PiFace.class);
		return CompletableFuture.completedFuture(state.isOutputActive(port.id));
	}

	public CompletableFuture<Void> setSwitchState(boolean state) throws Exception {
		PiFace oldState = restTemplate.getForObject(URL, PiFace.class);
		oldState.setOutputState(port.id,state);
		Map<String,Integer> map = new HashMap<String, Integer>();
		map.put("output_port", oldState.getOutput_port());
		oldState = restTemplate.getForObject(URL+"?output_port={output_port}", PiFace.class,map );
		return null;
	}

	public void subscribeSwitchState(HomekitCharacteristicChangeCallback callback) {
		// TODO Auto-generated method stub
		
	}

	public void unsubscribeSwitchState() {
		// TODO Auto-generated method stub
		
	}

}
