/*
 * @(#)PasswordFieldTest.java  1.0  13 February 2005
 *
 * Copyright (c) 2004 Werner Randelshofer, Immensee, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

package test;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;
import javax.swing.text.JTextComponent;
/**
 * PasswordFieldTest.
 *
 * @author  Werner Randelshofer
 * @version 1.0  13 February 2005  Created.
 */
public class PasswordFieldTest extends javax.swing.JPanel {
    
    /** Creates new form. */
    public PasswordFieldTest() {
        initComponents();
        
       // Prevent text components from becoming too small in GridBagLayout
        for (Component c : getComponents()) {
            if (c instanceof JTextComponent) {
                c.setMinimumSize(c.getPreferredSize());
            }
        }
                      // Try to get a better layout with J2SE6
        try {
            int BASELINE_LEADING = GridBagConstraints.class.getDeclaredField("BASELINE_LEADING").getInt(null);
            GridBagLayout layout = (GridBagLayout) getLayout();
            for (Component c : getComponents()) {
                GridBagConstraints gbc = layout.getConstraints(c);
                if (gbc.anchor == GridBagConstraints.WEST) {
                    gbc.anchor = BASELINE_LEADING;
                    layout.setConstraints(c, gbc);
                }
            }
        } catch (Exception ex) {
            // bail
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jLabel8 = new javax.swing.JLabel();
        jPasswordField3 = new javax.swing.JPasswordField();
        jLabel9 = new javax.swing.JLabel();
        jPasswordField7 = new javax.swing.JPasswordField();
        jLabel11 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPasswordField4 = new javax.swing.JPasswordField();
        jLabel10 = new javax.swing.JLabel();
        jPasswordField5 = new javax.swing.JPasswordField();
        jPasswordField6 = new javax.swing.JPasswordField();
        jSeparator2 = new javax.swing.JSeparator();
        jPasswordField8 = new javax.swing.JPasswordField();
        jLabel12 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 17, 17, 17));
        setLayout(new java.awt.GridBagLayout());

        jPasswordField1.setText("Ångström H");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField1, gridBagConstraints);

        jLabel7.setText("Editable");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel7, gridBagConstraints);

        jPasswordField2.setEditable(false);
        jPasswordField2.setText("Ångström H");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField2, gridBagConstraints);

        jLabel8.setText("Non-Editable");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel8, gridBagConstraints);

        jPasswordField3.setText("Ångström H");
        jPasswordField3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField3, gridBagConstraints);

        jLabel9.setText("Disabled");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel9, gridBagConstraints);

        jPasswordField7.setText("Ångström H");
        jPasswordField7.setEchoChar('\u2023');
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField7, gridBagConstraints);

        jLabel11.setText("Custom echo character");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel11, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jSeparator1, gridBagConstraints);

        jPasswordField4.setFont(new java.awt.Font("Lucida Grande", 0, 11));
        jPasswordField4.setText("Ångström H");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField4, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 0, 11));
        jLabel10.setText("Small Size");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel10, gridBagConstraints);

        jPasswordField5.setEditable(false);
        jPasswordField5.setFont(new java.awt.Font("Lucida Grande", 0, 11));
        jPasswordField5.setText("Ångström H");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField5, gridBagConstraints);

        jPasswordField6.setFont(new java.awt.Font("Lucida Grande", 0, 11));
        jPasswordField6.setText("Ångström H");
        jPasswordField6.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator2, gridBagConstraints);

        jPasswordField8.setBackground(new java.awt.Color(181, 212, 107));
        jPasswordField8.setText("Ångström H");
        jPasswordField8.setBorder(null);
        jPasswordField8.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPasswordField8, gridBagConstraints);

        jLabel12.setText("Borderless and Colored");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 99;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JPasswordField jPasswordField3;
    private javax.swing.JPasswordField jPasswordField4;
    private javax.swing.JPasswordField jPasswordField5;
    private javax.swing.JPasswordField jPasswordField6;
    private javax.swing.JPasswordField jPasswordField7;
    private javax.swing.JPasswordField jPasswordField8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
    
}
