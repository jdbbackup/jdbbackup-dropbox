package com.fathzer.jdbbackup.destinations.dropbox.token;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWebAuth;
import com.fathzer.jdbbackup.destinations.dropbox.DropboxBase;

import picocli.CommandLine.ExitCode;

class DropboxTokenCmdTest {
	private static class Silent implements Console {
		private final String command;
		private List<String> out;
		
		Silent(String command) {
			this.command = command;
			this.out = new ArrayList<>();
		}

		@Override
		public void out(String message) {
			this.out.add(message);
		}

		@Override
		public void err(String message) {
		}

		@Override
		public void err(Throwable e) {
		}

		@Override
		public String getCommand() {
			return command;
		}
	}

	@Test
	void testCall() throws Exception {
		final Field proxyField = DropboxBase.class.getDeclaredField("proxy");
		proxyField.setAccessible(true);
		final Field proxyLoginField = DropboxBase.class.getDeclaredField("proxyAuth");
		proxyLoginField.setAccessible(true);
		
		final DropboxTokenCmd cmd = new DropboxTokenCmd() {
			@Override
			void getToken() throws DbxException {
				if ("err".equals(console.getCommand())) {
					throw new DbxException("An error occured");
				}
			}
		};
		
		// Test error
		cmd.setConsole(new Silent("err"));
		assertEquals(ExitCode.SOFTWARE, cmd.call());
		
		// Test no proxy
		cmd.setConsole(new Silent(null));
		assertEquals(0, cmd.call());
		assertEquals(Proxy.NO_PROXY, (Proxy) proxyField.get(cmd));
		
		// Test with proxy
		cmd.setConsole(new Silent(null));
		cmd.proxy = new ProxySettings("host:4321");
		assertEquals(0, cmd.call());
		assertEquals(new InetSocketAddress("host", 4321), ((Proxy) proxyField.get(cmd)).address());
		PasswordAuthentication auth = (PasswordAuthentication) proxyLoginField.get(cmd);
		assertNull(auth);
		
		// Test with proxy and password
		cmd.proxy = new ProxySettings("a:b@host:1234");
		assertEquals(0, cmd.call());
		assertEquals(new InetSocketAddress("host", 1234), ((Proxy) proxyField.get(cmd)).address());
		auth = (PasswordAuthentication) proxyLoginField.get(cmd);
		assertEquals("a",auth.getUserName());
		assertArrayEquals(new char[] {'b'}, auth.getPassword());

		// Test with no proxy ... again
		cmd.proxy = null;
		assertEquals(0, cmd.call());
		assertEquals(Proxy.NO_PROXY, (Proxy) proxyField.get(cmd));
		assertNull(proxyLoginField.get(cmd));
	}
	
	@Test
	void testGetToken() throws Exception {
		DropboxTokenCmd cmd = new DropboxTokenCmd();
		final String url = "http://dropbox/url";
		try (MockedConstruction<DbxWebAuth> mock = mockConstruction(DbxWebAuth.class, (auth,context) -> {
			when(auth.authorize(any())).thenReturn(": "+url);
			when(auth.finishFromCode("ok")).thenReturn(new DbxAuthFinish("access", 10L, "refresh", "user", "team", "account", "state"));
			when(auth.finishFromCode("ko")).thenThrow(DbxException.class);
		})) {
			// Test when auth is ok
			Silent console = new Silent("ok");
			cmd.setConsole(console);
			cmd.getToken();
			assertTrue(console.out.get(0).endsWith(url));
			assertTrue(console.out.get(console.out.size()-2).endsWith(": refresh-refresh"));

			// Test when auth is ko
			console = new Silent("ko");
			cmd.setConsole(console);
			assertThrows(Exception.class, () -> cmd.getToken());
		}
	}

}
