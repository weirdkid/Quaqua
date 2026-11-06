package com.weirdkid.quaqua.test;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Test harness that forces the Quaqua JFileChooser UI to fully initialize
 * (LAF install, UI delegate construction, FileSystemView creation, and
 * showing the dialog) so that any illegal reflective access or module
 * errors on newer JDKs surface immediately at startup.
 */
public class ChooserHarness {

	public static void main(String[] args) {
		System.out.println("Java: " + System.getProperty("java.version"));
		System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

		try {
			if (args.length > 0) {
				UIManager.setLookAndFeel((javax.swing.LookAndFeel) Class.forName(args[0]).getDeclaredConstructor().newInstance());
			} else {
				UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
			}
			System.out.println("LAF: " + UIManager.getLookAndFeel().getClass().getName());
		} catch (Throwable t) {
			System.err.println("LAF INIT FAILED:");
			t.printStackTrace();
			System.exit(2);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			try {
				JFileChooser fc = new JFileChooser();
				System.out.println("FileChooserUI: " + fc.getUI().getClass().getName());
				System.out.println("FileSystemView: " + fc.getFileSystemView().getClass().getName());

				JFrame frame = new JFrame("ChooserHarness");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(300, 100);
				frame.setVisible(true);

				Timer timer = new Timer(6000, e -> {
					System.out.println("Auto-closing dialog after 6s");
					fc.setVisible(false);
				});
				timer.setRepeats(false);
				timer.start();

				fc.showOpenDialog(frame);
				System.out.println("Chooser closed OK");
				System.exit(0);
			} catch (Throwable t) {
				System.err.println("CHOOSER FAILED:");
				t.printStackTrace();
				System.exit(1);
			}
		});
	}
}
