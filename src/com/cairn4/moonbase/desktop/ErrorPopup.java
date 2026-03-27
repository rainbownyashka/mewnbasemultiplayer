/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class ErrorPopup
extends JFrame {
    public ErrorPopup(String message) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (!"Nimbus".equals(info.getName())) continue;
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(3);
        jframe.setSize(600, 400);
        jframe.setVisible(true);
        GridLayout layout = new GridLayout(0, 1);
        jframe.setLayout(layout);
        JLabel heading = new JLabel("MewnBase has crashed!", 0);
        jframe.add(heading);
        JPanel messagePanel = new JPanel(new BorderLayout());
        jframe.add(messagePanel);
        TitledBorder title = BorderFactory.createTitledBorder("Error");
        messagePanel.setBorder(title);
        JLabel errorMessage = new JLabel(message, 2);
        messagePanel.add(errorMessage);
        JButton b = new JButton("Exit");
        jframe.add(b);
    }
}

