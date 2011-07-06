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
 * AboutDialog.java
 *
 * Created on Dec 4, 2008, 4:22:19 PM
 */

package org.tamalin.panthersleek;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/**
 * The about dialog is a dialog presented to show information pertaining the application and it's environment,
 * including the license, version number, help pages, etc.
 *
 * @author Quytelda K. Gaiwin
 */
public class AboutDialog extends javax.swing.JDialog
{

    /**
     * Creates new form AboutDialog
     *
     * @param parent The parent frame: this may be null
     * @param modal  whether or not the dialog is modal
     */
    public AboutDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        this.setResizable(false);
    }
    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents()
    {
        title = new JLabel("<html><center><p><b>Panther Sleek</b></p><br /><small><p>Version: " + Panther.VERSION + "</p><br /><p>Copyright 2010-2011 Tamalin<br />Under the Apache License</p></small></center></html>");
        icon = new JPanel();
        text = new JPanel();
        content = new JPanel();

        // load the logo icon
        Toolkit tk = Toolkit.getDefaultToolkit();
        URL url = Panther.class.getResource("/panthersleek/resources/border-logo.png");
        logo = new ImageIcon(tk.getImage(url));
        brand = new JLabel();
        brand.setIcon(logo);

        /*#############*
         * Layout Code *
         *#############*/
        content.setLayout(new BorderLayout());
        EmptyBorder border = new EmptyBorder(INSET / 4, INSET + 15, INSET / 4, INSET + 15);
        content.setBorder(border);
        

        content.add(icon, BorderLayout.CENTER);
        content.add(text, BorderLayout.SOUTH);
        icon.add(brand, JLabel.CENTER_ALIGNMENT);
        text.add(title, JLabel.CENTER_ALIGNMENT);

        this.add(content);
    }

    private JLabel title;
    private ImageIcon logo;
    private JLabel brand;

    private JPanel content, icon, text;

    private final int INSET = 50;
}