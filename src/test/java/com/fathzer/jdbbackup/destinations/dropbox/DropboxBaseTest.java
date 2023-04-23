package com.fathzer.jdbbackup.destinations.dropbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.dropbox.core.http.StandardHttpRequestor.Config;
import com.fathzer.plugin.loader.utils.ProxySettings;

class DropboxBaseTest {
	@Test
	void test() {
		DropboxBase base = new DropboxBase();
		assertEquals(Proxy.NO_PROXY, base.getProxy());
		assertNull(base.getProxyAuthentication());
		base.setProxy(ProxySettings.fromString("host:3128").toProxy(), null);
		assertEquals(new InetSocketAddress("host",3128), base.getProxy().address());
		
		// Test proxy is set
		{
			final Config.Builder builder = mock(Config.Builder.class);
			try (MockedStatic<Config> config = mockStatic(Config.class)) {
				config.when(Config::builder).thenReturn(builder);
				ArgumentCaptor<Proxy> captor = ArgumentCaptor.forClass(Proxy.class);
				assertNotNull(base.getConfig());
				verify(builder).withProxy(captor.capture());
				assertEquals(new InetSocketAddress("host",3128), captor.getValue().address());
				
				assertNull(Authenticator.getDefault());
			}
		}

		// Test proxy auth is set
		ProxySettings settings = ProxySettings.fromString("a:b@host:3128");
		base.setProxy(settings.toProxy(), settings.getLogin());
		assertNotNull(base.getConfig());
		PasswordAuthentication login = Authenticator.getDefault().requestPasswordAuthenticationInstance(null, null, 0, null, null, null, null, null);
		assertEquals("a", login.getUserName());
		assertArrayEquals(new char[]{'b'}, login.getPassword());

		
		base.setProxy(Proxy.NO_PROXY, null);
		assertEquals(Proxy.NO_PROXY, base.getProxy());
		
		base.setDbxAppInfoSupplier(() -> DropboxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile1.properties"));
		assertThrows(RuntimeException.class, () -> base.getAppInfo());
		base.setDbxAppInfoSupplier(() -> DropboxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile2.properties"));
		assertThrows(RuntimeException.class, () -> base.getAppInfo());
		
		
		assertThrows(IllegalArgumentException.class, () -> base.setProxy(null, null));
		final PasswordAuthentication auth = new PasswordAuthentication("a", new char[0]);
		assertThrows(IllegalArgumentException.class, () -> base.setProxy(Proxy.NO_PROXY, auth));

		
	}
}
