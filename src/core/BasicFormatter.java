package core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BasicFormatter extends Formatter
{
	@Override
	public String format(LogRecord record)
	{
		return "[" + new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(new Date(record.getMillis())) + "][" 
				+ record.getLevel().getName() + "] "
				+ record.getMessage() + "\n";
	}
}
