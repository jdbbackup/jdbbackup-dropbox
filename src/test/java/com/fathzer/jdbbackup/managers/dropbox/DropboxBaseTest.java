package com.fathzer.jdbbackup.managers.dropbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.MissingResourceException;

import org.junit.jupiter.api.Test;

import com.fathzer.jdbbackup.utils.ProxySettings;

class DropboxBaseTest {
	@Test
	void test() {
		DropboxBase base = new DropboxBase();
		assertNull(base.getProxySettings());
		base.setProxy(ProxySettings.fromString("host:3128"));
		assertNotNull(base.getProxySettings());
		base.setProxy(null);
		assertNull(base.getProxySettings());
		
		base.setDbxAppInfoSupplier(() -> DropboxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile1.properties"));
		assertThrows(MissingResourceException.class, () -> base.getAppInfo());
		base.setDbxAppInfoSupplier(() -> DropboxBase.RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("wrongAppFile2.properties"));
		assertThrows(MissingResourceException.class, () -> base.getAppInfo());
	}
}
