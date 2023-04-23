package com.fathzer.jdbbackup.destinations.dropbox.token;

import java.net.Proxy;
import java.util.concurrent.Callable;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.fathzer.jdbbackup.destinations.dropbox.DropboxBase;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

/** A helper class to obtain a token usable with DropBoxManager.
 * <br>Run this class with -h argument to know available options.
 */
@Command(name = "java com.fathzer.jdbbackup.destinations.dropbox.DropBoxTokenCmd", usageHelpAutoWidth = true, description = {"Gets a token from Dropbox.","You will need a web browser to complete the process"})
public class DropboxTokenCmd extends DropboxBase implements Callable<Integer> {
	/** A converter that converts a String to a ProxySettings instance.
	 */
	static class ProxySettingsConverter implements ITypeConverter<ProxySettings> {
		@Override
		public ProxySettings convert(String value) throws Exception {
			return new ProxySettings(value);
		}
	}
	
	@Option(names={"-p","--proxy"}, description="The proxy used to communicate with Dropbox, format is [user[:pwd]@]host:port", converter = ProxySettingsConverter.class)
	ProxySettings proxy;
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
	private boolean usageHelpRequested;
	
	protected Console console = new Console() {};
	
	/** Launches the command.
	 * @param args The command arguments. Run the class with -h arguments to know available options.
	 */
	public static void main(String... args) {
		System.exit(doIt(args));
    }

	static int doIt(String... args) {
		return new CommandLine(new DropboxTokenCmd()).execute(args);
	}
	
	void setConsole(Console console) {
		this.console = console;
	}

	@Override
	public Integer call() throws Exception {
		try {
			if (proxy!=null) {
				setProxy(proxy.proxy, proxy.login);
			} else {
				setProxy(Proxy.NO_PROXY, null);
			}
			getToken();
			return ExitCode.OK;
        } catch (Exception e) {
			console.err ("Sorry, an error occurred:");
        	console.err(e);
        	return ExitCode.SOFTWARE;
        }
	}

	void getToken() throws DbxException {
	    DbxAppInfo appInfo = getAppInfo();
	    DbxWebAuth auth = new DbxWebAuth(getConfig(), appInfo);
	    DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
	             .withNoRedirect()
	             .withTokenAccessType(TokenAccessType.OFFLINE)
	             .build();
        String authorizeUrl = auth.authorize(authRequest);
        console.out("1. Go to: " + authorizeUrl);
        console.out("2. Click \"Allow\" (you might have to log in first)");
        console.out("3. Enter the authorization code there:");
		String code = console.getCommand();
		console.out("Please wait ...");
        DbxAuthFinish authFinish = auth.finishFromCode(code);
        String accessToken = authFinish.getRefreshToken();
        console.out("Your token is: "+REFRESH_PREFIX+accessToken);
        console.out("Keep it in a secure place as it allows to access to your backup folder on Dropbox");
	}
}
