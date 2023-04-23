package com.fathzer.jdbbackup.destinations.dropbox.token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

/** A class that acts as a command line.
 */
public interface Console {
	/** Output a message on the standard output.
	 * @param message The message to output
	 */
	@SuppressWarnings("java:S106")
	default void out(String message) {
		System.out.println(message);
	}
	
	/** Output a message on the standard error.
	 * @param message The message to output
	 */
	@SuppressWarnings("java:S106")
	default void err(String message) {
		System.err.println(message);
	}
	
	/** Output an exception on the standard error.
	 * @param e The exception to output
	 */
	@SuppressWarnings("java:S4507")
	default void err(Throwable e) {
		e.printStackTrace();
	}

	default String getCommand() {
		try {
			return new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}