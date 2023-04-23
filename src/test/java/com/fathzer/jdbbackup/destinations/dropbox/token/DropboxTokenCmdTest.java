package com.fathzer.jdbbackup.destinations.dropbox.token;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import com.dropbox.core.DbxException;
import com.fathzer.jdbbackup.JDbBackup;
import com.fathzer.jdbbackup.destinations.dropbox.DropboxBase;

import picocli.CommandLine.ExitCode;

class DropboxTokenCmdTest {
	private static class Silent implements Console {
		private final String command;
		
		Silent(String command) {
			this.command = command;
		}

		@Override
		public void out(String message) {
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
		
		
/*		
		// No proxy
		try (MockedConstruction<DropboxTokenCmd> mock = mockConstruction(DropboxTokenCmd.class)) {
			assertEquals(0, DropboxTokenCmd.doIt());
			verify(mock.constructed().get(0), never()).setProxy(any(), any());
		}
		// With proxy
		try (MockedConstruction<DropboxTokenCmd> mock = mockConstruction(DropboxTokenCmd.class)) {
			assertEquals(0, DropboxTokenCmd.doIt("-p","host:4321"));
			final DropboxTokenCmd cmd = mock.constructed().get(0);
			ArgumentCaptor<Proxy> proxyCaptor = ArgumentCaptor.forClass(Proxy.class);
			ArgumentCaptor<PasswordAuthentication> loginCaptor = ArgumentCaptor.forClass(PasswordAuthentication.class);
			verify(cmd).setProxy(proxyCaptor.capture(), loginCaptor.capture());
			Proxy proxy = proxyCaptor.getValue();
			assertEquals(new InetSocketAddress("host", 4321), proxy.address());
			assertEquals("a", loginCaptor.getValue().getUserName());
			assertArrayEquals(new char[] {'b'}, loginCaptor.getValue().getPassword());
		}
		// With proxy + password
		try (MockedConstruction<DropboxTokenCmd> mock = mockConstruction(DropboxTokenCmd.class)) {
			assertEquals(0, DropboxTokenCmd.doIt("-p","a:b@host:1234"));
			final DropboxTokenCmd cmd = mock.constructed().get(0);
			ArgumentCaptor<Proxy> proxyCaptor = ArgumentCaptor.forClass(Proxy.class);
			ArgumentCaptor<PasswordAuthentication> loginCaptor = ArgumentCaptor.forClass(PasswordAuthentication.class);
			verify(cmd).setProxy(proxyCaptor.capture(), loginCaptor.capture());
			Proxy proxy = proxyCaptor.getValue();
			assertEquals(new InetSocketAddress("host", 1234), proxy.address());
			assertEquals("a", loginCaptor.getValue().getUserName());
			assertArrayEquals(new char[] {'b'}, loginCaptor.getValue().getPassword());
		}*/
	}

}
