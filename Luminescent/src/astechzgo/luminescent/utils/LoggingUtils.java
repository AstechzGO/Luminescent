package astechzgo.luminescent.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

public class LoggingUtils {

	public static final String LOGGER_NAME = "Logger";
	public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
	
	public static final File LOG_FILE = newFile("logs/latest.log");
	
	private static final Formatter FORMATTER = new Formatter() {

		@Override
		public String format(LogRecord record) {
			// TODO Auto-generated method stub
			boolean debug = false;
			if(record.getLevel() == Level.CONFIG) {
				debug = true;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]");
			Date dt = new Date(record.getMillis());
			String S = sdf.format(dt);
			
			String threadName = null;
			for (Thread t : Thread.getAllStackTraces().keySet()) {
				if (t.getId()==record.getThreadID()) {
					threadName = t.getName();
				}
			}
			String msg = S + " [" + threadName + " thread/" + (debug ? "DEBUG" : record.getLevel().getName()) + "]: " + record.getMessage();
			
			return msg;
		}
		
	};
	
	/**
	 * Configures the logger
	 */
	public static void configureRobotLogger() {
		if(!newFile("logs").exists())
			newFile("logs").mkdir();
		
		try {
			final FileHandler fh = new FileHandler(LOG_FILE.getAbsolutePath(), true);
			fh.setFormatter(FORMATTER);
			LOGGER.getParent().getHandlers()[0].setFormatter(FORMATTER);
			LOGGER.addHandler(fh);
			
			if(Constants.getConstantAsBoolean(Constants.LOG_CONFIG)) {
				LOGGER.setLevel(Level.CONFIG);
				LOGGER.getParent().setLevel(Level.CONFIG);
				LOGGER.getParent().getHandlers()[0].setLevel(Level.CONFIG);
			}
			
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
			    @Override
			    public void run()
			    {
			    	fh.flush();
			    	fh.close();

			    	cleanupLogFilename();
			    }
			});
			
		} catch (SecurityException | IOException e) {
			//Utils.logException(LOGGER, e);
			e.printStackTrace();
		}
	}
	
	private static void cleanupLogFilename() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-");
		Date dt = new Date();
		String S = sdf.format(dt);
    	
		File datedFile = null;
		
		int i = 1;
		boolean exists = true;
				;
		while(exists) {
			datedFile = newFile("logs/" + S + i + ".log");
    	
			if(datedFile.exists()) {
				i++;
			}
			else {
				exists = false;
			}
		}

        // Rename file (or directory)
        boolean success = LOG_FILE.renameTo(datedFile);
        if (!success) {
            System.err.println("Unknown Error");
        }
	}
}
