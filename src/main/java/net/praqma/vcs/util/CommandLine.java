package net.praqma.vcs.util;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineException;

public class CommandLine {
	private static net.praqma.util.execute.CommandLineInterface cli = net.praqma.util.execute.CommandLine.getInstance();
	
	private static Logger logger = Logger.getLogger();
	
	public static CmdResult run(  String cmd ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( cmd, null, false, false );
	}
	
	public static CmdResult run(  String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( cmd, dir, false, false );
	}
	
	public static CmdResult run(  String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( cmd, dir, merge, ignore );
	}
}
