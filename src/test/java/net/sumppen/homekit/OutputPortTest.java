package net.sumppen.homekit;

import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestOperations;

import net.sumppen.homekit.OutputPort.Port;

public class OutputPortTest {

	private OutputPort outputPort;
	private RestOperations mock;
	private PiFace piFace = new PiFace();

	@Before
	public void setupTest() {
		outputPort = new OutputPort(Port.PORT_0);
		mock = Mockito.mock(RestOperations.class);
		Mockito.when(mock.getForObject(Mockito.anyString(),Mockito.any())).thenReturn(piFace);
		outputPort.setRestTemplate(mock);
	}
	
	@Test
	public void testGetSwitchStateOff() throws InterruptedException, ExecutionException {
		CompletableFuture<Boolean> state = outputPort.getSwitchState();
		assertFalse(state.get());
	}
	@Test
	public void testGetSwitchStateOn() throws InterruptedException, ExecutionException {
		piFace.setOutputState(0, true);
		CompletableFuture<Boolean> state = outputPort.getSwitchState();
		assertTrue(state.get());
	}
	@Test
	public void testGetSpecificSwitchStateOff() throws InterruptedException, ExecutionException {
		piFace.setOutputState(1, true);
		piFace.setOutputState(2, true);
		piFace.setOutputState(3, true);
		piFace.setOutputState(4, true);
		piFace.setOutputState(5, true);
		piFace.setOutputState(6, true);
		piFace.setOutputState(7, true);
		CompletableFuture<Boolean> state = outputPort.getSwitchState();
		assertFalse(state.get());
	}

}
