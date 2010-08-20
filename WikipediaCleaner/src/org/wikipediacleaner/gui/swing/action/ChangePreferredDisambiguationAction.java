/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2007  Nicolas Vervelle
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

package org.wikipediacleaner.gui.swing.action;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.text.TextAction;

import org.wikipediacleaner.utils.Configuration;


/**
 * An action listener for adding a preferred disambiguation.
 */
@SuppressWarnings("serial")
public class ChangePreferredDisambiguationAction extends TextAction {

  private final String page;
  private final String preferred;
  private final boolean add;

  public ChangePreferredDisambiguationAction(
      String page,
      String preferred,
      boolean add) {
    super("AddPreferredDisambiguation");
    this.page = page;
    this.preferred = preferred;
    this.add = add;
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
    Configuration config = Configuration.getConfiguration();
    List<String> preferredDabs = config.getStringSubList(
        Configuration.SUB_ARRAY_PREFERRED_DAB, page);
    if (add) {
      if (!preferredDabs.contains(preferred)) {
        preferredDabs.add(preferred);
        Collections.sort(preferredDabs);
        config.setStringSubList(Configuration.SUB_ARRAY_PREFERRED_DAB, page, preferredDabs);
      }
    } else {
      if (preferredDabs.contains(preferred)) {
        preferredDabs.remove(preferred);
        Collections.sort(preferredDabs);
        config.setStringSubList(Configuration.SUB_ARRAY_PREFERRED_DAB, page, preferredDabs);
      }
    }
  }
}