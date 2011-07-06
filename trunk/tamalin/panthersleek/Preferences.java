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
 * Preferences.java
 *
 * Created on Sep 5, 2009, 7:24:41 AM
 */

package org.tamalin.panthersleek;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

/**
 * @author Quytelda K. Gaiwin
 */
public class Preferences extends javax.swing.JDialog
{

    /**
     * Creates new form Preferences
     *
     * @param modal  whether or not the dialog is modal
     * @param parent the parent frame of the dialog
     */
    public Preferences(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents()
    {
        JPanel languagePanel = new JPanel();
        JPanel encryptionAlgPanel = new JPanel();
        JPanel digestAlgPanel = new JPanel();
        JPanel optionPanel = new JPanel();
        JPanel algorithmPanel = new JPanel();
        ButtonGroup languages = new ButtonGroup();
        english = new javax.swing.JRadioButton();
        chinese = new javax.swing.JRadioButton();
        JButton change = new JButton();
        JButton jButton1 = new JButton();
        encryptionAlgorithmChooser = new JComboBox(new String[]{"AES", "DES", "Blowfish", "DESede"});
        digestAlgorithmChooser = new JComboBox(new String[]{"SHA-1", "MD5", "MD2", "SHA-256", "SHA-512"});

        languages.add(english);
        english.setText(new Locale("en").getDisplayLanguage());
        english.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                englishActionPerformed();
            }
        });

        if(Locale.getDefault().getLanguage().equals("en"))
            english.setSelected(true);
        else
            chinese.setSelected(true);

        languages.add(chinese);
        chinese.setText(new Locale("zh").getDisplayLanguage());

        change.setText("Confirm");
        change.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                changeActionPerformed();
            }
        });

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed();
            }
        });

        encryptionAlgorithmChooser.setSelectedItem(Panther.getAlgorithm());
        digestAlgorithmChooser.setSelectedItem(Panther.getDigestAlgorithm());

        /* Configure the borders. */
        TitledBorder languageBorder = BorderFactory.createTitledBorder("Languages");
        TitledBorder encryptionAlgBorder = BorderFactory.createTitledBorder("Encryption Algorithm");
        TitledBorder digestAlgBorder = BorderFactory.createTitledBorder("Fingerprint Algorithm");

        languagePanel.setBorder(languageBorder);
        encryptionAlgPanel.setBorder(encryptionAlgBorder);
        digestAlgPanel.setBorder(digestAlgBorder);

        /* Layout code. */
        languagePanel.add(english);
        languagePanel.add(chinese);

        encryptionAlgPanel.add(encryptionAlgorithmChooser);
        digestAlgPanel.add(digestAlgorithmChooser);

        optionPanel.setLayout(new GridLayout(1, 2));
        optionPanel.add(change);
        optionPanel.add(jButton1);

        algorithmPanel.setLayout(new GridLayout(2, 1));

        algorithmPanel.add(encryptionAlgPanel);
        algorithmPanel.add(digestAlgPanel);

        this.setLayout(new BorderLayout());
        this.add(languagePanel, BorderLayout.NORTH);
        this.add(algorithmPanel, BorderLayout.CENTER);
        this.add(optionPanel, BorderLayout.SOUTH);

        pack();

        this.setResizable(false);
    }

    private void englishActionPerformed()
    {

    }

    private void changeActionPerformed()
    {
        confirmed = true;
        if (english.isSelected())
            locale = Locale.ENGLISH;
        else if (chinese.isSelected())
            locale = Locale.CHINESE;
        this.setVisible(false);
    }

    private void jButton1ActionPerformed()
    {
        confirmed = false;
        this.setVisible(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                Preferences dialog = new Preferences(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter()
                {
                    public void windowClosing(java.awt.event.WindowEvent e)
                    {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    /**
     * Returns the locale that is currently selected in the dialog.
     *
     * @return the selected locale
     */
    public Locale getLocaleChoice()
    {
        return locale;
    }

    /**
     * Returns the encryption algorithm selected in the dialog from the
     * JComboBox.
     *
     * @return the standard name of the selected encryption algorithm
     */
    public String getChosenAlgorithm()
    {
        return (String) encryptionAlgorithmChooser.getSelectedItem();
    }

    public String getChosenDigestAlgorithm()
    {
        return (String) digestAlgorithmChooser.getSelectedItem();
    }

    public Insets getInsets()
    {
        return new Insets(25, 10, 10, 10);
    }

    public boolean getConfirmed()
    {
        return confirmed;
    }


    private boolean confirmed = false;
    private javax.swing.JRadioButton chinese;
    private javax.swing.JRadioButton english;
    private JComboBox encryptionAlgorithmChooser, digestAlgorithmChooser;
    private Locale locale = null;
}
