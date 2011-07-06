/*
 * Copyright 2010 Quytelda Gaiwin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * PasswordPrompt.java
 *
 * Created on Feb 3, 2010, 4:38:32 PM
 */

package org.tamalin.panthersleek;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Quytelda K. Gaiwin
 */
public class PasswordPrompt extends javax.swing.JDialog
{

    /**
     * Creates new form PasswordPrompt
     *
     * @param parent the parent frame, can be null
     * @param modal  whether or not the dialog is modal.  Considering it's nature, this should usually be true.
     * @param label  the included dialog text.
     */
    public PasswordPrompt(java.awt.Frame parent, boolean modal, String label)
    {
        super(parent, modal);
        initComponents();
        this.promptLabel.setText("<html><font size=\"3\">" + label + "</font></html>");
        this.doLayoutUI();
        this.getRootPane().setDefaultButton(ok);

        //Center the window
        int ww = this.getWidth();
        int wh = this.getHeight();
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        int sw = ss.width;
        int sh = ss.height;
        int x = (sw / 2) - (ww / 2);
        int y = (sh / 2) - (wh / 2);

        this.setLocation(x, y);

        URL imageURL = Panther.class.getResource("/panthersleek/resources/locked.png");
        Image img = Toolkit.getDefaultToolkit().getImage(imageURL);
        setIconImage(img);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents()
    {

        ok = new javax.swing.JButton();
        cancel = new JButton();
        passwordLabel = new JLabel();
        passwordField = new javax.swing.JPasswordField();
        promptLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        if(System.getProperty("os.name").equals("Mac OS X"))
            this.setTitle("");
        else
            setTitle("Password?");

        ok.setText(ResourceBundle.getBundle(Panther.BUNDLE_PATH).getString("continue"));
        ok.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                okActionPerformed();
            }
        });

        cancel.setText(ResourceBundle.getBundle(Panther.BUNDLE_PATH).getString("close"));
        cancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelActionPerformed();
            }
        });

        passwordLabel.setText(ResourceBundle.getBundle(Panther.BUNDLE_PATH).getString("panther.generic.pwd"));        
    }

    private void doLayoutUI()
    {
        /*#############*
         * Layout Code *
         *#############*/
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addComponent(ok)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancel))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(passwordLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(promptLabel)))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(promptLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(passwordLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cancel)
                                .addComponent(ok))
                        .addContainerGap())
        );

        pack();
        this.setResizable(false);
    }

    private void okActionPerformed()
    {
        //store the password in an array
        password = passwordField.getPassword();

        //close the window
        this.setVisible(false);
    }

    private void cancelActionPerformed()
    {
        //set the password to null
        password = null;

        //close the window
        this.setVisible(false);
    }

    public char[] getPassword()
    {
        return password;
    }

    @Override
    public Insets getInsets()
    {
        return new Insets(25, 10, 10, 10);
    }

    public void cleanUp()
    {
        if (password == null)
            return;

        for (int i = 0; i < password.length; i++)
            password[i] = 0;

        dispose();
    }

    private javax.swing.JButton ok, cancel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel promptLabel, passwordLabel;
    private char[] password = null;
}
