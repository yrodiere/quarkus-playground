package org.acme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyService {
	public String greet(String name) {
		return "Hello, " + name + "!";
	}
}
