package com.fathzer.jdbbackup.destinations.dropbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.fathzer.jdbbackup.cmd.CommandLineSupport;
import com.fathzer.jdbbackup.cmd.ProxySettingsConverter;
import com.fathzer.plugin.loader.utils.ProxySettings;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** A helper class to obtain a token usable with DropBoxManager.
 * <br>Run this class with -h argument to know available options.
 */
@Command(name = "java com.fathzer.jdbbackup.destinations.dropbox.DropBoxTokenCmd", usageHelpAutoWidth = true, description = {"Gets a token from Dropbox.","You will need a web browser to complete the process"})
public class DropboxTokenCmd extends DropboxBase implements Callable<Integer>, CommandLineSupport {
	@Option(names={"-p","--proxy"}, description="The proxy used to communicate with Dropbox, format is [user[:pwd]@]host:port", converter = ProxySettingsConverter.class)
	private ProxySettings proxy;
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
	boolean usageHelpRequested;
	
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
        out("1. Go to: " + authorizeUrl);
        out("2. Click \"Allow\" (you might have to log in first)");
        out("3. Enter the authorization code there:");
		try {
			String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			out("Please wait ...");
	        DbxAuthFinish authFinish = auth.finishFromCode(code);
	        String accessToken = authFinish.getRefreshToken();
	        out("Your token is: "+REFRESH_PREFIX+accessToken);
	        out("Keep it in a secure place as it allows to access to your backup folder on Dropbox");
		} catch (Exception e) {
			err ("Sorry, an error occurred:");
			err(e);
		}
	}
}
