package astechzgo.luminescent.utils;

import java.util.logging.ConsoleHandler;

public class EConsoleHandler extends ConsoleHandler {

	public EConsoleHandler() {
		setOutputStream(System.out);
	}
}
