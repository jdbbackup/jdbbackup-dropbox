package com.fathzer.jdbbackup.destinations.dropbox.token;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.Proxy.Type;

class ProxySettings {
	final Proxy proxy;
	final PasswordAuthentication login;
	
	ProxySettings(String proxy) {
		try {
			final URI uri = new URI("http://"+proxy);
			final String host = uri.getHost();
			if (host==null) {
				throw new IllegalArgumentException("missing host");
			}
			final int port = uri.getPort();
			if (port<=0) {
				throw new IllegalArgumentException("missing port");
			}
			this.proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
			final String userAndPwd = uri.getUserInfo();
			if (userAndPwd!=null && !userAndPwd.trim().isEmpty()) {
				final int index = userAndPwd.indexOf(':');
				this.login = index<0 ? new PasswordAuthentication(userAndPwd, new char[0]) : new PasswordAuthentication(userAndPwd.substring(0,index), userAndPwd.substring(index+1).toCharArray());
			} else {
				this.login = null;
			}
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("argument should be of the form [user:pwd@]host:port",e);
		}
	}
}