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
 * NewKeyDialog.java
 *
 * Created on Nov 30, 2008, 12:55:33 PM
 */
package org.tamalin.panthersleek;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Quytelda K. Gaiwin
 */
public class NewKeyDialog extends javax.swing.JDialog
{

    /**
     * Creates new form NewKeyDialog
     *
     * @param parent the parent frame, can be null
     * @param modal  whether or not the dialog is modal
     * @param title  the String title of the dialog
     */
    public NewKeyDialog(java.awt.Frame parent, boolean modal, String title)
    {
        super(parent, modal);
        initComponents();

        //Set the title and default button.
        setTitle(title);
        this.getRootPane().setDefaultButton(createKeyButton);
        int x = parent.getX();
        int y = parent.getY();
        this.setLocation(x + 10, y + 10);
    }

    public byte[] getSeed()
    {
        return generatedSeed;
    }

    public String getKeyDisplayName()
    {
        return keyDisplayName.getText();
    }

    public boolean isOkay()
    {
        return ok;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents()
    {

        ResourceBundle bundle = ResourceBundle.getBundle(Panther.BUNDLE_PATH, Locale.getDefault());
        createKeyButton = new javax.swing.JButton();
        JButton cancelButton = new JButton();
        JLabel jLabel1 = new JLabel();
        keyDisplayName = new javax.swing.JTextField();
        JLabel jLabel2 = new JLabel();
        randomSeed = new javax.swing.JTextField();
        JButton generateButton = new JButton();
        JLabel jLabel3 = new JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        createKeyButton.setText(bundle.getString("keys.button.createkey"));
        createKeyButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                createKeyButtonActionPerformed();
            }
        });

        cancelButton.setText(bundle.getString("panther.generic.cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelButtonActionPerformed();
            }
        });

        jLabel1.setText(bundle.getString("keys.label.keyname"));

        jLabel2.setText(bundle.getString("keys.label.seed"));

        generateButton.setText(bundle.getString("keys.button.generate"));
        generateButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generateButtonActionPerformed();
            }
        });

        jLabel3.setText(bundle.getString("keys.label.warning"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel3))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(keyDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(randomSeed, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(generateButton))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(cancelButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(createKeyButton)))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(keyDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(randomSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(generateButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(createKeyButton)
                                .addComponent(cancelButton))
                        .addContainerGap())
        );

        pack();
        this.setResizable(false);
    }

    private void createKeyButtonActionPerformed()
    {
        //Check to make sure that the user filled out all required information.
        boolean okay = true;
        if ((generatedSeed == null) && (randomSeed.getText().equals("")))
        {
            okay = false;
        }
        else if (keyDisplayName.getText().equals(""))
        {
            okay = false;
        }
        else if ((Panther.getKeyByDisplayName(keyDisplayName.getText())) != null)
        {
            okay = false;
            JOptionPane.showMessageDialog(this, "This name is already in use.");
        }
        else if (randomSeed.getText().getBytes().length < 20)
        {
            okay = false;
            JOptionPane.showMessageDialog(this, "There must be at least 20 characters in the Random Seed field.");
        }

        if (okay)
        {
            //Set the ok flag to true.
            ok = true;

            //Obtain bytes to create the seed from.
            generatedSeed = randomSeed.getText().getBytes();

            //Close the dialog.
            setVisible(false);
        }
    }

    private void cancelButtonActionPerformed()
    {
        //Set the ok flag to false;
        ok = false;

        //Close the dialog.
        setVisible(false);
    }

    private void generateButtonActionPerformed()
    {
        int length = 21;

        //Generate a random seed for the given algorithm.
        SecureRandom random = new SecureRandom();
        byte[] seed = random.generateSeed(length);

        randomSeed.setText(new String(seed));
    }

    private javax.swing.JButton createKeyButton;
    private javax.swing.JTextField keyDisplayName;
    private javax.swing.JTextField randomSeed;
    private boolean ok = false;
    byte[] generatedSeed = null;
}
