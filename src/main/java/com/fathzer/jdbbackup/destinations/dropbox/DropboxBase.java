package com.fathzer.jdbbackup.destinations.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Authenticator;
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

/** Common component between {@link com.fathzer.jdbbackup.destinations.dropbox.DropboxManager} and {@link com.fathzer.jdbbackup.destinations.dropbox.token.DropboxTokenCmd}
 */
public class DropboxBase {
	// Please note this class does not implement ProxyCompliant to break the runtime dependency to jdbbackup-core
	// This allows DropboxTokenCmd to be executed directly from the jar
	/** A prefix that distinguish refresh tokens from legacy eternal access tokens. */
	protected static final String REFRESH_PREFIX = "refresh-";
	private static final String NAME = "jDbBackup";

	/** A function that build a DbxAppInfo instance from a resource file path. The properties resource file should contain
	 * <i>appKey</i> and <i>appSecret</i> keys.
	 */
	public static final Function<String,DbxAppInfo> RESOURCE_PROPERTY_APP_INFO_BUILDER = resName -> {
		try (InputStream in = DropboxBase.class.getResourceAsStream(resName)) {
			if (in==null) {
				throw new IOException("Unable to find "+resName+" resource");
			}
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
	private Proxy proxy = Proxy.NO_PROXY;
	private PasswordAuthentication proxyAuth;
	private Supplier<DbxAppInfo> dbxAppInfoProvider = () -> RESOURCE_PROPERTY_APP_INFO_BUILDER.apply("keys.properties");

	/** Sets the proxy.
	 * @param proxy The proxy to use to connect to destination ({@link Proxy#NO_PROXY} for disabling proxy).
	 * @param auth The proxy authentication (null if the proxy does not require authentication).
	 * @throws IllegalArgumentException if proxy is null or if auth is not null and proxy is {@link Proxy#NO_PROXY}.
	 */
	public void setProxy(Proxy proxy, PasswordAuthentication auth) {
		if (proxy==null) {
			throw new IllegalArgumentException("Use Proxy.NO_PROXY instead of null");
		}
		if (Proxy.NO_PROXY.equals(proxy) && auth!=null) {
			throw new IllegalArgumentException("Can't set no proxy with login");
		}
		this.proxy = proxy;
		this.proxyAuth = auth;
		this.config = null;
	}
	
	/** Gets the current proxy
	 * @return The proxy or Proxy.NO_PROXY if no proxy is set.
	 * @see #setProxy(Proxy, PasswordAuthentication)
	 */
	protected Proxy getProxy() {
		return this.proxy;
	}
	
	/** Gets the current proxy authentication
	 * @return The proxy authentication or null if no proxy authentication is set.
	 * @see #setProxy(Proxy, PasswordAuthentication)
	 */
	protected PasswordAuthentication getProxyAuthentication() {
		return this.proxyAuth;
	}
	
	/** Gets the Dropbox application identification information.
	 * @return a DbxAppInfo instance
	 */
	protected DbxAppInfo getAppInfo() {
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
			if (!Proxy.NO_PROXY.equals(proxy)) {
				if (proxyAuth != null) {
					Authenticator.setDefault(new Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return proxyAuth;
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
