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

import com.sun.awt.AWTUtilities;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.*;

/**
 * This class is the main class of the Panther program.
 * It contains the main method, and most of the GUI code.
 *
 * @author Quytelda K. Gaiwin
 * @version 4.0
 * @since 1.0
 */
public class Panther extends JFrame implements Updatable
{
    /**
     * Creates the main window of the application, and loads all the related properties.
     *
     * @param locale The locale for which the GUI should be configured.
     */
    public Panther()
    {
        logger.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
        /*#############*
         * Frame Icon  *
         *#############*/
        Toolkit tk = Toolkit.getDefaultToolkit();
        URL logoURL = Panther.class.getResource("/org/tamalin/panther/resources/icon_128.png");
        Image img = tk.getImage(logoURL);
        this.setIconImage(img);

        /* ------------------ Mac OS X Only -------------------
         * If this is a Mac OS X system, use reflection to load the MacAppHandler class, which
         * handles all the Apple events that may be thrown at the program from the Mac OS X
         * platform.
         */
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            this.OSXConfig();
        }

        /*######################################
         *#  Instantiate Interface Components  #
         *######################################*/
        initComponents();

        /*######################################
         *#  Lay out the interface components  #
         *######################################*/
        this.doLayoutUI();

        /*########################
         * Load Saved Properties *
         *#######################*/
        Properties properties = new Properties();
        File f = this.getPreferencesFile();

        if(!f.exists()) // No saved preferences, use defaults.
        {
            initialize(f);
            digestAlgorithm = "SHA-1";
            this.pack();
            hideOpacity = 0.05f;
        }
        else
        {
            try
            {
                properties.load(new FileInputStream(f));
            }
            catch(IOException ex)
            {
                logger.log(Level.WARNING, "Properties file was unreadable.", ex);
            }

            digestAlgorithm = properties.getProperty("digest_algorithm");
            int width = Integer.parseInt(properties.getProperty("width"));
            int height = Integer.parseInt(properties.getProperty("height"));
            int x = Integer.parseInt(properties.getProperty("x"));
            int y = Integer.parseInt(properties.getProperty("y"));
            this.setSize(width, height);
            this.setLocation(x, y);
            hideOpacity = Float.parseFloat(properties.getProperty("hidden_opacity"));
        }


        /* Try to instantiate a CipherRunnable with the AES algorithm. */
        try
        {
            cipherRunnable = new CipherRunnable("AES");
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm Not Found", ex);
        }
        catch (NoSuchPaddingException ex)
        {
            logger.log(Level.SEVERE, "Padding Error", ex);
        }


        /*####################
         *#  Event Handlers  #
         *####################*/
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                /* Make sure the JFileChooser is initialized. */
                if (fileChooser == null)
                {
                    fileChooser = new JFileChooser();
                }

                /* Open the JFileChooser, and wait for confirmation. */
                int confirmed = fileChooser.showSaveDialog(Panther.this);

                /* If the dialog was canceled, exit the method. */
                if (confirmed != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }

                /* Get the chosen file. */
                File file = fileChooser.getSelectedFile();

