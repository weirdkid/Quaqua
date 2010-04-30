/*
 * @(#)JPanel.java  1.0  19 March 2005
 *
 * Copyright (c) 2004 Werner Randelshofer
 * Hausmatt 10, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms. 
 */

package test;

import ch.randelshofer.quaqua.util.Methods;
import javax.swing.*;
/**
 * JPanel.
 *
 * @author  Werner Randelshofer
 * @version 1.0  19 March 2005  Created.
 */
public class ViewControlsTest extends javax.swing.JPanel {
    
    /** Creates new form. */
    public ViewControlsTest() {
        initComponents();
        
        tabbedPane.add(new LazyPanel("test.ListTest"), "List");
        tabbedPane.add(new LazyPanel("test.TableTest"), "Table");
        tabbedPane.add(new LazyPanel("test.TreeTest"), "Tree");
        tabbedPane.add(new LazyPanel("test.ScrollPaneTest"), "ScrollPane");
        tabbedPane.add(new LazyPanel("test.BrowserTest"), "Browser");
            
        Methods.invokeIfExists(tabbedPane, "setTabLayoutPolicy", 0); // JTabbedPane.WRAP_TAB_LAYOUT);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        add(tabbedPane, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
}