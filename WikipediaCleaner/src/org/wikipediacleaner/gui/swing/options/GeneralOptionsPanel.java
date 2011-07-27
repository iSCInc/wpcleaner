/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2011  Nicolas Vervelle
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wikipediacleaner.gui.swing.options;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.wikipediacleaner.gui.swing.basic.Utilities;
import org.wikipediacleaner.i18n.GT;
import org.wikipediacleaner.utils.ConfigurationValueBoolean;
import org.wikipediacleaner.utils.ConfigurationValueInteger;
import org.wikipediacleaner.utils.ConfigurationValueString;


/**
 * A panel for general options. 
 */
public class GeneralOptionsPanel extends OptionsPanel {

  /**
   * Serialisation.
   */
  private static final long serialVersionUID = -7125394212316622303L;

  /**
   * Construct a General Options panel. 
   */
  public GeneralOptionsPanel() {
    super(new GridBagLayout());
    initialize();
  }

  /**
   * Initialize the panel.
   */
  private void initialize() {
    setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), GT._("General options")));
    JCheckBox chk = null;
    JSpinner spin = null;
    JTextField txt = null;

    // Initialize constraints
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.ipadx = 0;
    constraints.ipady = 0;
    constraints.weightx = 0;
    constraints.weighty = 0;

    constraints.gridwidth = 3;

    // Close disambiguation window after sending
    chk = createJCheckBox(
        GT._("Close disambiguation window after sending"),
        ConfigurationValueBoolean.CLOSE_DISAMBIG);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Restore window position
    chk = createJCheckBox(
        GT._("Restore window position"),
        ConfigurationValueBoolean.RESTORE_WINDOW);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Save window position
    chk = createJCheckBox(
        GT._("Save window position"),
        ConfigurationValueBoolean.SAVE_WINDOW);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Use short notation
    chk = createJCheckBox(
        GT._("Use short notation [[Xxxxx (yyy)|]]"),
        ConfigurationValueBoolean.SHORT_NOTATION);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Use advanced features
    chk = createJCheckBox(
        GT._("Use advanced features (experimental)"),
        ConfigurationValueBoolean.ADVANCED_FEATURES);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Display "WikiCleaner" in the update comments
    chk = createJCheckBox(
        GT._("Display WikiCleaner link in update comments"),
        ConfigurationValueBoolean.WIKICLEANER_COMMENT);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Show 0 errors in Check Wiki
    chk = createJCheckBox(
        GT._("Show errors with no detection found"),
        ConfigurationValueBoolean.CHECK_SHOW_0_ERRORS);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Link to error description in comment for Check Wiki
    chk = createJCheckBox(
        GT._("Add link to error description in comments"),
        ConfigurationValueBoolean.CHECK_LINK_ERRORS);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Force watching pages that have been edited
    chk = createJCheckBox(
        GT._("Watch all edited pages"),
        ConfigurationValueBoolean.FORCE_WATCH);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Check orthograph
    chk = createJCheckBox(
        GT._("Check orthograph and typography"),
        ConfigurationValueBoolean.ORTHOGRAPH);
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(chk, constraints);
    constraints.gridy++;

    // Menu size
    spin = createJSpinner(
        ConfigurationValueInteger.MENU_SIZE,
        2, 999, 1);
    JLabel labelMenuSize = Utilities.createJLabel(GT._("Maximum number of items in a menu :"));
    labelMenuSize.setLabelFor(spin);
    labelMenuSize.setHorizontalAlignment(SwingConstants.TRAILING);
    constraints.gridwidth = 2;
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(labelMenuSize, constraints);
    constraints.gridwidth = 1;
    constraints.gridx = 2;
    constraints.weightx = 1;
    add(spin, constraints);
    constraints.gridy++;

    // Max pages
    spin = createJSpinner(
        ConfigurationValueInteger.MAXIMUM_PAGES,
        1, 99, 1);
    JLabel labelMaxPages = Utilities.createJLabel(GT._("Maximum number of simultaneous analysis :"));
    labelMaxPages.setLabelFor(spin);
    labelMaxPages.setHorizontalAlignment(SwingConstants.TRAILING);
    constraints.gridwidth = 2;
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(labelMaxPages, constraints);
    constraints.gridwidth = 1;
    constraints.gridx = 2;
    constraints.weightx = 1;
    add(spin, constraints);
    constraints.gridy++;

    // Max errors for Check Wiki
    spin = createJSpinner(
        ConfigurationValueInteger.CHECK_NB_ERRORS,
        10, 1000, 5);
    JLabel labelMaxErrorsCheckWiki = Utilities.createJLabel(
        GT._("Maximum number of errors for Check Wiki :"));
    labelMaxErrorsCheckWiki.setLabelFor(spin);
    labelMaxErrorsCheckWiki.setHorizontalAlignment(SwingConstants.TRAILING);
    constraints.gridwidth = 2;
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(labelMaxErrorsCheckWiki, constraints);
    constraints.gridwidth = 1;
    constraints.gridx = 2;
    constraints.weightx = 1;
    add(spin, constraints);
    constraints.gridy++;

    // Interrogation threads
    spin = createJSpinner(
        ConfigurationValueInteger.INTERROG_THREAD,
        1, 99, 1);
    JLabel labelThreads = Utilities.createJLabel(GT._("Maximum number of interrogation threads :"));
    labelThreads.setLabelFor(spin);
    labelThreads.setHorizontalAlignment(SwingConstants.TRAILING);
    constraints.gridwidth = 2;
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(labelThreads, constraints);
    constraints.gridwidth = 1;
    constraints.gridx = 2;
    constraints.weightx = 1;
    add(spin, constraints);
    constraints.gridy++;

    // Signature
    txt = createJTextField(ConfigurationValueString.SIGNATURE, 15);
    JLabel labelSignature = Utilities.createJLabel(GT._("Signature :"));
    labelSignature.setLabelFor(txt);
    labelSignature.setHorizontalAlignment(SwingConstants.TRAILING);
    constraints.gridwidth = 1;
    constraints.gridx = 0;
    constraints.weightx = 0;
    add(labelSignature, constraints);
    constraints.gridwidth = 2;
    constraints.gridx++;
    constraints.weightx = 1;
    add(txt, constraints);
    constraints.gridy++;

    // Empty panel
    JPanel emptyPanel = new JPanel();
    emptyPanel.setMinimumSize(new Dimension(0, 0));
    emptyPanel.setPreferredSize(new Dimension(0, 0));
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridwidth = 3;
    constraints.gridx = 0;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 1;
    constraints.weighty = 1;
    add(emptyPanel, constraints);
  }
}