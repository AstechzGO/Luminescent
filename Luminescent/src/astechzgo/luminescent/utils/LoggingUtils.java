package astechzgo.luminescent.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

public class LoggingUtils {

	public static final String LOGGER_NAME = "Logger";
	public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
	
	public static final File LOG_FILE = newFile("logs/latest.log");
	
	/**
	 * Configures the logger
	 */
	public static void configureRobotLogger() {
		if(!newFile("logs").exists())
			newFile("logs").mkdir();
			
		try {
			final FileHandler fh = new FileHandler(LOG_FILE.getAbsolutePath(), true);
			fh.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fh);
			
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
			    @Override
			    public void run()
			    {
			    	LOGGER.info("Closing logger");
			    	fh.flush();
			    	fh.close();
			    }
			});
			
		} catch (SecurityException | IOException e) {
			//Utils.logException(LOGGER, e);
			e.printStackTrace();
		}
	}
}
