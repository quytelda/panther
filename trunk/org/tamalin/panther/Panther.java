/*
 * Copyright 2010 Quytelda K. Gaiwin
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

package org.tamalin.panther;

import org.tamalin.panther.crypt.CipherRunnable;
import org.tamalin.panther.file.FileSaveRunnable;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

/**
 * This class is the main class of the Panther program.
 * It contains the main method, and most of the GUI code.
 *
 * @author Quytelda K. Gaiwin
 * @version 4.0 August 7 2010
 * @since 1.0
 */
public class Panther extends JFrame implements Updatable
{

    /**
     * Creates the main window of the application, and loads all the related properties.
     *
     * @param locale The locale for which the GUI should be configured.
     */
    public Panther(Locale locale)
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        URL logoURL = Panther.class.getResource("/org/tamalin/panther/resources/icon_128.png");
        Image img = tk.getImage(logoURL);
        this.setIconImage(img);

        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            MacAppHandler app = new MacAppHandler(this, img);
            app.init();
        }

        Properties properties = loadLanguageResources(locale.getLanguage());

        /* Initialize the GUI in whatever language is selected. */

        /* Get the text for the about dialog. */
        aboutString = (String) properties.get("aboutBoxText");

        /* Instantiate components. */
        /* For the components that contain text, provide a localized text string. */
        /* Each localized text string will be extracted from the .properties file. */

        /* Initialize the JPanels first. */
        centerPanel = new JPanel();
        pnlNorth_North = new JPanel();
        tools = new JToolBar(JToolBar.HORIZONTAL);

        /* Next handle the text components. */
        txtPlain = new JTextArea(15, 50);
        txtPassword = new JPasswordField(20);

        /* Then the JButtons and JLabels. */
        btnEncrypt = new JButton((String) properties.get("encryptButtonLabel"));
        btnDecrypt = new JButton((String) properties.get("decryptButtonLabel"));
        btnLock = new JButton((String) properties.get("lockButtonLabel"));
        btnUnlock = new JButton((String) properties.get("unlockButtonLabel"));
        Charset set = Charset.forName("UTF-8");
        ByteBuffer buffer = set.encode((String) properties.get("passwordLabelText"));
        /* Fix any encoding issues in the password text label. */
        buffer.array();
        CharBuffer text = set.decode(buffer);
        lblPassword = new JLabel(text.toString());
        btnAbout = new JButton((String) properties.get("aboutButtonLabel"));
        btnHide = new JButton((String) properties.get("hideButtonLabel"));
        btnSave = new JButton((String) properties.get("saveButtonLabel"));
        btnOpen = new JButton((String) properties.get("openButtonLabel"));
        spPlain = new JScrollPane(txtPlain)
        {
            public Insets getInsets()
            {
                return new Insets(0, 0, 0, 0);
            }
        };

        mb = new JMenuBar();

        /* Initialize the JMenus. */
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        privacyMenu = new JMenu("Privacy");
        operationMenu = new JMenu("Operations");
        helpMenu = new JMenu("Help");

        /* Now initialize the JMenuItems. */
        openFileItem = new JMenuItem((String) properties.get("openButtonLabel"));
        saveFileItem = new JMenuItem((String) properties.get("saveButtonLabel"));
        editPreferencesItem = new JMenuItem("Preferences...");
        fingerprintItem = new JMenuItem("Compute Fingerprint");
        encryptItem = new JMenuItem((String) properties.get("encryptButtonLabel"));
        decryptItem = new JMenuItem((String) properties.get("decryptButtonLabel"));
        lockItem = new JMenuItem((String) properties.get("lockButtonLabel"));
        unlockItem = new JMenuItem((String) properties.get("unlockButtonLabel"));
        hideItem = new JMenuItem("Hide");
        aboutMenuItem = new JMenuItem("About");


        /* Find the current version number. */
        VERSION = (String) properties.get("versionNumber");
        COPYRIGHT_TEXT = (String) properties.get("copyright_text");


        /* Put together the user interface. */
        configUI();

        /* Try to instantiate a CipherRunnable with the AES algorithm. */
        try
        {
            cipherRunnable = new CipherRunnable("AES");
        }
        catch (NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
        }
        catch (NoSuchPaddingException ex)
        {
            ex.printStackTrace();
        }


        btnSave.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                /* Make sure the JFileChooser is initialized. */
                if (fileChooser == null)
                    fileChooser = new JFileChooser();

                /* Open the JFileChooser, and wait for confirmation. */
                int confirmed = fileChooser.showSaveDialog(Panther.this);

                /* If the dialog was canceled, exit the method. */
                if (confirmed != JFileChooser.APPROVE_OPTION)
                    return;

                /* Get the chosen file. */
                File file = fileChooser.getSelectedFile();

                /* Save the data to file. */
                saveBytes(txtPlain.getText().getBytes(), file);
            }
        });
        btnHide.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                /* Hide the program window from immediate view. */
                Panther.this.toggleHidden();
            }
        });

        btnLock.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent evt)
            {
                if (verbose)
                {
                    output("Disabling and reconfiguring UI components.");
                }
                setDisplayUnlocked(false);

                btnUnlock.setEnabled(true);
                tmpPlaintext = txtPlain.getText();
                txtPlain.setText("");
                txtPassword.setText("");
            }
        });

        btnUnlock.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent evt)
            {
                if (verbose)
                {
                    output("Enabling and reconfiguring UI components.");
                }
                setDisplayUnlocked(true);

                txtPlain.setText(tmpPlaintext);
                btnUnlock.setEnabled(false);
                pack();
            }
        });

        btnAbout.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent evt)
            {

            }
        });

        btnEncrypt.addActionListener(new ActionListener()  //Handle clicks to btnEncrypt.
        {
            public void actionPerformed(ActionEvent ae)
            {
                /* Create the encryption thread. */
                byte[] data = txtPlain.getText().getBytes();
                int cipherMode = Cipher.ENCRYPT_MODE;
                char[] password = txtPassword.getPassword();

                try
                {
                    cipherRunnable.init(data, cipherMode, password, Panther.this);
                }
                catch (InvalidKeyException ex)
                {
                    ex.printStackTrace();
                    Panther.this.showError("Invalid", "The provided password has generated an invalid encryption key.");
                }
                catch (NoSuchAlgorithmException ex)
                {
                    ex.printStackTrace();
                    showError("Algorithm not found!", "The encryption algorithm in use was not detected.");
                }
                catch (InvalidKeySpecException ex)
                {
                    ex.printStackTrace();
                    showError("Invalid", "The generated encryption key has been reported as invalid.");
                }

                /* Create a thread from the cipherRunnable. */
                Thread encryptionThread = new Thread(cipherRunnable);

                /* Main the thread. */
                encryptionThread.start();
            }
        });

        btnDecrypt.addActionListener(new ActionListener() //Handle clicks on btnDecrypt.
        {
            public void actionPerformed(ActionEvent e)
            {
                /* Read the file to be decrypted. */
                File file;

                /* Make sure the JFileChooser is initialized. */
                if (Panther.this.fileChooser == null)
                    fileChooser = new JFileChooser();

                /* Show the file chooser and wait for confirmation. */
                int confirmed = fileChooser.showOpenDialog(Panther.this);

                /* If the user canceled the dialog, exit the method. */
                if (confirmed != JFileChooser.APPROVE_OPTION)
                    return;

                /* Get the selected file. */
                file = fileChooser.getSelectedFile();

                /* Read the contents of the file into a byte array. */
                FileInputStream fis = null;
                byte[] data = new byte[(int) file.length()];
                try
                {
                    fis = new FileInputStream(file);
                    //noinspection ResultOfMethodCallIgnored
                    fis.read(data);
                }
                catch (FileNotFoundException ex)
                {
                    ex.printStackTrace();
                    showError("File Not Found", "The specified file was not found.");
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    showError("IO Error", "Unable to read the file.  Make sure that you have read permission.");
                }
                finally
                {
                    /* Make sure the stream is closed. */
                    try
                    {
                        if (fis != null)
                        {
                            fis.close();
                        }
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }

                /* Create the decryption thread. */
                /* The CipherRunnable must be initialized correctly before it can be run as a thread. */
                int cipherMode = Cipher.DECRYPT_MODE;
                char[] password = txtPassword.getPassword();

                try
                {
                    cipherRunnable.init(data, cipherMode, password, Panther.this);
                }
                catch (InvalidKeyException ex)
                {
                    showError("Invalid Key", "The provided password has produced an invalid decryption key.");
                }
                catch (NoSuchAlgorithmException ex)
                {
                    showError("Unknown Algorithm", "The current decryption algorithm is not available.");
                }
                catch (InvalidKeySpecException ex)
                {
                    showError("Invalid Key", "The generated encryption key is invalid.");
                }

                /* Create a thread from the cipherRunnable. */
                Thread decryptionThread = new Thread(cipherRunnable);

                /* Main the thread. */
                decryptionThread.start();
            }
        });

        editPreferencesItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                showPreferences();
            }
        });
    }

    public void showPreferences()
    {
        PreferencesDialog preferencesDialog = new PreferencesDialog(this);
        preferencesDialog.setVisible(true);
        if (preferencesDialog.isApproved())
        {
            this.setDigestAlgorithm(preferencesDialog.getChosenDigestAlgorithm());
        }
    }

    public static Properties loadLanguageResources(String language)
    {
        Properties properties = new Properties();

        /* Attempt to load required language resources. */
        try
        {
            /* Detect unsupported languages. */
            /* Supported languages are English, Spanish, and Italian, */
            /* If it is not one of these, display an error message. */
            if ((!language.equals("en")) && (!language.equals("sp")) && (!language.equals("it")))
            {
                JOptionPane.showMessageDialog(null, "Language code \"" + language + "\" is not supported.", "Unsupported Language", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            /*
                          * Load the labels for the correct locale.
                          * The multilingual language packets are located inside the .jar file in
                          * /org/tamalin/panther/resources/.  Each file which contains a series of
                          * keys and values is identified as so: labels_[language code].properties:
                          * For example, the English package is 'labels_en.properties'.
                          */
            URL url = Panther.class.getResource("/org/tamalin/panther/resources/labels_" + language + ".properties");
            URLConnection uc = url.openConnection();
            properties.load(uc.getInputStream());
        }
        catch (FileNotFoundException ex) //File was not found
        {
            JOptionPane.showMessageDialog(null, "Language packet not found.");
        }
        catch (IOException ioe) //Problem reading from the file
        {
            JOptionPane.showMessageDialog(null, "Error reading language packet.");
        }

        return properties;
    }

    /**
     * Shortcut method for <code>System.out.println()</code>.  This method will soon be changed to use the Logger class.
     *
     * @param s = argument for println() method.
     */
    private void output(String s)
    {
        System.out.println(s);
    }

    /**
     * This method locks the display or unlocks the display.  If the argument is false, the display
     * is locked, if the argument is true, the display is unlocked.
     *
     * @param unlocked This boolean argument specifies whether to unlock or lock the display.
     */
    public void setDisplayUnlocked(boolean unlocked)
    {
        btnEncrypt.setEnabled(unlocked);
        btnDecrypt.setEnabled(unlocked);
        txtPlain.setEditable(unlocked);
        btnLock.setEnabled(unlocked);
        txtPassword.setEditable(unlocked);
        btnSave.setEnabled(unlocked);
        btnOpen.setEnabled(unlocked);
    }

    /**
     * This method hides the display by resizing it to {0, 0}
     * if it is not already hidden.
     * If the display is already hidden, the hide button
     * sets the frame size to fit all the components, using pack().
     */
    public void toggleHidden()
    {
        if (!hidden)
        {
            if (verbose)
            {
                output("Hiding");
                output("Setting size to {0, 0}");
            }
            setSize(0, 0);
            hidden = true;
        }
        else
        {
            if (verbose)
            {
                output("Revealing Window...");
            }
            pack();
            hidden = false;
        }
    }

    /**
     * Configures and adds the UI components to the frame.
     */
    public void configUI()
    {
        //Set the UI layout and the frame title.
        if (verbose)
        {
            output("Laying out UI.");
        }
        setLayout(new BorderLayout());
        setTitle("Panther");

        //Adds all the containers to the Panther Frame.
        if (verbose)
        {
            output("Adding UI component main panels.");
        }
        tools.setFloatable(false);
        this.add(tools, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);


        //Lay out the UI Components the components in the container.
        if (verbose)
        {
            output("Laying out UI panels.");
        }
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(pnlNorth_North, BorderLayout.NORTH);
        centerPanel.add(spPlain, BorderLayout.SOUTH);

        /* ---------- Mac OS X Only ----------- */
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            String style = "segmentedCapsule";
            btnEncrypt.putClientProperty("JButton.buttonType", style);
            btnEncrypt.putClientProperty("JButton.segmentPosition", "first");
            btnDecrypt.putClientProperty("JButton.buttonType", style);
            btnDecrypt.putClientProperty("JButton.segmentPosition", "last");
            btnLock.putClientProperty("JButton.buttonType", style);
            btnLock.putClientProperty("JButton.segmentPosition", "first");
            btnUnlock.putClientProperty("JButton.buttonType", style);
            btnUnlock.putClientProperty("JButton.segmentPosition", "last");
            btnHide.putClientProperty("JButton.buttonType", style);
            btnHide.putClientProperty("JButton.segmentPosition", "only");
            btnSave.putClientProperty("JButton.buttonType", style);
            btnSave.putClientProperty("JButton.segmentPosition", "first");
            btnOpen.putClientProperty("JButton.buttonType", style);
            btnOpen.putClientProperty("JButton.segmentPosition", "last");
        }


        //Add all the UI components to the frame.
        //Several of the components are contained inside other
        //panels that will be added to the frame.
        if (verbose)
        {
            output("Adding UI components");
        }
        tools.add(btnEncrypt);
        tools.add(btnDecrypt);
        tools.add(btnLock);
        tools.add(btnUnlock);
        tools.add(btnHide);
        tools.add(btnSave);
        tools.add(btnOpen);
        pnlNorth_North.add(lblPassword);
        pnlNorth_North.add(txtPassword);
        spPlain.setBorder(null);


        Component[] components = tools.getComponents();

        for (Component c : components)
            c.setFocusable(false);

        mb.add(fileMenu);
        mb.add(editMenu);
        mb.add(operationMenu);
        mb.add(helpMenu);

        fileMenu.add(this.openFileItem);
        fileMenu.add(this.saveFileItem);
        editMenu.add(this.editPreferencesItem);
        operationMenu.add(this.encryptItem);
        operationMenu.add(this.decryptItem);
        operationMenu.add(fingerprintItem);
        operationMenu.add(this.privacyMenu);
        privacyMenu.add(this.hideItem);
        privacyMenu.add(lockItem);
        privacyMenu.add(unlockItem);
        helpMenu.add(aboutMenuItem);

        this.setJMenuBar(mb);


        /* Set the button mnemonics.  Each mnemonic character should be the first character in the
         * button's text, unless it has some diacritic marker over it.
         */
        if (verbose)
        {
            output("Configuring UI components...");
        }
        btnUnlock.setEnabled(false);
        btnUnlock.setMnemonic(btnUnlock.getText().charAt(1));
        btnLock.setMnemonic(btnLock.getText().charAt(0));
        btnHide.setMnemonic(btnHide.getText().charAt(0));
        btnEncrypt.setMnemonic(btnEncrypt.getText().charAt(0));
        btnDecrypt.setMnemonic(btnDecrypt.getText().charAt(0));
        btnAbout.setMnemonic(btnAbout.getText().charAt(0));
        btnSave.setMnemonic(btnSave.getText().charAt(0));
        btnOpen.setMnemonic(btnOpen.getText().charAt(0));

    }

    /**
     * Displays the program help in the given locale.
     */

    public static void showHelp()
    {
        //Print out the help.
        System.out.println("Panther " + VERSION);
        try
        {
            String langCode = locale.getLanguage();
            URL url = Panther.class.getResource("/org/tamalin/panther/resources/help_" + langCode + ".hlp");
            URLConnection uc = url.openConnection();
            Scanner s = new Scanner(uc.getInputStream());

            while (s.hasNextLine())
            {
                System.out.println(s.nextLine());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        //Exit the program.
        System.exit(0);
    }

    /**
     * Establishes the Panther version to the version number of given locale.  It is specified in the language given
     * in the locale.
     *
     * @param locale The locale for that should be used to determine the language to print the help in.
     * @throws IOException thrown if an IOException occurs while reading the properties file.
     */
    public static void establishVersion(Locale locale) throws IOException
    {
        /* Create a new Properties object. */
        Properties props = new Properties();

        URLConnection connection = null;

        try
        {
            String langCode = locale.getLanguage();
            URL url = Panther.class.getResource("/org/tamalin/panther/resources/labels_" + langCode + ".properties");
            connection = url.openConnection();
            props.load(connection.getInputStream());
            VERSION = (String) props.get("versionNumber");
        }
        finally
        {
            if (connection != null)
            {
                connection.getInputStream().close();
            }
        }
    }

    public void saveBytes(byte[] data, File file)
    {
        try
        {
            FileSaveRunnable saveRunnable = new FileSaveRunnable(data, file);
            Thread saveThread = new Thread(saveRunnable);
            saveThread.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void updateFromBytes(byte[] data)
    {
        if (cipherRunnable.getMode() == Cipher.DECRYPT_MODE)
        {
            txtPlain.setText(new String(data));
        }
        else
        {
            /* Make sure the JFileChooser is initialized. */
            if (fileChooser == null)
                fileChooser = new JFileChooser();

            /* Open the JFileChooser, and wait for confirmation. */
            int confirmed = fileChooser.showSaveDialog(this);

            /* If the file dialog was canceled, exit the method. */
            if (confirmed != JFileChooser.APPROVE_OPTION)
                return;

            /* Get the chosen file. */
            File file = fileChooser.getSelectedFile();

            /* Save the bytes to file. */
            saveBytes(data, file);
        }
    }

    public void showAbout()
    {
        StringBuilder message = new StringBuilder(aboutString);
        message.append(VERSION + System.getProperty("line.separator"));
        message.append(COPYRIGHT_TEXT);
        JOptionPane.showMessageDialog(Panther.this, message.toString(), btnAbout.getText(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void setDigestAlgorithm(String alg)
    {
        digestAlgorithm = alg;
    }

    public void showError(String title, String message)
    {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private JPanel centerPanel;
    private JPanel pnlNorth_North;
    private JToolBar tools;
    private static JTextArea txtPlain;
    private JButton btnEncrypt, btnDecrypt, btnLock, btnUnlock, btnHide, btnSave, btnOpen;
    private static JPasswordField txtPassword;
    private JLabel lblPassword;
    private JButton btnAbout;
    private String tmpPlaintext = "";
    private boolean hidden = false;
    private JFileChooser fileChooser;
    private JScrollPane spPlain;
    private String aboutString = "";
    private JMenuBar mb;
    private JMenu editMenu;
    private JMenu operationMenu;
    private JMenu privacyMenu;
    private JMenu fileMenu, helpMenu;
    private JMenuItem openFileItem, saveFileItem, editPreferencesItem;
    private JMenuItem encryptItem, decryptItem, fingerprintItem, lockItem, unlockItem, hideItem;
    private JMenuItem aboutMenuItem;
    private static Locale locale = null;
    private CipherRunnable cipherRunnable;
    private String digestAlgorithm;

    /**
     * The Panther version number.
     * The Panther version number follows this pattern:
     * [release number] [release month] [release year]
     */
    public static String VERSION;
    public static String COPYRIGHT_TEXT;
    public static boolean verbose = false;
}
