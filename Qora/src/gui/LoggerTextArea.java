package gui;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class LoggerTextArea extends JTextArea {
	
	private Handler handler;
	private Logger logger;
	
	public LoggerTextArea(Logger logger)
	{
		super();
		
		//CREATE HANDLER
		this.handler = new TextComponentHandler(this);
		this.logger = logger;
		
		//DISABLE INPUT
		this.setLineWrap(true);
		this.setEditable(false);
	}
	
	@Override
	public void addNotify() 
	{
	    super.addNotify();
	    
	    for(Handler hh : this.logger.getHandlers())
	    {
	    	if (hh == this.handler)
	    	{
	    		return;
	    	}
	    }
	    
	    this.logger.addHandler(this.handler);
	}
	  
	
	@Override
	public void removeNotify() 
	{
	    super.removeNotify();
	    this.logger.removeHandler(this.handler);
	}
}


class TextComponentHandler extends Handler 
{
	private final JTextArea text;
	   
	TextComponentHandler(JTextArea text)
	{
		this.text = text;
		this.setFormatter(new SimpleFormatter());
	}
	  
	@Override
	public void publish(LogRecord record) 
	{
		if(isLoggable(record))
		{
			synchronized(text) 
		    {
				text.append(this.getFormatter().format(record));
		    }
		}   
	}
	
	@Override
	public void flush() {/**/}
	
	@Override
	public void close() throws SecurityException {/**/}
}
