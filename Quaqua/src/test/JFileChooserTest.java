package test;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

public class JFileChooserTest extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JFileChooserTest() {
		super("JFileChooserTest");
	
		
		getContentPane().add(new JLabel("chooser will popup"), BorderLayout.CENTER);
		setSize(300,200);
		
	}
	
	public void showFD() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.putClientProperty("JFileChooser.packageIsTraversable", Boolean.FALSE);
		//fileChooser.putClientProperty("JFileChooser.packageIsTraversable", "never");
		//fileChooser.putClientProperty("JFileChooser.appBundleIsTraversable", "never");
		
		boolean istrav = fileChooser.isTraversable(new File("/Users/mhovey/Documents/testout/testpackage.rge"));
		
		System.out.println("is traversible: " + istrav);
		
		//int result = fileChooser.showOpenDialog(this);
		//System.out.println("result: " + result);
	}
	
	public static void main(String[] args) {
		
//		try{
//			UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
//			//UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//		}catch(Exception e){
//			System.err.println("unable to load quaqua");
//		}
		
		JFileChooserTest app = new JFileChooserTest();
		app.setVisible(true);
		app.showFD();

	}

}
