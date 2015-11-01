package core;

public class Core
{
	public static void main(String[] args)
	{
		Program prog = new Program(args);
		System.out.println("Program exit with: " + parseExitCode(prog.run()));
	}

	public static String parseExitCode(int code)
	{
		// TODO
		return "" + code;
	}

}
