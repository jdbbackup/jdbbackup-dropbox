package com.fathzer.jdbbackup.managers.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.http.StandardHttpRequestor.Config;
import com.fathzer.jdbbackup.utils.ProxySettings;

/** Common component between {@link com.fathzer.jdbbackup.managers.dropbox.DropboxManager} and {@link com.fathzer.jdbbackup.managers.dropbox.DropboxTokenCmd}
 */
public class DropboxBase {
	/** A prefix that distinguish refresh tokens from legacy eternal access tokens. */
	protected static final String REFRESH_PREFIX = "refresh-";
	private static final String NAME = "jDbBackup";

	/** A function that build a DbxAppInfo instance from a resource file path. The properties resource file should contain
	 * <i>appKey</i> and <i>appSecret</i> keys.
	 */
	public static final Function<String,DbxAppInfo> RESOURCE_PROPERTY_APP_INFO_BUILDER = resName -> {
		try (InputStream in = DropboxBase.class.getResourceAsStream(resName)) {
			final Properties properties = new Properties();
			properties.load(in);
			String key = getKey(properties,resName, "appKey");
			String secret = getKey(properties,resName, "appSecret");
			return new DbxAppInfo(key, secret);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	};
	
	private static String getKey(Properties properties, String resName, String key) {
		final String value = properties.getProperty(key);
		if (value==null || value.isBlank()) {
			throw new MissingResourceException("Property file is incorrect",resName,key);
		}
		return value;
	}
	
	private DbxRequestConfig config;
	private ProxySettings proxySettings;
	private Supplier<DbxAppInfo> dbxAppInfoProvider = () -> RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("keys.properties");

	/** Sets the proxy settings to be used by the Dropbox API.
	 * @param settings The proxy settings
	 */
	public void setProxy(final ProxySettings settings) {
		this.proxySettings = settings;
		this.config = null;
	}
	
	/** Gets the current proxy settings
	 * @return The proxy settings or null if no proxy is set.
	 * @see #setProxy(ProxySettings)
	 */
	protected ProxySettings getProxySettings() {
		return this.proxySettings;
	}
	
	DbxAppInfo getAppInfo() {
		return dbxAppInfoProvider.get();
	}
	
	/** Gets the Dropbox request configuration.
	 * <br>This implementation return a configuration that uses the standard http requestor, with, the proxy settings applied.
	 * <br>You can override this method to build a configuration on other bases. 
	 * @return a {@link DbxRequestConfig} instance
	 */
	protected DbxRequestConfig getConfig() {
		if (config==null) {
			Config.Builder builder = Config.builder();
			if (proxySettings!=null && proxySettings.getHost()!=null) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxySettings.getHost(),proxySettings.getPort()));
				if (proxySettings.getLogin() != null) {
					Authenticator.setDefault(new Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return proxySettings.getLogin();
						}
					});
				}
				builder.withProxy(proxy);
			}

			DbxRequestConfig.Builder rbuilder = DbxRequestConfig.newBuilder(NAME);
			rbuilder.withHttpRequestor(new StandardHttpRequestor(builder.build()));
			this.config = rbuilder.build();
		}
		return config;
	}

	/** Sets the supplier of Dropbox application's credentials.
	 * <br>By default, the library uses the jdbbackup application's credential stored in keys.properties resource file.
	 * <br>You can switch to another application of your choice by passing another supplier to this method.
	 * @param dbxAppInfoProvider The new application credentials supplier
	 * @see DropboxBase#RESOURCE_PROPERTY_APP_INFO_BUILDER
	 */
	public void setDbxAppInfoSupplier(Supplier<DbxAppInfo> dbxAppInfoProvider) {
		this.dbxAppInfoProvider = dbxAppInfoProvider;
	}
}
