// WHAT: The entry point. WHY: Java needs one class with main() to start the whole app.
package com.streamly;

import org.springframework.boot.SpringApplication; // helper that boots Spring
import org.springframework.boot.autoconfigure.SpringBootApplication; // "set everything up automatically"

@SpringBootApplication // scans this package for controllers/entities and auto-configures
public class StreamlyApplication {
	public static void main(String[] args) { // Java starts here
		SpringApplication.run(StreamlyApplication.class, args); // start server on port 8080
	}
}
