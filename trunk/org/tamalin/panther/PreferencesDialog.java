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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The user options and preferences.
 *
 * @author Quytelda K. Gaiwin
 * @version 2.0
 * @since 3.1
 */
public class PreferencesDialog extends JDialog
{
    public PreferencesDialog(JFrame parent)
    {
        super(parent, true);
        setLayout(new BorderLayout());
        setTitle("Preferences");
        setResizable(false);
        initUI();
    }

    public void setDefaultDigestAlg(String alg)
    {
        if(alg.equals("SHA-1"))
        {
            digestSHA1Option.setSelected(true);
        }
        else
        {
            digestMD5Option.setSelected(true);  
        }
    }

    public String getChosenDigestAlgorithm()
    {
        return digestAlgorithm;
    }

    public float getHideOpacity()
    {
        return hideOpacity;
    }

    public boolean isApproved()
    {
        return changeApproved;
    }

    public void initUI()
    {
        digestSHA1Option = new JRadioButton("SHA-1 Fingerprints");
        digestMD5Option = new JRadioButton("MD5 Fingerprints");
        ButtonGroup bg = new ButtonGroup();
        opacity = new JSpinner();
        JButton cancelOption = new JButton("Cancel");
        JButton okOption = new JButton("OK");
        JPanel bottomPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel opacityPanel = new JPanel();

        okOption.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                changeApproved = true;

                if (digestSHA1Option.isSelected())
                    digestAlgorithm = SHA1;
                else
                    digestAlgorithm = MD5;

                hideOpacity = (Integer) opacity.getValue();
                hideOpacity /= 100;

                setVisible(false);
            }
        });

        cancelOption.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                changeApproved = false;

                setVisible(false);
            }
        });

        bg.add(digestSHA1Option);
        bg.add(digestMD5Option);

        int value = (int) (Panther.getHideOpacity() * 100);
        SpinnerModel model = new SpinnerNumberModel(value, 0, 10, 1);
        opacity.setModel(model);

        centerPanel.setLayout(new GridLayout(2, 1));
        centerPanel.add(digestSHA1Option);
        centerPanel.add(digestMD5Option);
        centerPanel.setBorder(BorderFactory.createTitledBorder("Fingerprint Algorithm"));
        opacityPanel.add(opacity);
        opacityPanel.setBorder(BorderFactory.createTitledBorder("Hidden Window Opacity"));

        bottomPanel.setLayout(new GridLayout(1, 2));
        bottomPanel.add(okOption);
        bottomPanel.add(cancelOption);

        this.add(centerPanel, BorderLayout.NORTH);
        this.add(opacityPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        pack();
    }

    private JRadioButton digestSHA1Option, digestMD5Option;
    private JSpinner opacity;
    public static final String SHA1 = "SHA-1";
    public static final String MD5 = "MD5";
    private String digestAlgorithm;
    private float hideOpacity;
    private boolean changeApproved = false;
}