                /* Save the data to file. */
                saveBytes(plaintext.getText().getBytes(), file);
            }
        });

        hide.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                /* Hide the program window from immediate view. */
                Panther.this.toggleHidden();
            }
        });

        lock.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                setLocked(true);
            }
        });

        unlock.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                setLocked(false);
            }
        });

        encrypt.addActionListener(new ActionListener()  //Handle clicks to encrypt.
        {
            public void actionPerformed(ActionEvent e)
            {
                encrypt();
            }
        });

        decrypt.addActionListener(new ActionListener() //Handle clicks on decrypt.
        {
            public void actionPerformed(ActionEvent e)
            {
                decrypt();
            }
        });

        editPreferencesItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showPreferences();
            }
        });

        lockItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                Panther.this.setLocked(true);
            }
        });

        unlockItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                Panther.this.setLocked(false);
            }
        });

        fingerprintItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                byte[] fingerprint;
                try
                {
                    fingerprint = computeFingerprint(plaintext.getText().getBytes());

                    /* Construct a dialog to display the digest. */
                    JPanel controlPanel = new JPanel();
                    controlPanel.add(new JLabel(getDigestAlgorithm() + " Fingerprint:"));
                    JTextField copyField = new JTextField();
                    copyField.setEditable(false);

                    /* Convert the fingerprint to Hex code. */
                    StringBuilder hexBuilder = new StringBuilder();
                    for (int i = 0; i < fingerprint.length; i += 2)
                    {
                        String hexComponents = Integer.toHexString(fingerprint[i] & 0xFF).toUpperCase();

                        if (hexComponents.length() == 1)
                        {
                            hexComponents = "0".concat(hexComponents);
                        }

                        hexBuilder.append(hexComponents);
                        if (i != fingerprint.length - 2)
                        {
                            hexBuilder.append(":");
                        }
                    }

                    /* Display the dialog. */
                    copyField.setColumns(hexBuilder.toString().toCharArray().length + 2);
                    copyField.setText(hexBuilder.toString());
                    controlPanel.add(copyField);
                    JOptionPane.showMessageDialog(Panther.this, controlPanel,
                            "Fingerprint", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (NoSuchAlgorithmException ex)
                {
                    Panther.this.showError("Unknown Algorithm", "The algorithm " + Panther.this.getDigestAlgorithm() + " was not found");
                }
            }
        });

        this.aboutMenuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                Panther.this.showAbout();
            }
        });

        this.encryptItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                encrypt();
            }
        });

        this.decryptItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                decrypt();
            }
        });
    }

    /**
     * This method encrypts the text in the main text area, and saves the result to file.
     */
    public void encrypt()
    {
        /* Create the encryption thread. */
        byte[] data = plaintext.getText().getBytes();
        int cipherMode = Cipher.ENCRYPT_MODE;
        char[] pswd = password.getPassword();

        try
        {
            cipherRunnable.init(data, cipherMode, pswd, Panther.this);
        }
        catch (InvalidKeyException ex)
        {
            logger.log(Level.SEVERE, "Invalid Key Generated", ex);
            Panther.this.showError("Invalid", "The provided password has generated an invalid encryption key.");
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Invalid Algorithm", ex);
            showError("Algorithm not found!", "The encryption algorithm in use was not detected.");
        }
        catch (InvalidKeySpecException ex)
        {
            logger.log(Level.SEVERE, "Invalid Key Specification", ex);
            showError("Invalid", "The generated encryption key has been reported as invalid.");
        }

        /* Create a thread from the cipherRunnable. */
        Thread encryptionThread = new Thread(cipherRunnable);

        /* Main the thread. */
        encryptionThread.start();
    }

    private void initComponents()
    {
        logger.log(Level.FINE, "Initializing interface components.");
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());
        centerPanel = new JPanel();
        north = new JPanel();
        tools = new JToolBar(JToolBar.HORIZONTAL);

        /* Handle the text components. */
        plaintext = new JTextArea(20, 55);
        plaintext.setLineWrap(true);
        password = new JPasswordField(20);

        /* Then the JButtons and JLabels. */
        encrypt = new JButton(bundle.getString("encrypt"));
        decrypt = new JButton(bundle.getString("decrypt"));
        lock = new JButton(bundle.getString("lock"));
        unlock = new JButton(bundle.getString("unlock"));
        passwordLabel = new JLabel(bundle.getString("password"));
        about = new JButton(bundle.getString("about"));
        hide = new JButton(bundle.getString("hide"));
        save = new JButton(bundle.getString("save"));
        open = new JButton(bundle.getString("open"));

        /* Construct the JScrollPane in the main window with no insets. */
        plaintextPane = new JScrollPane(plaintext)
        {

            @Override
            public Insets getInsets()
            {
                return new Insets(0, 0, 0, 0);
            }
        };

        mb = new JMenuBar();

        /* Initialize the JMenus. */
        fileMenu = new JMenu(bundle.getString("menu.file"));
        editMenu = new JMenu(bundle.getString("menu.edit"));
        privacyMenu = new JMenu(bundle.getString("menu.privacy"));
        operationMenu = new JMenu(bundle.getString("menu.operations"));
        helpMenu = new JMenu(bundle.getString("menu.help"));

        /* Now initialize the JMenuItems. */
        openFileItem = new JMenuItem(bundle.getString("menu.file.open"));
        saveFileItem = new JMenuItem(bundle.getString("menu.file.save"));
        editPreferencesItem = new JMenuItem(bundle.getString("menu.edit.preferences"));
        fingerprintItem = new JMenuItem(bundle.getString("menu.privacy.fingerprint"));
        encryptItem = new JMenuItem(bundle.getString("menu.privacy.encrypt"));
        decryptItem = new JMenuItem(bundle.getString("menu.privacy.decrypt"));
        lockItem = new JMenuItem(bundle.getString("menu.operations.lock"));
        unlockItem = new JMenuItem(bundle.getString("menu.operations.unlock"));
        hideItem = new JMenuItem(bundle.getString("menu.operations.hide"));
        aboutMenuItem = new JMenuItem(bundle.getString("menu.help.about"));
    }

    private void initialize(File file)
    {
        if(!file.exists())
        {
            try
            {
                boolean created = file.createNewFile();
                if(!created) throw new IOException();
            }
            catch(IOException ex)
            {
                logger.log(Level.WARNING, "Unable to write preferences file.", ex);
            }
        }
    }

    /**
     * This method decrypts the contents of a file, and displays the result in the main text area.
     */
    public void decrypt()
    {
        /* Read the file to be decrypted. */
        File file;

        /* Make sure the JFileChooser is initialized. */
        if (Panther.this.fileChooser == null)
        {
            fileChooser = new JFileChooser();
        }

        /* Show the file chooser and wait for confirmation. */
        int confirmed = fileChooser.showOpenDialog(Panther.this);

        /* If the user canceled the dialog, exit the method. */
        if (confirmed != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

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
            logger.log(Level.INFO, "File Not Found", ex);
            showError("File Not Found", "The specified file was not found.");
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "Input/Output Error", ex);
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
                logger.log(Level.WARNING, "Unable to close file input stream.", ex);
            }
        }

        /* Create the decryption thread. */
        /* The CipherRunnable must be initialized correctly before it can be run as a thread. */
        int cipherMode = Cipher.DECRYPT_MODE;
        char[] pswd = password.getPassword();

        try
        {
            cipherRunnable.init(data, cipherMode, pswd, Panther.this);
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

    /**
     * This method shows the preferences dialog.
     */
    public void showPreferences()
    {
        PreferencesDialog preferencesDialog = new PreferencesDialog(this);
        preferencesDialog.setDefaultDigestAlg(this.getDigestAlgorithm());
        preferencesDialog.setVisible(true);
        if (preferencesDialog.isApproved())
        {
            this.setDigestAlgorithm(preferencesDialog.getChosenDigestAlgorithm());
            hideOpacity = preferencesDialog.getHideOpacity();
        }
    }

    /**
     * This method locks the display or unlocks the display.  If the argument is false, the display
     * is locked, if the argument is true, the display is unlocked.
     *
     * @param unlocked This boolean argument specifies whether to unlock or lock the display.
     */
    public void setDisplayUnlocked(boolean unlocked)
    {
        encrypt.setEnabled(unlocked);
        decrypt.setEnabled(unlocked);
        plaintext.setEditable(unlocked);
        lock.setEnabled(unlocked);
        password.setEditable(unlocked);
        save.setEnabled(unlocked);
        open.setEnabled(unlocked);
    }

    /**
     * This method hides the main frame by reducing the opacity.
     * If the frame is already hidden, the hide button sets the frame opacity back to 100%.
     */
    public void toggleHidden()
    {
        if (!hidden)
        {
            logger.log(Level.INFO, "Concealing Frame");
            
            if(AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.TRANSLUCENT));
            {
                AWTUtilities.setWindowOpacity(this, hideOpacity);
            }

            hidden = true;
        }
        else
        {
            logger.log(Level.INFO, "Revealing frame.");
            if(AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.TRANSLUCENT));
            {
                AWTUtilities.setWindowOpacity(this, 1f);
            }
            hidden = false;
        }
    }

    /**
     * This method either locks or unlocks the JFrame display.
     *
     * @param lock whether lock or unlock the display.
     */
    public void setLocked(boolean lock)
    {
        setDisplayUnlocked(!lock);
        unlock.setEnabled(lock);
        if (lock)
        {
            tmpPlaintext = plaintext.getText();
            plaintext.setText("");
            password.setText("");
        }
        else
        {
            plaintext.setText(tmpPlaintext);
            tmpPlaintext = null;
        }

    }

    private File getPreferencesFile()
    {
        String home = System.getProperty("user.home");
        String path = null;
        if(System.getProperty("os.name").equals("Mac OS X"))
            path = home + "/Library/Preferences/org.tamalin.panther";
        else
            path = home + "./panthersleek";

        return new File(path);
    }

    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Configures and adds the UI components to the frame.
     */
    private void doLayoutUI()
    {
        //Set the UI layout and the frame title.
        logger.log(Level.FINE, "Layout out the user interface.");
        setLayout(new BorderLayout());
        setTitle("Panther");

        //Adds all the containers to the Panther Frame.
        tools.setFloatable(false);
        this.add(tools, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);

        

        //Lay out the UI Components the components in the container.
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(north, BorderLayout.NORTH);
        centerPanel.add(plaintextPane, BorderLayout.CENTER);

        plaintextPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        /* ---------- Mac OS X Only ----------- */
        /* Lays out the toolbar properly using the OS X segmented capsule button style. */
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            String style = "segmentedTextured";
            encrypt.putClientProperty("JButton.buttonType", style);
            encrypt.putClientProperty("JButton.segmentPosition", "first");
            decrypt.putClientProperty("JButton.buttonType", style);
            decrypt.putClientProperty("JButton.segmentPosition", "last");
            lock.putClientProperty("JButton.buttonType", style);
            lock.putClientProperty("JButton.segmentPosition", "first");
            unlock.putClientProperty("JButton.buttonType", style);
            unlock.putClientProperty("JButton.segmentPosition", "last");
            hide.putClientProperty("JButton.buttonType", style);
            hide.putClientProperty("JButton.segmentPosition", "only");
            save.putClientProperty("JButton.buttonType", style);
            save.putClientProperty("JButton.segmentPosition", "first");
            open.putClientProperty("JButton.buttonType", style);
            open.putClientProperty("JButton.segmentPosition", "last");
        }

        logger.log(Level.FINER, "Adding components to frame.");
        tools.add(encrypt);
        tools.add(decrypt);
        tools.add(lock);
        tools.add(unlock);
        tools.add(hide);
        tools.add(save);
        tools.add(open);
        north.add(passwordLabel);
        north.add(password);
        plaintextPane.setBorder(null);


        Component[] components = tools.getComponents();

        for (Component c : components)
        {
            c.setFocusable(false);
        }

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
        logger.log(Level.FINER, "Configuring interface element behavior.");
        unlock.setEnabled(false);
        unlock.setMnemonic(unlock.getText().charAt(1));
        lock.setMnemonic(lock.getText().charAt(0));
        hide.setMnemonic(hide.getText().charAt(0));
        encrypt.setMnemonic(encrypt.getText().charAt(0));
        decrypt.setMnemonic(decrypt.getText().charAt(0));
        about.setMnemonic(about.getText().charAt(0));
        save.setMnemonic(save.getText().charAt(0));
        open.setMnemonic(open.getText().charAt(0));
    }

    private void OSXConfig()
    {
        try
        {
            Class<?> appClass = Class.forName("org.tamalin.panther.MacAppHandler");
            Object app = appClass.newInstance();
            Method initializer = appClass.getMethod("init", Panther.class, Image.class);
            initializer.invoke(app, this, this.getIconImage());
        }
        catch (ClassNotFoundException ex)
        {
            logger.log(Level.WARNING, "Can't find Mac OS X configuration class.", ex);
        }
        catch (NoSuchMethodException ex)
        {
            logger.log(Level.WARNING, "Can't find Mac OS X configuration class.", ex);
        }
        catch (InvocationTargetException ex)
        {
            logger.log(Level.WARNING, "Can't access Mac OS X configuration class.", ex);
        }
        catch (InstantiationException ex)
        {
            logger.log(Level.WARNING, "Can't access Mac OS X configuration class.", ex);
        }
        catch (IllegalAccessException ex)
        {
            logger.log(Level.WARNING, "Can't access Mac OS X configuration class.", ex);
        }
    }

    /**
     * Saves the bytes to the given file.
     *
     * @param data the bytes to write to file
     * @param file the file to write to
     */
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
            logger.log(Level.WARNING, "Unable to run the save thread.", ex);
        }
    }

    public void updateFromBytes(byte[] data)
    {
        if (cipherRunnable.getMode() == Cipher.DECRYPT_MODE)
        {
            plaintext.setText(new String(data));
        }
        else
        {
            /* Make sure the JFileChooser is initialized. */
            if (fileChooser == null)
            {
                fileChooser = new JFileChooser();
            }

            /* Open the JFileChooser, and wait for confirmation. */
            int confirmed = fileChooser.showSaveDialog(this);

            /* If the file dialog was canceled, exit the method. */
            if (confirmed != JFileChooser.APPROVE_OPTION)
            {
                return;
            }

            /* Get the chosen file. */
            File file = fileChooser.getSelectedFile();

            /* Save the bytes to file. */
            saveBytes(data, file);
        }
    }

    /**
     * Computes the message digest, checksum, or "fingerprint" of a byte array.
     *
     * @param data the data to be digested
     * @return the message digest as a byte array
     * @throws NoSuchAlgorithmException throws a no such algorithm exception when Panther.getDigestAlgorithm() returns
     *                                  an algorithm which is not supported or known under the current provider.
     */
    public byte[] computeFingerprint(byte[] data) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance(this.getDigestAlgorithm());
        return md.digest(data);
    }

    public void showAbout()
    {
        String info = "<html><center><b>Panther " + VERSION + "</b><br /><small>© 2010-2011 Tamalin<br />Under the Apache 2.0 License</small></center></html>";
        JOptionPane.showMessageDialog(Panther.this, info, about.getText(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void setDigestAlgorithm(String alg)
    {
        digestAlgorithm = alg;
    }

    public String getDigestAlgorithm()
    {
        return digestAlgorithm;
    }

    public static float getHideOpacity()
    {
        return hideOpacity;
    }

    public void showError(String title, String message)
    {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void cleanUp() throws IOException
    {
        /*##################
         * Save Properties *
         *#################*/
        Properties properties = new Properties();
        File file = this.getPreferencesFile();
        properties.setProperty("height", "" + this.getHeight());
        properties.setProperty("width", "" + this.getWidth());
        properties.setProperty("x", "" + this.getX());
        properties.setProperty("y", "" + this.getY());
        properties.setProperty("digest_algorithm", this.getDigestAlgorithm());
        properties.setProperty("hidden_opacity", "" + hideOpacity);
        properties.store(new FileOutputStream(file), "Do not modify this file by hand!");
    }
    
    private JPanel centerPanel;
    private JPanel north;
    private JToolBar tools;
    private static JTextArea plaintext;
    private JButton encrypt, decrypt, lock, unlock, hide, save, open;
    private static JPasswordField password;
    private JLabel passwordLabel;
    private JButton about;
    private String tmpPlaintext = "";
    private boolean hidden = false;
    private JFileChooser fileChooser;
    private JScrollPane plaintextPane;
    private JMenuBar mb;
    private JMenu editMenu;
    private JMenu operationMenu;
    private JMenu privacyMenu;
    private JMenu fileMenu, helpMenu;
    private JMenuItem openFileItem, saveFileItem, editPreferencesItem;
    private JMenuItem encryptItem, decryptItem, fingerprintItem, lockItem, unlockItem, hideItem;
    private JMenuItem aboutMenuItem;
    private CipherRunnable cipherRunnable;
    private String digestAlgorithm;
    private static float hideOpacity;
    /**
     * The Panther version description.
     * The Panther version description follows this pattern:
     * [release number] [release month] [release year]
     * =====================================================
     * The individual release number consists of three
     * parts separated by periods:
     * 1) the major version number - For major changes that introduce
     * compatibility issues with older versions or contain visible changes
     * that are large enough to warrant a major version number change.
     * 2) the minor version number - For moderate to (visibly) large changes
     * that do not affect compatibility with older versions.
     * 3) the micro version number - For bug fixes or trivial changes
     * 4) the micro release number may optionally be followed by or replaced by
     * (in the case that it is 0) the release type 'a' for alpha, 'b' for beta.
     */
    public static final String VERSION = "4.0";
    public static final String BUNDLE_PATH = "org.tamalin.panther.resources.labels";
    /**
     * The text that represents the copyright of the program.
     */
    private static final Logger logger = Logger.getLogger("org.tamalin.panther");
}
