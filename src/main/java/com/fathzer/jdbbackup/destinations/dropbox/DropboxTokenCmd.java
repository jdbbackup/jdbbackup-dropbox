package com.fathzer.jdbbackup.destinations.dropbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.fathzer.plugin.loader.utils.ProxySettings;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

/** A helper class to obtain a token usable with DropBoxManager.
 * <br>Run this class with -h argument to know available options.
 */
@Command(name = "java com.fathzer.jdbbackup.destinations.dropbox.DropBoxTokenCmd", usageHelpAutoWidth = true, description = {"Gets a token from Dropbox.","You will need a web browser to complete the process"})
public class DropboxTokenCmd extends DropboxBase implements Callable<Integer> {
	/** A converter that converts a String to a ProxySettings instance.
	 */
	private static class ProxySettingsConverter implements ITypeConverter<ProxySettings> {
		@Override
		public ProxySettings convert(String value) throws Exception {
			return ProxySettings.fromString(value);
		}
	}
	
	/** A class that acts as a command line.
	 */
	public interface CommandLineSupport {
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

	}
	
	@Option(names={"-p","--proxy"}, description="The proxy used to communicate with Dropbox, format is [user[:pwd]@]host:port", converter = ProxySettingsConverter.class)
	private ProxySettings proxy;
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
	boolean usageHelpRequested;
	
	private CommandLineSupport clSupport = new CommandLineSupport() {};
	
	/** Launches the command.
	 * @param args The command arguments. Run the class with -h arguments to know available options.
	 */
	public static void main(String... args) {
		System.exit(new CommandLine(new DropboxTokenCmd()).execute(args));
    }

	@Override
	public Integer call() throws Exception {
		if (proxy!=null) {
			setProxy(proxy.toProxy(), proxy.getLogin());
		}
		getToken();
		return 0;
	}

	private void getToken() {
	    DbxAppInfo appInfo = getAppInfo();
	    DbxWebAuth auth = new DbxWebAuth(getConfig(), appInfo);
	    DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
	             .withNoRedirect()
	             .withTokenAccessType(TokenAccessType.OFFLINE)
	             .build();
        String authorizeUrl = auth.authorize(authRequest);
        clSupport.out("1. Go to: " + authorizeUrl);
        clSupport.out("2. Click \"Allow\" (you might have to log in first)");
        clSupport.out("3. Enter the authorization code there:");
		try {
			String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			clSupport.out("Please wait ...");
	        DbxAuthFinish authFinish = auth.finishFromCode(code);
	        String accessToken = authFinish.getRefreshToken();
	        clSupport.out("Your token is: "+REFRESH_PREFIX+accessToken);
	        clSupport.out("Keep it in a secure place as it allows to access to your backup folder on Dropbox");
		} catch (Exception e) {
			clSupport.err ("Sorry, an error occurred:");
			clSupport.err(e);
		}
	}
}
