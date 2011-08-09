package net.praqma.vcs.util;

import java.io.File;

import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineException;

public class CommandLine {
	private static net.praqma.util.execute.CommandLineInterface cli = net.praqma.util.execute.CommandLine.getInstance();
	
	public static CmdResult run(  String cmd ) throws CommandLineException, AbnormalProcessTerminationException {
		System.out.println(" $ " + cmd );
		return cli.run( cmd, null, false, false );
	}
	
	public static CmdResult run(  String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException {
		System.out.println(" $ " + cmd );
		return cli.run( cmd, dir, false, false );
	}
	
	public static CmdResult run(  String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
		System.out.println(" $ " + cmd );
		return cli.run( cmd, dir, merge, ignore );
	}
}
