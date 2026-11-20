package com.exam.test.exception;

/** Thrown when an operation targets a client id that does not exist. */
public class ClientNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClientNotFoundException(int id) {
		super("Client with id " + id + " not found");
	}
}
