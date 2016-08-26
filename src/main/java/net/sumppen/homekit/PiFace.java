package net.sumppen.homekit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PiFace {
	private int input_port;
	private int output_port;
	
	public int getInput_port() {
		return input_port;
	}
	public void setInput_port(int input_port) {
		this.input_port = input_port;
	}
	public int getOutput_port() {
		return output_port;
	}
	public boolean isOutputActive(int port) {
		return (output_port & (2^port))!= 0;
	}
	public void setOutput_port(int output_port) {
		this.output_port = output_port;
	}
	public void setOutputState(int id, boolean newState) {
		if(newState){
			output_port |= (1 << id); 
		} else {
			output_port &= ~(1 << id);
		}
	}

}
