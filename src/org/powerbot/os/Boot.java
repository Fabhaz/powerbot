package org.powerbot.os;

import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.powerbot.os.ui.BotChrome;
import org.powerbot.os.util.PrintStreamHandler;

/**
 * @author Paris
 */
public class Boot implements Runnable {

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Boot());
	}

	@Override
	public void run() {
		final Logger logger = Logger.getLogger("");
		for (final Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}
		logger.addHandler(new PrintStreamHandler());

		if (Configuration.OS == Configuration.OperatingSystem.MAC) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}

		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("http.keepalive", "false");

		new BotChrome();
	}
}
