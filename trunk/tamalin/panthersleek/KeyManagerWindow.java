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
 * KeyManagerWindow.java
 *
 * Created on Nov 30, 2008, 12:46:54 PM
 */
package org.tamalin.panthersleek;

import javax.crypto.KeyGenerator;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Quytelda K. Gaiwin
 */
public class KeyManagerWindow extends JFrame
{
    /**
     * Creates new form KeyManagerWindow
     */
    public KeyManagerWindow()
    {
        ArrayList<PantherKey> keys = Panther.getPantherKeys();

        initComponents();

        URL imageURL = Panther.class.getResource("/panthersleek/resources/dock-logo.png");
        Image img = Toolkit.getDefaultToolkit().getImage(imageURL);
        setIconImage(img);

        //Create the list model to be used on the JList of keys.
        model = new DefaultListModel();

        //Add existing keys to the model.
        for (PantherKey key : keys)
        {
            if (key != null)
            {
                model.addElement(key.getDisplayName());
            }
        }

        //Set the list to use the model.
        keyList.setModel(model);

        //Configure the list to select only 1 element at a time.
        keyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Size the window
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        double w = screen.getWidth();
        double h = screen.getHeight();
        int width = (int) w / 2 -  50;
        int height = (int) h / 3;

        this.setSize(width, height);
    }

