/*
 * Copyright 2011 Quytelda Gaiwin
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
 * Panther.java
 * Created on Nov 30, 2008, 11:49:18 AM
 *
 */
package org.tamalin.panthersleek;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
import org.tamalin.panthersleek.resources.plaf.EventHandler;
import org.tamalin.panthersleek.resources.plaf.osx.OSXApp;

import javax.crypto.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.logging.*;

/**
 * The main window of Panther Sleek. Displays the main window, loads the properties,
 * stores the keys, and holds the password.
 *
 * @author Quytelda K. Gaiwin
 * @version 2.1.1
 * @since 1.0.0
 */
public class Panther extends JFrame
{

    /**
     * This is the start method, which gets everything up and running in the main window of the program.
     *
     * @param args The command line arguments
     */
    public Panther(String[] args)
    {
        if (logger.getLevel() == null)
        {
            logger.setLevel(Level.INFO);
        }

        /* Parse arguments to determine if a language interface change is needed. */
        if (args.length >= 2)
        {
            if (args[0].equals("-l") || args[0].equals("--language"))
            {
                if (args[1].equals("zh"))
                {
                    Locale.setDefault(Locale.CHINESE);
                }
                else if(args[1].equals("en"))
                {
                    Locale.setDefault(Locale.ENGLISH);
                }
                else
                {
                    logger.log(Level.WARNING, "Unsupported language packet requested.");
                }
            }
        }

        /* Configure the display and appearance of the frame. */
        this.setTitle("Panther Sleek");
        URL imageURL = Panther.class.getResource("/panthersleek/resources/dock-logo.png");
        Image img = Toolkit.getDefaultToolkit().getImage(imageURL);
        setIconImage(img);

        /*###########################
         * Preferences & Properties *
         *###########################*/
        Properties props = new Properties();
        String propFilePath = getFileStorageDir() + separator() + "properties.properties";
        File propertyFile = new File(propFilePath);

        /*###############*
         * Load Keystore *
         *###############*/
        String keyStoreFilePath = getFileStorageDir() + separator() + "keys";
        File keyStoreFile = new File(keyStoreFilePath);

        /*#################
         * First Run Code *
         *#################*/
        if (!propertyFile.exists() || !keyStoreFile.exists())
        {
            try
            {
                /* Get the necessary setup information, eg.
                 * Master Password
                 */
                String message = ResourceBundle.getBundle(BUNDLE_PATH).getString("panther.prompt.newpwd");
                PasswordPrompt pp = new PasswordPrompt(null, true, message);
                pp.setVisible(true);
                char[] pswd = pp.getPassword();
                initialize(pswd, propertyFile);

                /* Emulate the ending of the program before any of the settings load.
                 * This way, they load as if they had always been there. */
                saveKeyStore();
            }
            catch (IOException ex)
            {
                logger.log(Level.SEVERE, "I/O Error while creating the keystore.", ex);
                String message = "The Keystore was unable to be created because of an error writing to the location.";
                Panther.showErrorDialog("I/O Error", algorithm, message);
            }
        }

        /* Try to load all the properties into memory. */
        FileInputStream in = null;
        try
        {
            //load the properties through in.
            in = new FileInputStream(propertyFile);
            props.load(in);
        }
        catch (FileNotFoundException ex)
        {
            logger.log(Level.WARNING, "Preferences file was not found!", ex);
            JOptionPane.showMessageDialog(null, "The preferences file was not found!", "Missing Preference File", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "I/O Error while reading the preferences file.", ex);
            JOptionPane.showMessageDialog(null, "An error occurred while reading the preferences file.", "Error Reading Preferences File", JOptionPane.ERROR_MESSAGE);
        }
        finally /* Try to close the input stream to the properties file. */

        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                logger.log(Level.WARNING, "I/O Error while closing input stream to preferences file.", ex);
            }
        }

        /*##################
         * Interface Setup *
         *##################*/

        // Set locale and language
        Locale.setDefault(new Locale(props.getProperty("lang")));

        /* Determine the algorithms. */
        algorithm = props.getProperty("encryption_algorithm");
        digestAlgorithm = props.getProperty("digest_algorithm");

        if (algorithm == null)
        {
            algorithm = "AES";
        }
        if (digestAlgorithm == null)
        {
            digestAlgorithm = "SHA-1";
        }

        initComponents();

        EventHandler preferenceHandler = new EventHandler()
        {

            public boolean eventTriggered()
            {
                Panther.this.showPreferences();
                return true;
            }
        };

        EventHandler aboutHandler = new EventHandler()
        {

            public boolean eventTriggered()
            {
                Panther.this.showAbout();
                return true;
            }
        };

        EventHandler quitHandler = new EventHandler()
        {

            public boolean eventTriggered()
            {
                return cleanUp();
            }
        };
        /* ================= Mac OS X Only =================
         * Create the apple Application object, and configure it to
         * display the necessary information, including the preferences menu
         * and the dock icon, as well as attaching an ApplicationListener to handle Apple events.
         */
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            OSXApp app = new OSXApp(this.getJMenuBar(), img);
            app.setPreferencesHandler(preferenceHandler);
            app.setAboutHandler(aboutHandler);
            app.setQuitHandler(quitHandler);
        }

        //create a new PlaintextDeposit
        plaintextDeposit = new PlaintextDeposit();

        //load the stored keys from the disk
        keys = loadStoredKeys();

        try
        {
            //get the default ByteCipher.
            byteCipher = new ByteCipher(getAlgorithm());
        }
        catch (NoSuchAlgorithmException ex)
        {
            Panther.showErrorDialog("AES Encryption Error", "The " + getAlgorithm() + " algorithm is unknown.", "This could be a configuration error.  Check your configuration related to encryption and security settings.");
        }
        catch (NoSuchPaddingException ex)
        {
            String msg = "The padding scheme requested for the " + getAlgorithm() + " was not found.";
            Panther.showErrorDialog("Padding Scheme Not Found", msg, "This may be a configuration error.  Check your system's configuration relating to security and encryption.");
        }

        /* refresh the choices in the key combo-box */
        refreshKeyChoices();

        /* Parse the saved preferences. */
        boolean sd = props.get("showDigest").equals("true");
        boolean useNativeLookAndFeel = props.get("useNativeLookAndFeel").equals("true");
        useNativeLookAndFeelItem.setSelected(useNativeLookAndFeel);
        showDigest = (sd);

        showMessageDigestItem.setSelected(sd);

        /* Try to set the look and feel to whatever in specified in the preferences, if any. */
        try
        {
            if (!useNativeLookAndFeel)
            {
                logger.log(Level.INFO, "Using Nimbus LAF");
                /* Use the cross platform look and feel "Nimbus" */
                LookAndFeel nimbus = new NimbusLookAndFeel();
                UIManager.setLookAndFeel(nimbus);
                SwingUtilities.updateComponentTreeUI(plaintext);
            }
            else
            {
                logger.log(Level.INFO, "Applying Native LAF");
                /* Set the look and feel to the one that looks closest to the system's native interface. */
                String nativeLookAndFeel = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(nativeLookAndFeel);
                SwingUtilities.updateComponentTreeUI(plaintext);
            }
        }
        catch (UnsupportedLookAndFeelException ex)
        {
            logger.log(Level.WARNING, "Look and feel is unsupported.", ex);
            JOptionPane.showMessageDialog(null, "There was an error applying your chosen look and feel.  It is either missing or unsupported.", "Unsupported or Missing Look and Feel", JOptionPane.ERROR_MESSAGE);
        }
        catch (ClassNotFoundException ex)
        {
            logger.log(Level.WARNING, "Look and feel class was not found.", ex);
            JOptionPane.showMessageDialog(null, "There was an error applying your chosen look and feel.  Unable to find the system's look and feel.", "Missing Look and Feel.", JOptionPane.ERROR_MESSAGE);
        }
        catch (InstantiationException ex)
        {
            logger.log(Level.WARNING, "Look and feel class could not be instantiated.", ex);
            JOptionPane.showMessageDialog(null, "There was an error applying your chosen look and feel.  Unable to instantiate the look and feel.", "Unable to Instantiate Look and Feel", JOptionPane.ERROR_MESSAGE);
        }
        catch (IllegalAccessException ex)
        {
            logger.log(Level.WARNING, "Illegal Access Exception while trying to apply look and feel.", ex);
            JOptionPane.showMessageDialog(null, "There was an error applying your chosen look and feel.  Unable to Access this property.", "Error Accessing Look and Feel", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called to set up the environment during the first run of the program.
     * It will create a KeyStore with the given password for future Key storage, and it will provide
     * a configured properties file in the home directory.
     *
     * @param pswd         the KeyStore password
     * @param propertyFile the file for property storage
     * @throws IOException The exception is thrown when an error is encountered opening, accessing, writing to, or creating a file.
     */
    public static void initialize(char[] pswd, File propertyFile) throws IOException
    {
        /* Make sure any old files are cleared out of the storage directory, if it exists. */
        if (propertyFile.getParentFile().exists())
        {
            File[] files = propertyFile.getParentFile().listFiles();

            if (files != null)
            {

                for (File file : files)
                {
                    boolean deleted = file.delete();

                    if (!deleted)
                    {
                        String message = "Unable to delete old configuration files!";
                        JOptionPane.showMessageDialog(null, message, "Error Deleting Files", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        /*--------------------------------------------------------------------+
         * Create a new encrypted KeyStore that will store all the future keys.
         *--------------------------------------------------------------------+
         */
        try
        {
            algorithm = "AES";
            encryptedKeyStore = KeyStore.getInstance("JCEKS");
            password = pswd;

            //load encrypted keys
            encryptedKeyStore.load(null, pswd);
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm error while creating keystore.", ex);
        }
        catch (CertificateException ex)
        {
            logger.log(Level.SEVERE, "Certificate error while creating keystore.", ex);
        }
        catch (KeyStoreException ex)
        {
            logger.log(Level.SEVERE, "Keystore error while creating keystore.", ex);
            showErrorDialog("Key Store Error", ex.getCause().getMessage(), "");
        }


        /* Create the default random key to use for experimental purposes.
         * Wait to add the default key until after the ArrayList has been
         * initialized, possibly with other keys. */
        try
        {
            KeyGenerator factory = KeyGenerator.getInstance(Panther.getAlgorithm());
            SecureRandom rand = new SecureRandom();
            factory.init(rand);
            Key newKey = factory.generateKey();
            PantherKey pkey = new PantherKey(newKey, 0, "default-key");
            Key key = pkey.getKey();

            //try to add the key to the KeyStore
            try
            {
                //Generate a SecretKey from the key
                SecretKey secretKey = (SecretKey) key;

                //Find the key's information
                String alias = pkey.getDisplayName();
                SecretKeyEntry keyEntry = new SecretKeyEntry(secretKey);

                //Store the key in the KeyStore
                encryptedKeyStore.setEntry(alias, keyEntry, new PasswordProtection(pswd));
            }
            catch (KeyStoreException ex)
            {
                logger.log(Level.SEVERE, "Keystore error while creating default key.", ex);
                displayErrorMessage("Error", "Unable to store the default key on file!");
            }
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm error while creating default key.", ex);
            displayErrorMessage("Error", "The algorithm \"" + algorithm + "\" is not not usable.");
        }


        /*+--------------------------------------------+
         *          Create properties File
         *+--------------------------------------------+ */
        if (!propertyFile.exists())
        {

            /* Attempt to create the properties file. */
            try
            {
                /* First, make sure there is a parent directory. */
                if (!propertyFile.getParentFile().exists())
                {
                    boolean created = propertyFile.getParentFile().mkdir();
                    if (!created)
                    {
                        JOptionPane.showMessageDialog(null, "There was an error creating the configuration directory!", "Error", JOptionPane.ERROR_MESSAGE);
                        throw new IOException();
                    }
                }

                //Now create the property file.
                boolean created = propertyFile.createNewFile();
                if (!created)
                {
                    JOptionPane.showMessageDialog(null, "There was an error creating the properties file!", "Error", JOptionPane.ERROR_MESSAGE);
                    throw new IOException();
                }
            }
            catch (IOException ex)
            {
                logger.log(Level.WARNING, "I/O error while creating preferences file.", ex);
            }

            /* Now that the file is created, fill it with the required information. */
            Properties properties = new Properties();

            /* Create an output stream to the properties file. */
            FileOutputStream fos = new FileOutputStream(propertyFile);

            /* Add properties to the properties object. */
            properties.setProperty("showDigest", Boolean.FALSE.toString());
            properties.setProperty("useNativeLookAndFeel", Boolean.TRUE.toString());
            properties.setProperty("lang", Locale.getDefault().getLanguage());
            properties.setProperty("encryption_algorithm", "AES");

            /* Pass the output stream to the properties file. */
            String comment = "This is a properties file, do not modify it unless you know what you are doing!";
            properties.store(fos, comment);
        }
    }

    /**
     * Returns a property object loaded with all of the program information such
     * as algorithms, resource URLs, etc.
     *
     * @return An object for accessing the Panther Properties.
     */
    public static Properties loadPantherInformation()
    {
        Properties pantherProps = new Properties();
        String resourcePath = "/panthersleek/resources/properties/properties.properties";
        InputStream in = Panther.class.getResourceAsStream(resourcePath);
        try
        {
            //Load the properties with $in  (haha PHP :)).
            pantherProps.load(in);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null, "There was a problem loading the Panther Sleek properties.", "I/O Error", JOptionPane.ERROR_MESSAGE);
        }

        return pantherProps;
    }

    /**
     * Decrypts and loads the keys on the hard disk from the KeyStore.
     *
     * @return The decrypted Keys.
     */
    public static ArrayList<PantherKey> loadStoredKeys()
    {
        File keyStore = getKeyStoreFile();
        ArrayList<PantherKey> loadedKeys = null;
        FileInputStream inKeys;

        /* -----------------------------------------------------------------+
         * Read and decrypt the Key objects from the JCEKS KeyStore on file.
         * -----------------------------------------------------------------+
         */
        try
        {
            //get the keystore object
            encryptedKeyStore = KeyStore.getInstance("JCEKS");

            //query for a password
            String prompt = ResourceBundle.getBundle(BUNDLE_PATH).getString("panther.prompt.pwd");
            PasswordPrompt pp = new PasswordPrompt(null, true, prompt);
            pp.setVisible(true);
            char[] storePassword = pp.getPassword();

            //make sure there is a store password
            if (storePassword != null)
            {
                //Set globally stored password.
                password = storePassword;

                /* Read and decrypt the KeyStore through the FileInputStream. */
                inKeys = new FileInputStream(keyStore);

                /* Load the decrypted keys from the KeyStore into memory. */
                encryptedKeyStore.load(inKeys, storePassword);
            }
            else //No password was provided
            {
                System.exit(0);
            }
        }
        catch (KeyStoreException ex)
        {
            logger.log(Level.SEVERE, "Keystore error occured while reading the keystore.", ex);
            JOptionPane.showMessageDialog(null, "An error occurred while trying to access the keystore.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (FileNotFoundException ex)
        {
            logger.log(Level.SEVERE, "Keystore file was not found!", ex);
            JOptionPane.showMessageDialog(null, "An error occurred while reading from the encrypted keystore.  The system says: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, "I/O Error while reading keystore.  Could be a wrong password.", ex);
            JOptionPane.showMessageDialog(null, ResourceBundle.getBundle(BUNDLE_PATH).getString("error.auth.badpwd"), ResourceBundle.getBundle(BUNDLE_PATH).getString("error.auth.title"), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm error while reading the keystore.", ex);
            JOptionPane.showMessageDialog(null, "An error occurred while reading from the encrypted keystore.  The system says: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (java.security.cert.CertificateException ex)
        {
            logger.log(Level.SEVERE, "Certificate error while reading from the keystore.", ex);
            JOptionPane.showMessageDialog(null, "There is a problem with the security certificate.  Keystore access is denied.", "Certificate Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        /*--------------------------------------------------------+
         * Load the decrypted keys into an ArrayList (loadedKeys).
         *--------------------------------------------------------+ */
        Enumeration allKeyAliases = null;
        try
        {
            if (encryptedKeyStore == null)
            {
                System.exit(0);
            }
            allKeyAliases = encryptedKeyStore.aliases();
        }
        catch (KeyStoreException ex)
        {
            logger.log(Level.SEVERE, "Keystore exception while loading keys.", ex);
        }

        try
        {
            loadedKeys = new ArrayList<PantherKey>(1);

            //Transfer keys from allKeys to loadedKeys ArrayList.
            if (allKeyAliases != null)
            {
                for (int i = 1; allKeyAliases.hasMoreElements(); i++)
                {
                    String nextKeyAlias = (String) allKeyAliases.nextElement();
                    loadedKeys.add(new PantherKey(encryptedKeyStore.getKey(nextKeyAlias, password), i, nextKeyAlias));
                }
            }
        }
        catch (KeyStoreException ex)
        {
            displayErrorMessage("Error", "There was a problem the stored encryption keys reading from file.");
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm error while loading keys.", ex);
            displayErrorMessage("Unsupported Algorithm", "The encryption algorithm in use is not supported!");
        }
        catch (UnrecoverableKeyException ex)
        {
            logger.log(Level.WARNING, "An unrecoverable key was encountered while loading keys.", ex);
            displayErrorMessage("Unable to Recover Key", "There was an error reading a key from file.  It may be corrupted.");
        }

        return loadedKeys;
    }

    /**
     * Returns the name of the file in which the keys are stored.
     * It uses the system home directory and the system file separator.
     *
     * @return The file where the keys are stored.
     */
    public static File getKeyStoreFile()
    {
        //Prepare to load the keys stored in files.
        //Determine the user's home directory, and the system file seperator.
        //Then create a file object representing the key file.
        String homeDir = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        String path = homeDir + separator + ".panthersleek" + separator + "keys";

        return new File(path);
    }

    /**
     * Returns the directory in which the saved keys are stored.
     *
     * @return the directory used to store all the preferences and keys.
     */
    public static File getFileStorageDir()
    {
        //Prepare to load the keys stored in files.
        //Determine the user's home directory, and the system file seperator.
        //Then create a file object representing the key file.
        String homeDir = System.getProperty("user.home");
        String seperator = System.getProperty("file.separator");
        String path = homeDir + seperator + ".panthersleek";

        return new File(path);
    }

    /**
     * Returns the file separator for the current operating system.
     *
     * @return The system file separator.
     */
    public static String separator()
    {
        return System.getProperty("file.separator");
    }

    /**
     * Returns the cryptographic algorithm used by Panther to create Cipher objects,
     * keys, and complete other tasks that require an algorithm name.
     *
     * @return The encryption algorithm used by Panther.
     */
    public static String getAlgorithm()
    {
        return algorithm;
    }

    /**
     * Sets the cryptographic algorithm used by Panther Sleek to create Cipher objects, keys, and complete encryption
     * and decryption tasks.
     *
     * @param alg The encryption algorithm to use
     */
    public static void setAlgorithm(String alg) //Make sure this is used
    {
        algorithm = alg;
    }

    /**
     * Returns the name of the digest algorithm used by Panther Sleek.
     *
     * @return The name of the digest algorithm.
     */
    public static String getDigestAlgorithm()
    {
        return digestAlgorithm;
    }

    public static void setDigestAlgorithm(String alg)
    {
        digestAlgorithm = alg;
    }

    public void setLocked(boolean l)
    {
        locked = l;
    }

    public boolean isLocked()
    {
        return locked;
    }

    /**
     * Returns the number of keys currently existing for use in Panther.
     *
     * @return The number of usable keys in the keys ArrayList.
     */
    public static int getNumberOfKeys()
    {
        return keys.size();
    }

    /**
     * Returns an ArrayList of all the Panther keys currently in use by the program.
     *
     * @return An ArrayList of keys that Panther is currently using.
     */
    public static ArrayList<PantherKey> getPantherKeys()
    {
        return keys;
    }

    /**
     * Adds a key to the current collection of keys.
     *
     * @param key The key to add to the collection.  It's display name is used in
     *            the JComboBox.
     */
    public static void addKey(PantherKey key)
    {
        keys.add(key);
        String keyType = key.getKey().getAlgorithm();
        currentEncryptionKey.addItem(key.getDisplayName() + "  (" + keyType + ")");
    }

    /**
     * Refreshes all the key choices in the currentEncryptionKey JComboBox.  This
     * method should be called <b>after</b> adding or removing a key to the running program,
     * so that it can be accessed immediately.
     */
    public static void refreshKeyChoices()
    {
        currentEncryptionKey.removeAllItems();

        for (PantherKey key : keys)
        {
            currentEncryptionKey.addItem(key.getDisplayName() + "  (" + key.getKey().getAlgorithm() + ")");
        }
    }

    public void clearPlaintext()
    {
        plaintext.setText("");
    }

    /**
     * Returns the key that has the same display name as specified.  If no key exists
     * with that display name, it returns null.
     *
     * @param name The display name to search for.
     * @return The key that has the same display name.
     */
    public static PantherKey getKeyByDisplayName(String name)
    {
        for (PantherKey key : keys)
        {
            if (key.getDisplayName().equals(name))
            {
                return key;
            }
        }

        return null;
    }

    public Key getSelectedKey()
    {
        String humanName = (String) currentEncryptionKey.getSelectedItem();
        String name = humanName.substring(0, humanName.indexOf(' '));
        return getKeyByDisplayName(name).getKey();
    }

    /**
     * Removes the given key from the index of usable keys kept by Panther.
     *
     * @param key The key to remove.
     * @return Whether or not the operation was successful.
     */
    public static boolean removeKey(PantherKey key)
    {
        return keys.remove(key);
    }

    public File getCurrentOpenFile()
    {
        return currentOpenFile;
    }

    public void setCurrentOpenFile(File file)
    {
        currentOpenFile = file;
    }

    /**
     * This method clears all the text in the main text area, making sure the the text
     * has been saved if the user wants to save it.
     * In this case, the file exists, but has been modified,
     * so confirm the clearing.
     *
     * @return whether the operation was successful.
     */
    public boolean clearText()
    {
        File file = getCurrentOpenFile();
        String txt = plaintext.getText();
        String msg = ResourceBundle.getBundle(BUNDLE_PATH).getString("confirm.clear");


        if (currentOpenFile != null)
        {
            // test for match
            if (!file.exists() || file.length() != txt.length())
            {
                // ask for confirmation
                JOptionPane.showConfirmDialog(this, msg, "", JOptionPane.YES_NO_CANCEL_OPTION);
            }
        }
        else
        {
            if (txt.length() != 0)
            {
                // ask for confirmation
            }
        }

        return true;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents()
    {
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            this.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        }

        JLabel keyLabel = new JLabel();
        JScrollPane plaintextPane = new JScrollPane();
        plaintext = new javax.swing.JTextArea()
        {

            @Override
            public void setBorder(Border border)
            {
                //Do nothing
            }
        };
        currentEncryptionKey = new javax.swing.JComboBox();
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu();
        JMenuItem openFileItem = new JMenuItem();
        JMenuItem saveFileItem = new JMenuItem();
        JMenuItem encryptFileItem = new JMenuItem();
        JMenuItem decryptFileItem = new JMenuItem();
        JMenuItem destroyFileItem = new JMenuItem();
        JMenuItem computeFileDigestItem = new JMenuItem();
        JSeparator decrypt_exitSeperator = new JSeparator();
        JMenuItem exitItem = new JMenuItem();
        JMenu editMenu = new JMenu();
        JMenuItem encryptTextItem = new JMenuItem();
        JMenuItem decryptTextItem = new JMenuItem();
        JMenuItem computeDigestItem = new JMenuItem();
        JSeparator useNative_decryptTextSeperator = new JSeparator();
        JMenuItem preferencesItem = new JMenuItem();
        JMenuItem changePassword = new JMenuItem();
        useNativeLookAndFeelItem = new javax.swing.JCheckBoxMenuItem();
        JMenu toolsMenu = new JMenu();
        JMenuItem keyManagerItem = new JMenuItem();
        showMessageDigestItem = new javax.swing.JCheckBoxMenuItem();
        JMenu helpMenu = new JMenu();
        JMenuItem helpItem = new JMenuItem();
        JSeparator help_aboutSeperator = new JSeparator();
        JMenuItem aboutItem = new JMenuItem();
        JToolBar tools = new JToolBar(JToolBar.HORIZONTAL);
        lockButton = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter()
        {

            @Override
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing();
            }
        });

        /*
         * Load resource bundle information (localization).
         */
        ResourceBundle bundle = ResourceBundle.getBundle(Panther.BUNDLE_PATH, Locale.getDefault());

        keyLabel.setText(bundle.getString("panther.label.key"));

        plaintext.setColumns(20);
        plaintext.setRows(5);
        plaintext.setTabSize(4);
        plaintextPane.setViewportView(plaintext);
        plaintext.setLineWrap(true);

        fileMenu.setText(bundle.getString("menu.file"));

        openFileItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openFileItem.setMnemonic('O');
        openFileItem.setText(bundle.getString("menu.file.open"));
        openFileItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openDiskFile();
            }
        });
        fileMenu.add(openFileItem);

        saveFileItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveFileItem.setMnemonic('S');
        saveFileItem.setText(bundle.getString("menu.file.save"));
        saveFileItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveFileItemActionPerformed();
            }
        });
        fileMenu.add(saveFileItem);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        encryptFileItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        encryptFileItem.setMnemonic('E');
        encryptFileItem.setText(bundle.getString("menu.file.encrypt"));
        encryptFileItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                encryptFileItemActionPerformed();
            }
        });
        fileMenu.add(encryptFileItem);

        decryptFileItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        decryptFileItem.setMnemonic('D');
        decryptFileItem.setText(bundle.getString("menu.file.decrypt"));
        decryptFileItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                decryptFileItemActionPerformed();
            }
        });
        fileMenu.add(decryptFileItem);

        destroyFileItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, java.awt.event.InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        destroyFileItem.setText(bundle.getString("menu.file.destroy"));
        destroyFileItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                destroyFileItemActionPerformed();
            }
        });
        fileMenu.add(destroyFileItem);

        computeFileDigestItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        computeFileDigestItem.setText(bundle.getString("menu.file.computedigest"));
        computeFileDigestItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                computeFileDigestItemActionPerformed();
            }
        });
        fileMenu.add(computeFileDigestItem);
        fileMenu.add(decrypt_exitSeperator);

        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitItem.setText(bundle.getString("menu.file.exit"));
        exitItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitItemActionPerformed();
            }
        });
        fileMenu.add(exitItem);

        mb.add(fileMenu);

        editMenu.setText(bundle.getString("menu.edit"));

        encryptTextItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        encryptTextItem.setText(bundle.getString("menu.edit.encrypt"));
        encryptTextItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                encryptTextItemActionPerformed();
            }
        });

        editMenu.add(encryptTextItem);

        decryptTextItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        decryptTextItem.setText(bundle.getString("menu.edit.decrypt"));
        decryptTextItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                decryptTextItemActionPerformed();
            }
        });
        editMenu.add(decryptTextItem);

        computeDigestItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        computeDigestItem.setText(bundle.getString("menu.edit.computedigest"));
        computeDigestItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                computeDigestItemActionPerformed();
            }
        });
        editMenu.add(computeDigestItem);
        editMenu.add(useNative_decryptTextSeperator);

        preferencesItem.setText(bundle.getString("menu.edit.preferences"));
        preferencesItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showPreferences();
            }
        });
        editMenu.add(preferencesItem);

        changePassword.setText(bundle.getString("menu.edit.password"));
        changePassword.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                changePasswordActionPerformed();
            }
        });
        editMenu.add(changePassword);

        useNativeLookAndFeelItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        useNativeLookAndFeelItem.setSelected(true);
        useNativeLookAndFeelItem.setText(bundle.getString("menu.edit.usenativelaf"));
        useNativeLookAndFeelItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                if(UIManager.getLookAndFeel().isNativeLookAndFeel())
                {
                    logger.log(Level.INFO, "Changing to cross-platform LAF.");
                    useNativeLookAndFeelItemActionPerformed(com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel.class.getName());
                }
                else
                {
                    logger.log(Level.INFO, "Changing to native LAF.");
                    useNativeLookAndFeelItemActionPerformed(UIManager.getSystemLookAndFeelClassName());
                }
            }
        });
        editMenu.add(useNativeLookAndFeelItem);

        mb.add(editMenu);

        toolsMenu.setText(bundle.getString("menu.tools"));

        keyManagerItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        keyManagerItem.setText(bundle.getString("menu.tools.keyman"));
        keyManagerItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showKeyManager();
            }
        });
        toolsMenu.add(keyManagerItem);

        showMessageDigestItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        showMessageDigestItem.setText(bundle.getString("menu.tools.showdigest"));
        showMessageDigestItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showMessageDigestItemActionPerformed();
            }
        });
        toolsMenu.add(showMessageDigestItem);

        mb.add(toolsMenu);

        helpMenu.setText(bundle.getString("menu.help"));

        helpItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpItem.setText(bundle.getString("menu.help.help"));
        helpItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                helpItemActionPerformed();
            }
        });
        helpMenu.add(helpItem);

        helpMenu.add(help_aboutSeperator);

        aboutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        aboutItem.setText(bundle.getString("menu.help.about"));
        aboutItem.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showAbout();
            }
        });
        helpMenu.add(aboutItem);

        mb.add(helpMenu);

        setJMenuBar(mb);

        lockButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent evt)
            {
                Panther.this.toggleLocked();
            }
        });

        /*
         * Set the toolbar button images based on platform.
         */
        if (System.getProperty("os.name").contains("Mac OS X"))
        {
            /* Set icons to Mac OS X specific values. */
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image unlockedIcon = tk.getImage("NSImage://NSLockLockedTemplate");
            ImageIcon imgIcon = new ImageIcon(unlockedIcon);
            lockButton.setIcon(imgIcon);
            lockButton.putClientProperty("JButton.buttonType", "textured");
            lockButton.setFocusable(false);
        }
        else
        {
            lockButton.setText("Lock");
        }

        /*
         * ----------------------------------------------------------------------+
         *                              Layout Code
         * ----------------------------------------------------------------------+
         */

        this.setLayout(new BorderLayout());

        /* Code for layout of the top of the panel. */
        tools.setMargin(new Insets(0, 4, 0, 0));
        tools.add(keyLabel);
        tools.add(currentEncryptionKey);
        tools.add(lockButton);
        tools.setFloatable(false);
        this.add(tools, BorderLayout.NORTH);

        /* Center layout code. */
        Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        plaintextPane.setBorder(border);
        if(System.getProperty("os.name").equals("Mac OS X"))
            plaintextPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(plaintextPane, BorderLayout.CENTER);

        /* Bottom Layout Code */
        JPanel bottomPanel = new JPanel();
        this.add(bottomPanel, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Shows the key manager dialog when the keyManagerItem JMenuItem is selected.
     */
    private void showKeyManager()
    {
        if (this.isLocked())
        {
            return;
        }

        /* Create a new instance of the key manager window. */
        KeyManagerWindow keyManager = new KeyManagerWindow();
        int parentWidth = this.getWidth();
        int parentHeight = this.getHeight();
        int width = keyManager.getWidth();
        int height = keyManager.getHeight();

        int x = (parentWidth / 2) - (width / 2) + getX();
        int y = (parentHeight / 2) - (height / 2) + getY();

        keyManager.setLocation(x, y);
        keyManager.setVisible(true);
        keyManager.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    /**
     * Encrypts the plaintext that is within the text area. The currently selected key will be used for
     * encryption, and therefore must be used for decryption also.  The encryption algorithm is specified
     * in the properties.properties file located within the Jar at
     * "/panthersleek/resources/properties/properties.properties"
     * The encrypted result will be saved on file.
     */
    private void encryptTextItemActionPerformed()
    {
        /* Create an array to hold the text contents and the encrypted bytes. */
        byte[] text;
        byte[] ciphertext;

        /* Get the byte content of the plaintext. */
        text = plaintext.getText().getBytes();

        /* If the Show Message Digest feature is turned on, find the digest and display it. */
        if (showDigest)
        {
            //Find the digest as a string.
            String displayDigest = getDigestAsString(text);

            //Create text component that holds the digest.
            JTextField digestField = new JTextField(displayDigest.length() + 2);
            digestField.setText(displayDigest);

            //Display the dialog.
            JOptionPane.showMessageDialog(this, digestField);
        }

        /* Obtain a working copy of the key in the key chooser. */
        String keyName = (String) currentEncryptionKey.getSelectedItem();
        String substring = keyName.substring(0, keyName.indexOf(' '));
        PantherKey wrapper = getKeyByDisplayName(substring);
        Key key = wrapper.getKey();

        /* Try to encrypt the bytes from the plaintext. */
        try
        {
            ciphertext = byteCipher.encrypt(key, text);
        }
        catch (InvalidKeyException ex)
        {
            logger.log(Level.WARNING, "Invalid Key!", ex);
            JOptionPane.showMessageDialog(this, "The selected key \"" + keyName + "\" does not support " + algorithm + " encryption.", "Invalid Key", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (IllegalBlockSizeException ex)
        {
            logger.log(Level.WARNING, "Illegal block size exception while encrypting text.", ex);
            JOptionPane.showMessageDialog(this, "The input appears to be invalid having an invalid block size, the input may be corrupt.", "Invalid Input Block Size", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (BadPaddingException ex)
        {
            logger.log(Level.WARNING, "Padding error while encrypting text.", ex);
            JOptionPane.showMessageDialog(this, "The input was improperly padded.  Please make sure the file was not corrupted.", "Improperly Padded Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /* Save the ciphertext bytes on file.
         * It would be useless to simply output the encrypted bytes,
         * because they do not convert to String and back
         * without measurable corruption.
         */

        /* Make sure that the filechooser is initialized. */
        File file = this.showFileDialog(FileDialog.SAVE);

        if (file != null)
        {
            /* Write the ciphertext to file. */
            try
            {
                /* Make sure the file has the correct file extension. */
                if (!file.exists())
                {
                    file = new File(file.getCanonicalPath() + ".ef");
                }

                /* Carry out the write operation. */
                writeFileBytes(file, ciphertext);
            }
            catch (IOException ex)
            {
                logger.log(Level.WARNING, "I/O error while writing to file.", ex);
                JOptionPane.showMessageDialog(this, "There was an error writing the encrypted information to file.", "Error Writing to File", JOptionPane.ERROR_MESSAGE);
            }

            JOptionPane.showMessageDialog(this, "The encryption operation completed successfully.", "Operation Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Opens a file chosen by the user.  The contents of the file are inserted into the text area.
     */
    protected void openDiskFile()
    {
        clearText();

        File file = this.showFileDialog(FileDialog.LOAD);
        if (file != null) //Proceed to read the file.
        {
            setCurrentOpenFile(file);
            PlaintextFileOpener fileOpener = new PlaintextFileOpener(file);

            fileOpener.addTextDeposit(plaintextDeposit);

            Thread openThread = new Thread(fileOpener);
            openThread.start();
        }
    }

    /**
     * Handles the closing of the window, and stores the properties and keys on the hard drive.
     */
    private void formWindowClosing()
    {
        /* 
         * Clean up and exit the program.
         */
        boolean confirmed = cleanUp();
        if (!confirmed)
        {
            // Abort
            return;
        }

        /* Everything is in order, quit the application. */
        System.exit(0);
    }

    private void saveFileItemActionPerformed()
    {
        File file = this.showFileDialog(FileDialog.SAVE);

        if (file != null)
        {
            PlaintextFileSaver filesaver = new PlaintextFileSaver(plaintext.getText(), file);

            Thread saveThread = new Thread(filesaver);
            saveThread.start();
        }
    }

    private void encryptFileItemActionPerformed()
    {
        //Create an array to hold the file contents, and one to hold the ciphertext.
        byte[] contents;
        byte[] ciphertext = null;

        //Show the choose file dialog.
        File file = this.showFileDialog(FileDialog.SAVE);
        if (file != null) //Check if the user approved.
        {
            //Read the bytes from the file into memory.
            contents = readFileBytes(file);

            //Try encrypting the bytes.
            try
            {
                //Obtain an encryption key.
                Key key = this.getSelectedKey();

                ciphertext = byteCipher.encrypt(key, contents); //Carry out encryption.
            }
            catch (InvalidKeyException ex)
            {
                logger.log(Level.WARNING, "Invalid key!", ex);
                JOptionPane.showMessageDialog(this, "The selected key \"" + currentEncryptionKey.getSelectedItem() + "\" does not support " + algorithm + "encryption.", "Invalid Key", JOptionPane.ERROR_MESSAGE);
            }
            catch (IllegalBlockSizeException ex)
            {
                logger.log(Level.WARNING, "Illegal block size exception while encrypting file.", ex);
                JOptionPane.showMessageDialog(this, "This file's block size is invalid.  Please make sure the file is not corrupted.", "Illegal Block Size", JOptionPane.ERROR_MESSAGE);
            }
            catch (BadPaddingException ex)
            {
                logger.log(Level.WARNING, "Padding error while encrypting file.", ex);
                JOptionPane.showMessageDialog(this, "This file is improperly padded.  Please make sure the file is not corrupted.", "Improper Padding", JOptionPane.ERROR_MESSAGE);
            }

            //Write the bytes back to the filesystem.
            File encryptedFile = new File(file.getPath() + ".ef");

            //Create the file if it doesn't exist.
            if (!encryptedFile.exists())
            {
                try
                {
                    //Create new file.
                    boolean fileWasCreated = encryptedFile.createNewFile();
                    if (!fileWasCreated)
                    {
                        String message = "Encrypted file could not be created.  Check that you have read/write permissions to the target directory.";
                        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (IOException ex)
                {
                    logger.log(Level.WARNING, "I/O error while creating file.", ex);
                }
            }

            //Try writing the bytes to file.
            try
            {
                writeFileBytes(encryptedFile, ciphertext);
            }
            catch (IOException ex)
            {
                logger.log(Level.WARNING, "I/O error while writing to file.", ex);
                JOptionPane.showMessageDialog(this, "There was an error writing the encrypted contents to file.\nMake Sure that you have read/write access to the target directory.", "Error Writing to File", JOptionPane.ERROR_MESSAGE);
            }

            JOptionPane.showMessageDialog(this, "The file encryption operation completed successfully.", "Operation Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void decryptFileItemActionPerformed()
    {
        //Declare an array to hold the file contents and plaintext bytes.
        byte[] contents;
        byte[] plaintextBytes = null;

        File file = this.showFileDialog(FileDialog.LOAD);
        if (file != null)
        {
            //Read the bytes from the file to memory.
            contents = readFileBytes(file);

            //Try to decrypt the bytes.
            try
            {
                //Obtain a decryption key.
                Key key = this.getSelectedKey();

                plaintextBytes = byteCipher.decrypt(key, contents);
            }
            catch (InvalidKeyException ex)
            {
                logger.log(Level.WARNING, "Invalid Key!", ex);
                JOptionPane.showMessageDialog(this, "The selected key \"" + currentEncryptionKey.getSelectedItem() + "\" does not support " + algorithm + " decryption.", "Invalid Key", JOptionPane.ERROR_MESSAGE);
            }
            catch (IllegalBlockSizeException ex)
            {
                logger.log(Level.SEVERE, "Illegal block size error while decrypting file.", ex);
                JOptionPane.showMessageDialog(this, "The file could not be decrypted because the data has an incorrect block size.", "Illegal Block Size", JOptionPane.ERROR_MESSAGE);
            }
            catch (BadPaddingException ex)
            {
                logger.log(Level.WARNING, "Padding error while decrypting file.", ex);
                JOptionPane.showMessageDialog(this, "The file is incorrectly padded, may be corrupted, or may need to be decrypted with a different key.", "Invalid Padding", JOptionPane.ERROR_MESSAGE);
            }

            //Try to write the plaintext back to the filesystem.
            File decryptedFile;

            //Give the new file the appropriate name.
            if (file.getName().endsWith(".ef")) //Make sure .ef extension is consistent.
            {
                decryptedFile = new File(file.getPath().substring(0, file.getPath().indexOf(".ef")));
            }
            else
            {
                decryptedFile = new File(file.getPath() + ".df");
            }


            try
            {
                //Create the new file if it doesn't exist.
                if (!decryptedFile.exists())
                {
                    boolean fileWasCreated = decryptedFile.createNewFile();

                    if (!fileWasCreated)
                    {
                        String message = "The decrypted file was not created! Please make sure you have read/write access to the location.";
                        JOptionPane.showMessageDialog(this, message, "Error creating file!", JOptionPane.ERROR_MESSAGE);
                    }
                }

                //Write the bytes to the file.
                writeFileBytes(decryptedFile, plaintextBytes);
            }
            catch (IOException ex)
            {
                logger.log(Level.WARNING, "I/O error while writing to file.", ex);
                JOptionPane.showMessageDialog(this, "There was an error writing to the decrypted file.", "Error Writing to File", JOptionPane.ERROR_MESSAGE);
            }

            JOptionPane.showMessageDialog(this, "The decryption operation was successful.", "Operation Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exitItemActionPerformed()
    {
        //Save the keys
        this.saveKeyStore();

        //destroy records of the password
        boolean destroyed = destroyPassword();
        if (!destroyed)
        {
            displayErrorMessage("Security Error!", "Unable to destroy copies of your password stored in RAM!");
        }

        //exit the application
        System.exit(0);
    }

    private void destroyFileItemActionPerformed()
    {
        File file = this.showFileDialog(FileDialog.LOAD);
        if (file != null)
        {
            String msg = "Are you sure you want to destroy the file " + file.getName() + "?\n";
            String warning = "Once destroyed, the file can not be recovered.";
            int agreed = JOptionPane.showConfirmDialog(this, msg + warning, "Destroy File?", JOptionPane.YES_NO_OPTION);

            if (agreed == JOptionPane.YES_OPTION)
            {
                try
                {
                    destroyFile(file);
                }
                catch (IOException ex)
                {
                    logger.log(Level.WARNING, "I/O error while writing to file.", ex);
                    JOptionPane.showMessageDialog(this, "There was an error overwriting and destroying the file.", "I/O Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Sets the look and feel to either the native or metal look and feel when the user
     * clicks the 'Use Native Look and Feel menu item.
     */
    private void useNativeLookAndFeelItemActionPerformed(String laf)
    {
        //Attempt to change the look and feel.
        try
        {
            UIManager.setLookAndFeel(laf);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException ex)
        {
            logger.log(Level.WARNING, "Look and feel class not found!", ex);
            JOptionPane.showMessageDialog(this, "There was a problem finding the selected look and feel.", "Look and Feel not Found", JOptionPane.ERROR_MESSAGE);
        }
        catch (InstantiationException ex)
        {
            logger.log(Level.WARNING, "Instantiation error while changing look and feel.", ex);
            JOptionPane.showMessageDialog(this, "Could not instantiate the selected look and feel.", "Error Instantiating Look and Feel", JOptionPane.ERROR_MESSAGE);
        }
        catch (IllegalAccessException ex)
        {
            logger.log(Level.WARNING, "Illegal access error while changing look and feel", ex);
            JOptionPane.showMessageDialog(this, "An access error occurred while setting the selected look and feel.", "Illegal Access Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (UnsupportedLookAndFeelException ex)
        {
            logger.log(Level.WARNING, "Unsupported look and feel.", ex);
            JOptionPane.showMessageDialog(this, "The selected look and feel is missing, or not supported on this platform.", "Unsupported Look And Feel", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decryptTextItemActionPerformed()
    {
        //Declare an array to hold the file contents and plaintext bytes.
        byte[] plain = null;

        File file = this.showFileDialog(FileDialog.LOAD);

        if (file != null)
        {
            //Read the contents of the file to memory.
            byte[] contents = readFileBytes(file);

            //Obtain a key for decryption.
            Key key = this.getSelectedKey();

            //Try to decrypt the text.
            try
            {
                plain = byteCipher.decrypt(key, contents);
            }
            catch (InvalidKeyException ex)
            {
                logger.log(Level.WARNING, "Invalid Key!", ex);
                JOptionPane.showMessageDialog(this, "The selected key \"" + currentEncryptionKey.getSelectedItem() + "\" does not support " + algorithm + " decryption.", "Invalid Key", JOptionPane.ERROR_MESSAGE);
            }
            catch (IllegalBlockSizeException ex)
            {
                logger.log(Level.WARNING, "Illegal block size exception while decrypting file.", ex);
                JOptionPane.showMessageDialog(this, "The block size in the file is illegal.  You may be using the wrong decryption key.", "Illegal Block Size", JOptionPane.ERROR_MESSAGE);
            }
            catch (BadPaddingException ex)
            {
                logger.log(Level.WARNING, "Bad padding exception while decrypting file.", ex);
                showErrorDialog("Bad Padding", "The file's padding does not match the key.", "Try using a different key for decryption.");
            }

            /* Display the decrypted plaintext. */
            if (plain != null)
            {
                this.plaintext.setText(new String(plain));
            }
        }
    }

    private void showMessageDigestItemActionPerformed()
    {
        showDigest = showMessageDigestItem.isSelected();
    }

    private void computeFileDigestItemActionPerformed()
    {
        //Show an open file dialog.
        File file = this.showFileDialog(FileDialog.LOAD);

        if (file != null)
        {
            /* Read its contents into a byte array. */
            byte[] contents = readFileBytes(file);

            /* Get the message digest as a hex string. */
            String viewableDigest = getDigestAsString(contents);

            /* Display the message digest in a dialog box. */
            JTextField messageDigestText = new JTextField(viewableDigest.length());
            JLabel digestName = new JLabel(Panther.getDigestAlgorithm() + " Fingerprint:");
            messageDigestText.setText(viewableDigest);
            JPanel contentPanel = new JPanel();
            contentPanel.add(digestName);
            contentPanel.add(messageDigestText);
            JOptionPane.showMessageDialog(this, contentPanel, "File Fingerprint", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void computeDigestItemActionPerformed()
    {
        /* Get the plaintext as a byte array. */
        byte[] text = plaintext.getText().getBytes();

        /* Compute the message digest as a hex string. */
        String viewableDigest = getDigestAsString(text);

        /* Display the message digest in a dialog box. */
        JPanel contentPanel = new JPanel();
        JTextField digestField = new JTextField(viewableDigest.length() - (viewableDigest.length() / 3));
        digestField.setText(viewableDigest);
        contentPanel.add(new JLabel(Panther.getDigestAlgorithm() + " Fingerprint:"));
        contentPanel.add(digestField);
        JOptionPane.showMessageDialog(this, contentPanel, "Message Fingerprint", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Toggles the interface between locked and unlocked mode.
     */
    protected void toggleLocked()
    {
        boolean doLock = !this.isLocked();
        plaintext.setEditable(!doLock);
        currentEncryptionKey.setEnabled(!doLock);
        this.setLocked(doLock);

        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            ImageIcon icon;
            if (doLock)
            {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockUnlockedTemplate"));
            }
            else
            {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockLockedTemplate"));
            }
            lockButton.setIcon(icon);
        }
        else
        {
            if (doLock)
            {
                lockButton.setText("Unlock");
            }
            else
            {
                lockButton.setText("Lock");
            }
        }
    }

    protected void showPreferences()
    {
        if (this.isLocked())
        {
            return;
        }

        Preferences preferencesDialog = new Preferences(this, true);
        preferencesDialog.setLocation(this.getX() + (this.getWidth() / 4), this.getY() + (this.getHeight() / 4));
        preferencesDialog.setVisible(true);

        Locale l = preferencesDialog.getLocaleChoice();
        String alg = preferencesDialog.getChosenAlgorithm();
        String mdAlgorthithm = preferencesDialog.getChosenDigestAlgorithm();
        boolean confirmed = preferencesDialog.getConfirmed();

        preferencesDialog.dispose();

        if (!confirmed)
        {
            return;
        }

        if (l != null)
        {
            if (!Locale.getDefault().getLanguage().contains(l.getLanguage()))
            {
                changeLocale(l);
            }
        }

        if (alg != null)
        {
            if (!alg.equals(getAlgorithm()))
            {
                /* Change the algorithm in the currently running application. */
                setAlgorithm(alg);
            }
        }

        if (mdAlgorthithm != null)
        {
            if (!mdAlgorthithm.equals(getDigestAlgorithm()))
            {
                setDigestAlgorithm(mdAlgorthithm);
            }
        }
    }

    protected void showAbout()
    {
        AboutDialog dialog = new AboutDialog(Panther.this, false);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onlineHelpItemActionPerformed()
    {
        //Launch online help in the default browser.
        java.net.URI uri;
        try
        {
            //Obtain the URI for the online help.
            Properties pantherProperties = Panther.loadPantherInformation();
            String helpURL = (String) pantherProperties.get("panther.site.help");
            uri = new java.net.URI(helpURL);

            //Show the uri in the default browser.
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(uri);
        }
        catch (URISyntaxException ex)
        {
            logger.log(Level.WARNING, "URI syntax error while opening webpage.", ex);
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "I/O error while opening webpage.", ex);
            displayErrorMessage("I/O Exception", "Unable to communicate with URL.");
        }
    }

    private void helpItemActionPerformed()
    {
        // Open the documentation PDF
        Desktop env = Desktop.getDesktop();
        try
        {
            URI doc = new URI("http://doc.tamalin.org/panthersleek-doc.pdf");
            env.browse(doc);
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, "I/O Error: Unable to open help file.", ex);
        }
        catch(java.net.URISyntaxException ex)
        {
            logger.log(Level.SEVERE, "URI Syntax Error: Unable to open help file.", ex);
        }
    }

    private void changePasswordActionPerformed()
    {
        if (this.isLocked())
        {
            return;
        }

        String msg = ResourceBundle.getBundle(BUNDLE_PATH).getString("panther.prompt.newpwd");
        PasswordPrompt pp = new PasswordPrompt(null, true, msg);
        pp.setVisible(true);

        char[] pswd = pp.getPassword();
        if(pswd == null) return;

        msg = ResourceBundle.getBundle(BUNDLE_PATH).getString("panther.prompt.confirmpwd");
        pp = new PasswordPrompt(null, true, msg);
        pp.setVisible(true);

        if(pp.getPassword() != null)
        {
            char[] confirm = pp.getPassword();
            if(!Arrays.equals(pswd, confirm))
            {
                showErrorDialog("Password Mismatch", "The provided passwords do not match!", "Please try entering your password again.  Note: the password is case sensitive.");
                return;
            }

            //Assign the new value to the password field
            password = pswd;

            //Apply the changes
            this.saveKeyStore();

            JOptionPane.showMessageDialog(this, "The password change was successful.", "Password Changed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class PlaintextDeposit implements TextDepositor
    {

        public void loadText(String text)
        {
            plaintext.setText(text);
        }
    }

    public File showFileDialog(int mode)
    {
        if (fileDialog == null)
        {
            fileDialog = new FileDialog(this);
        }

        fileDialog.setMode(mode);
        fileDialog.setVisible(true);

        String path = fileDialog.getDirectory() + fileDialog.getFile();
        System.out.println(logger.getLevel().toString());
        logger.log(Level.FINE, "Opening file {0}", path);
        return (fileDialog.getFile() != null) ? new File(path) : null;
    }

    /**
     * Reads a file and returns it's contents as a byte array.
     *
     * @param file The file to read.
     * @return The bytes contained in the given file.
     */
    public byte[] readFileBytes(File file)
    {
        RandomAccessFile in;
        byte[] bytes = null;

        //Attempt to access the file.
        try
        {
            in = new RandomAccessFile(file, "r");

            //Try to read from the file.
            try
            {
                int length = (int) in.length();

                bytes = new byte[length];

                //Read the file's bytes into the array.
                in.readFully(bytes);
            }
            finally
            {
                //Close the file.
                in.close();
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "", ex);
            JOptionPane.showMessageDialog(this, "An error occurred while reading the file.", "Error Reading File", JOptionPane.ERROR_MESSAGE);
        }

        //Return the file's contents.
        return bytes;
    }

    /**
     * Writes the provided bytes to the specified file on disk.
     *
     * @param file  The file to write to.
     * @param bytes The bytes to write to file.
     * @throws java.io.IOException throws IOexception when an error is encountered while writing to the file.
     */
    public void writeFileBytes(File file, byte[] bytes) throws IOException
    {
        /* Make sure the file exists first! */
        if (!file.exists())
        {
            boolean created = file.createNewFile();
            if (!created)
            {
                displayErrorMessage("Error", "Unable to create the file!  Make sure the disk has write access.");
            }
        }

        /* Create a stream to write the bytes. */
        RandomAccessFile out = new RandomAccessFile(file, "rw");

        //Attempt to write the bytes to the file and close the file.
        try
        {
            out.write(bytes);
        }
        finally
        {
            out.close();
        }
    }

    private void destroyFile(File file) throws IOException
    {
        RandomAccessFile out = new RandomAccessFile(file, "rw");

        //Set the progress bar to display write progress.
        //Attempt to write the bytes to the file and close the file.
        /* This method is similar to the Unix "shred" command. */
        try
        {
            int length = (int) out.length();

            for (int j = 0; j < 5; j++)
            {
                for (int i = 0; i < length; i++)
                {
                    byte over = (byte) (i % 256);

                    out.writeByte(over); //Write the cover-up byte to file.
                }
            }
        }
        finally
        {
            out.close();
        }

        try
        {
            boolean deleted = file.delete();

            if (!deleted)
            {
                String msg = "Unable to delete " + file.getName();
                JOptionPane.showMessageDialog(this, msg, "Unable to delete file!", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "", ex);
            displayErrorMessage("Error", "Unable to delete the file!  Make sure that write access is available.");
        }

        JOptionPane.showMessageDialog(this, "The operation was successful.", "File destroyed", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String getDigestAsString(byte[] bytes)
    {
        //Create an array to hold the digest in.
        byte[] digest = null;

        MessageDigest messageDigest = null;

        try
        {
            /* Create the MessageDigest object with the correct algorithm. */
            messageDigest = MessageDigest.getInstance(Panther.getDigestAlgorithm());

            /* Feed the message into the MessageDigest object. */
            messageDigest.update(bytes);
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm error while computing fingerprint.", ex);
            displayErrorMessage("Fingerprint Algorithm Unsupported", "This algorithm is not supported for fingerprinting.");
        }

        if (messageDigest != null)
        {
            digest = messageDigest.digest();
        }

        /* Convert the digested bytes to more "friendly" hexidecimal. */
        StringBuilder hexBuilder = new StringBuilder();
        if (digest != null)
        {
            for (int i = 0; i < digest.length; i += 2)
            {
                hexBuilder.append(Integer.toHexString(digest[i] & 0xFF).toUpperCase());
                if (i != (digest.length - 2))
                {
                    hexBuilder.append(":");
                }
            }
        }

        return hexBuilder.toString();
    }

    public void changeLocale(Locale l)
    {
        String msg = ResourceBundle.getBundle(BUNDLE_PATH).getString("panther.restart");
        String title = ResourceBundle.getBundle(BUNDLE_PATH).getString("confirm.title");
        int confirm = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_CANCEL_OPTION);

        if(confirm == JOptionPane.YES_OPTION);
        {
            Locale.setDefault(l);
            cleanUp();
            System.exit(0);
        }
    }

    public boolean destroyPassword()
    {
        if (password != null)
        {
            int l = password.length;
            for (int c = 0; c < 5; c++)
            {
                for (int i = 0; i < l; i++)
                {
                    password[i] = 0;
                }
            }

            return true;
        }

        /* When this method returns false, the password is null,
         * which could be a problem.
         */
        return false;
    }

    private void saveKeyStore()
    {
        /*-------------------------------+
         * Store Encrypted KeyStore
         *-------------------------------+*/
        String errorMessageTitle = "Keystore Error";
        //File KeyStore file and containing directory
        File file = getKeyStoreFile();
        File directory = file.getParentFile();

        //make sure keystore is initialized
        if (encryptedKeyStore == null)
        {
            try
            {
                encryptedKeyStore = KeyStore.getInstance("JCEKS");
            }
            catch (KeyStoreException ex)
            {
                logger.log(Level.SEVERE, "Keystore error while creating keystore.", ex);
                displayErrorMessage(errorMessageTitle, "Could not create the encrypted keystore.");
            }
        }

        //Prompt for key storage password if password doesn't exist
        if (password == null)
        {
            PasswordPrompt prompt = new PasswordPrompt(this, true, "Please enter a password.");
            prompt.setVisible(true);
            password = prompt.getPassword();
        }

        //try to add all the keys to the KeyStore
        try
        {
            if (keys == null)
            {
                keys = new ArrayList<PantherKey>();
            }

            for (PantherKey key : keys)
            {
                //Generate a SecretKey from the key
                SecretKey secretKey = (SecretKey) key.getKey();

                //Find the key's information
                String alias = key.getDisplayName();
                SecretKeyEntry keyEntry = new SecretKeyEntry(secretKey);

                //Store the key in the KeyStore
                encryptedKeyStore.setEntry(alias, keyEntry, new PasswordProtection(password));
            }
        }
        catch (KeyStoreException ex)
        {
            logger.log(Level.SEVERE, "Keystore exception while saving keystore.", ex);
            Panther.displayErrorMessage(errorMessageTitle, "An error occurred while your keys were being stored.");
            return;
        }

        //encrypt to KeyStore with password
        try
        {
            //check if the directory exists
            if (!directory.exists())
            {
                boolean created = directory.mkdir();
                if (!created)
                {
                    String message = "Unable to create the required directory \"" + directory.getName() + "\".";
                    displayErrorMessage(errorMessageTitle, message);
                }
            }

            //check if the key storage file exists
            if (!file.exists())
            {
                boolean created = file.createNewFile();
                if (!created)
                {
                    String message = "Unable to create required keystore file.";
                    displayErrorMessage(errorMessageTitle, message);
                }
            }

            /* Carry out the encrypted write operation. */
            FileOutputStream secureOut = new FileOutputStream(file);
            encryptedKeyStore.store(secureOut, password);
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, "I/O error while saving keystore.", ex);
            displayErrorMessage(errorMessageTitle, "Unable to write data to disk.");
        }
        catch (KeyStoreException ex)
        {
            logger.log(Level.SEVERE, "Keystore error while saving keystore.", ex);
            Panther.displayErrorMessage(errorMessageTitle, "There was an error encrypting and storing your keys.");
        }
        catch (NoSuchAlgorithmException ex)
        {
            logger.log(Level.SEVERE, "Algorithm error while saving keystore.", ex);
            Panther.displayErrorMessage(errorMessageTitle, "The algorithm \"" + getAlgorithm() + "\" is not available in this environment.");
        }
        catch (java.security.cert.CertificateException ex)
        {
            logger.log(Level.SEVERE, "Certificate error while saving keystore.", ex);
            Panther.displayErrorMessage(errorMessageTitle, "The security certificate for the keystore is invalid.");
        }
    }

    protected boolean cleanUp()
    {
        /* Save KeyStore. */
        this.saveKeyStore();

        /* Save Data. */
        boolean cleared = clearText();
        if (!cleared)
        {
            /* Abort the operation. */
            return false;
        }

        /* Save Preferences. */
        Properties properties = new Properties();
        try
        {
            File propertyFile = new File(getFileStorageDir().getCanonicalPath() + separator() + "properties.properties");
            FileOutputStream fos = new FileOutputStream(propertyFile);
            properties.put("showDigest", ((Boolean) showDigest).toString());
            properties.put("useNativeLookAndFeel", ((Boolean) useNativeLookAndFeelItem.isSelected()).toString());
            properties.put("lang", Locale.getDefault().getLanguage());
            properties.put("encryption_algorithm", algorithm);
            properties.put("digest_algorithm", digestAlgorithm);

            String comment = "This is a properties file, do NOT modify it.";
            properties.store(fos, comment);
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, "I/O error while saving preferences.", ex);
        }


        /* Hide everything. */
        this.setVisible(false);

        /* Overwrite the password array so it is not "free-floating" in memory. */
        this.destroyPassword();

        /* Return true, the operation was successful. */
        return true;
    }

    public static void displayErrorMessage(String title, String content)
    {
        JOptionPane.showMessageDialog(null, content, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showErrorDialog(String title, String description, String suggestion)
    {
        String content = "<html><p><b>" + description + "</b></p><br /><p>" + suggestion + "</p></html>";
        JOptionPane.showMessageDialog(null, content, title, JOptionPane.ERROR_MESSAGE);
    }
    private static JComboBox currentEncryptionKey;
    private JTextArea plaintext;
    private JCheckBoxMenuItem showMessageDigestItem;
    private JCheckBoxMenuItem useNativeLookAndFeelItem;
    private JButton lockButton;
    private boolean locked;
    private static ArrayList<PantherKey> keys;
    private FileDialog fileDialog;
    private static String algorithm = "";
    private static String digestAlgorithm = "";
    private PlaintextDeposit plaintextDeposit;
    private ByteCipher byteCipher;
    private boolean showDigest = false;
    private static KeyStore encryptedKeyStore;
    private static char[] password = null;
    private File currentOpenFile = null;
    public static final String VERSION = "2.2";
    public static final String BUNDLE_PATH = "panthersleek.resources.international.panthersleek";
    private static final Logger logger = Logger.getLogger(Panther.class.getName());
}