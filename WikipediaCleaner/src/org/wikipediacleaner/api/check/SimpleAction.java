/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2008  Nicolas Vervelle
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

package org.wikipediacleaner.api.check;

import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.text.Element;


/**
 * A simple action.
 */
class SimpleAction implements Actionnable {

  private final String name;
  private final ActionProvider actionProvider;

  /**
   * @param name Action name.
   * @param actionProvider Action provider.
   */
  SimpleAction(String name, ActionProvider actionProvider) {
    this.name = name;
    this.actionProvider = actionProvider;
  }

  /* (non-Javadoc)
   * @see org.wikipediacleaner.api.check.Actionnable#getAction(javax.swing.text.Element, javax.swing.JTextPane)
   */
  public Action getAction(Element element, JTextPane textPane) {
    if (actionProvider == null) {
      return null;
    }
    return actionProvider.getAction(element, textPane);
  }

  /* (non-Javadoc)
   * @see org.wikipediacleaner.api.check.Actionnable#getActions()
   */
  public ArrayList<Actionnable> getActions() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.wikipediacleaner.api.check.Actionnable#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.wikipediacleaner.api.check.Actionnable#isCompositeAction()
   */
  public boolean isCompositeAction() {
    return false;
  }
}
