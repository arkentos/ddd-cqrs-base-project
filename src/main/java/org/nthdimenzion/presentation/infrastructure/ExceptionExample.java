package org.nthdimenzion.presentation.infrastructure;

import com.adamtaft.eb.BusExceptionEvent;
import com.adamtaft.eb.EventBusService;
import com.adamtaft.eb.EventHandler;

import javax.management.RuntimeErrorException;

public class ExceptionExample {
    @EventHandler
	public void throwRuntimeException(String event) {
		throw new RuntimeException("Some RuntimeException");
	}

	@EventHandler
	public void throwException(String event) throws Exception {
		throw new Exception("Some Exception");
	}

	@EventHandler
	public void HandleExceptions(BusExceptionEvent event) {
		System.out.println("Exception Handled was: " + event.getCause());
        throw new RuntimeException("Throw exception in handle exception");
	}

	public static void main(String[] args) {
		ExceptionExample ee = new ExceptionExample();
		EventBusService.subscribe(ee);

		EventBusService.publish("Some String Event");
	}
}
