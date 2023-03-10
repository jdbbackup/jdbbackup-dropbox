package com.fathzer.jdbbackup.managers.dropbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.MissingResourceException;

import org.junit.jupiter.api.Test;

import com.fathzer.jdbbackup.utils.ProxySettings;

class DropBoxBaseTest {
	@Test
	void test() {
		DropBoxBase base = new DropBoxBase();
		assertNull(base.getProxySettings());
		base.setProxy(ProxySettings.fromString("host:3128"));
		assertNotNull(base.getProxySettings());
		base.setProxy(null);
		assertNull(base.getProxySettings());
		
		base.setDbxAppInfoSupplier(() -> DropBoxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile1.properties"));
		assertThrows(MissingResourceException.class, () -> base.getAppInfo());
		base.setDbxAppInfoSupplier(() -> DropBoxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile2.properties"));
		assertThrows(MissingResourceException.class, () -> base.getAppInfo());
	}
}
