package com.fathzer.jdbbackup.destinations.dropbox;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.junit.jupiter.api.Test;

import com.fathzer.plugin.loader.utils.ProxySettings;

class DropboxBaseTest {
	@Test
	void test() {
		DropboxBase base = new DropboxBase();
		assertEquals(Proxy.NO_PROXY, base.getProxy());
		assertNull(base.getProxyAuthentication());
		base.setProxy(ProxySettings.fromString("host:3128").toProxy(), null);
		assertEquals(new InetSocketAddress("host",3128), base.getProxy().address());
		base.setProxy(Proxy.NO_PROXY, null);
		assertEquals(Proxy.NO_PROXY, base.getProxy());
		
		base.setDbxAppInfoSupplier(() -> DropboxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile1.properties"));
		assertThrows(RuntimeException.class, () -> base.getAppInfo());
		base.setDbxAppInfoSupplier(() -> DropboxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile2.properties"));
		assertThrows(RuntimeException.class, () -> base.getAppInfo());
	}
}