    /**
     * Adds a newly created key to the JList box on the left.
     *
     * @param key the key to add
     */
    public void addNewCreatedKey(PantherKey key)
    {
        model.addElement(key.getDisplayName());
        Panther.addKey(key);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents()
    {
        /*#################################*
         * Initialize Interface Components *
         *#################################*/
        ResourceBundle bundle = ResourceBundle.getBundle(Panther.BUNDLE_PATH, Locale.getDefault());

        JScrollPane jScrollPane1 = new JScrollPane();
        keyList = new javax.swing.JList();
        tools = new JToolBar(JToolBar.HORIZONTAL);
        JButton newKey = new JButton();
        JButton deleteKey = new JButton();
        JButton closeButton = new JButton();
        JButton exportKeyButton = new JButton();
        JButton importKeyButton = new JButton();
        JButton findKeyFingerprintButton = new JButton();

        this.setTitle(bundle.getString("keymanager.title"));
        tools.setFloatable(false);

        jScrollPane1.setViewportView(keyList);

        newKey.setText(bundle.getString("keys.button.newkey"));
        newKey.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newKeyButtonActionPerformed();
            }
        });

        deleteKey.setText(bundle.getString("keys.button.rmkey"));
        deleteKey.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteKeyButtonActionPerformed();
            }
        });

        closeButton.setText(bundle.getString("keys.button.close"));
        closeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                closeButtonActionPerformed();
            }
        });

        exportKeyButton.setText(bundle.getString("keys.button.exportkey"));
        exportKeyButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportKeyButtonActionPerformed();
            }
        });

        importKeyButton.setText(bundle.getString("keys.button.importkey"));
        importKeyButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                importKeyButtonActionPerformed();
            }
        });

        findKeyFingerprintButton.setText(bundle.getString("keys.button.computefingerprint"));
        findKeyFingerprintButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                findKeyFingerprintButtonActionPerformed();
            }
        });

        if(System.getProperty("os.name").equals("Mac OS X"))
        {
            String buttonType = "segmentedTextured";
            this.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
            newKey.putClientProperty("JButton.buttonType", buttonType);
            newKey.putClientProperty("JButton.segmentPosition", "first");
            deleteKey.putClientProperty("JButton.buttonType", buttonType);
            deleteKey.putClientProperty("JButton.segmentPosition", "last");
            closeButton.putClientProperty("JButton.buttonType", buttonType);
            closeButton.putClientProperty("JButton.segmentPosition", "only");
            exportKeyButton.putClientProperty("JButton.buttonType", buttonType);
            exportKeyButton.putClientProperty("JButton.segmentPosition", "last");
            importKeyButton.putClientProperty("JButton.buttonType", buttonType);
            importKeyButton.putClientProperty("JButton.segmentPosition", "first");
            findKeyFingerprintButton.putClientProperty("JButton.buttonType", buttonType);
            findKeyFingerprintButton.putClientProperty("JButton.segmentPosition", "only");

            newKey.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSAddTemplate")));
            newKey.setText("");
            deleteKey.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSRemoveTemplate")));
            deleteKey.setText("");
        }


        /*#############*
         * Layout Code *
         *#############*/
        this.setLayout(new BorderLayout());
        this.add(tools, BorderLayout.NORTH);
        this.add(keyList, BorderLayout.CENTER);

        tools.add(newKey);
        tools.add(deleteKey);
        tools.add(importKeyButton);
        tools.add(exportKeyButton);
        tools.add(findKeyFingerprintButton);
        tools.add(closeButton);
        for(Component c : tools.getComponents())
                c.setFocusable(false);
    }

    /**
     * Creates a new key with user collected by displaying a JDialog.
     */
    private void newKeyButtonActionPerformed()
    {
        //Create and show a New Key Dialog.
        NewKeyDialog keyDialog = new NewKeyDialog(this, true, ResourceBundle.getBundle(Panther.BUNDLE_PATH, Locale.getDefault()).getString("keys.window.title"));
        keyDialog.setVisible(true);

        if (keyDialog.isOkay())
        {
            byte[] seed = keyDialog.getSeed();
            SecureRandom random = new SecureRandom(seed);
            Key key = null;
            try
            {
                //Generate a new key.
                KeyGenerator generator = KeyGenerator.getInstance(Panther.getAlgorithm());
                generator.init(random);
                key = generator.generateKey();
            }
            catch (NoSuchAlgorithmException ex)
            {
                ex.printStackTrace();
            }

            //Add the key to Panther.
            PantherKey pKey = new PantherKey(key, Panther.getNumberOfKeys() + 1, keyDialog.getKeyDisplayName());
            this.addNewCreatedKey(pKey);
        }
    }

    private void deleteKeyButtonActionPerformed()
    {
        String keyName = (String) keyList.getSelectedValue();
        PantherKey key = Panther.getKeyByDisplayName(keyName);

        if (key == null) return;

        //Make sure the user really want to delete the key.
        String msg = ResourceBundle.getBundle(Panther.BUNDLE_PATH, Locale.getDefault()).getString("keys.label.deletesure").replace("{1}", keyName);
        int result = JOptionPane.showConfirmDialog(this, msg, "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION)
        {
            boolean removed = model.removeElement(key.getDisplayName());
            if (!removed)
            {
                JOptionPane.showMessageDialog(this, "Unable to remove key from list!", "Key Removal Error", JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                Panther.removeKey(key);
                Panther.refreshKeyChoices();
            }
        }
    }

    private void closeButtonActionPerformed()
    {
        this.setVisible(false);
    }

    private void importKeyButtonActionPerformed()
    {
        if (jfc == null)
            jfc = new JFileChooser();

        int open = jfc.showOpenDialog(this);

        if (open == JFileChooser.APPROVE_OPTION)
        {
            File file = jfc.getSelectedFile();
            KeyImporterExporter ie = new KeyImporterExporter(file);
            KeyDepositor depositor = new KeyDepositor()
            {
                public void loadKey(PantherKey key)
                {
                    addNewCreatedKey(key);
                    Panther.refreshKeyChoices();
                }
            };
            ie.installKeyDepositor(depositor);
            Thread importThread = new Thread(ie);
            importThread.start();
        }
    }

    private void exportKeyButtonActionPerformed()
    {
        //Export the selected key to file.
        String keyName = (String) keyList.getSelectedValue();
        if (keyName == null) return; //exit method if the key is null

        PantherKey key = Panther.getKeyByDisplayName(keyName);
        if (key == null) return;

        if (jfc == null)
        {
            jfc = new JFileChooser();
        }

        int save = jfc.showSaveDialog(this);

        if (save == JFileChooser.APPROVE_OPTION)
        {
            File file = jfc.getSelectedFile();
            Thread exportThread = new Thread(new KeyImporterExporter(file, key));
            exportThread.start();
        }
    }

    private void findKeyFingerprintButtonActionPerformed()
    {
        //Get the key.
        String keyName = (String) keyList.getSelectedValue();
        PantherKey key = Panther.getKeyByDisplayName(keyName);

        //Make sure the key isn't null;
        if (key == null) return;

        //Create an array to hold the fingerprint;

        //Get the bytes from the keys toString() method.
        byte[] keyEncoding = key.getKey().getEncoded();

        String viewableDigest = Panther.getDigestAsString(keyEncoding);

        //Show the viewable digest.
        JPanel contentPanel = new JPanel();
        JTextField digestField = new JTextField(viewableDigest.length() - (viewableDigest.length() / 3));
        digestField.setText(viewableDigest);
        contentPanel.add(new JLabel(Panther.getDigestAlgorithm() + " Fingerprint:"));
        contentPanel.add(digestField);
        JOptionPane.showMessageDialog(this, contentPanel, "Fingerprint of \"" + key.getDisplayName() + "\"", JOptionPane.PLAIN_MESSAGE);
    }

    private javax.swing.JList keyList;
    private JToolBar tools;
    private DefaultListModel model;
    private JFileChooser jfc;
}