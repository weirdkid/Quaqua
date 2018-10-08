package test;



import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;



public class IcnsTest {

	public static void main(String[] args) {

		BufferedImage image = null;
		IcnsTest itest = new IcnsTest();
    	
    	try{
    		//image = ImageIO.read(itest.getClassLoaderResource("/icns/SidebarDesktopFolder.icns"));
    		image = ImageIO.read(new File("/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/SidebarHomeFolder.icns"));
    		
    		System.out.println(image.getHeight() + " x " + image.getWidth());
    		
//    		image = ImageIO.read(itest.getClassLoaderResource("/icns/appStore.icns"));
//    		System.out.println(image.getHeight() + " x " + image.getWidth());
    		
    	}catch(Exception e) {
    		System.err.println(e);
    		e.printStackTrace(System.err);
    	}
    	
    	
    	JFrame frame = new JFrame();
    	frame.setSize(200, 200);
    	frame.setLayout(new BorderLayout());
    	
    	frame.getContentPane().add(new JLabel(new ImageIcon(image)), "Center");
    	
    	frame.setVisible(true);

	}
	
	protected URL getClassLoaderResource(final String pName) {
        return getClass().getResource(pName);
    }

}
