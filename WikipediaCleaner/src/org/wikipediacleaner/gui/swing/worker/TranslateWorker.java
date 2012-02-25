/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2012  Nicolas Vervelle
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

package org.wikipediacleaner.gui.swing.worker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.wikipediacleaner.api.base.API;
import org.wikipediacleaner.api.base.APIException;
import org.wikipediacleaner.api.base.APIFactory;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.data.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementInternalLink;
import org.wikipediacleaner.gui.swing.basic.BasicWindow;
import org.wikipediacleaner.gui.swing.basic.BasicWorker;
import org.wikipediacleaner.i18n.GT;


/**
 * SwingWorker for translating a page.
 */
public class TranslateWorker extends BasicWorker {

  private final EnumWikipedia from;
  private final Page page;
  private final String initialText;

  /**
   * @param wikipedia Wikipedia.
   * @param window Window.
   * @param from Original Wikipedia.
   * @param page Page.
   * @param text Page contents.
   */
  public TranslateWorker(
      EnumWikipedia wikipedia, BasicWindow window,
      EnumWikipedia from,
      Page page, String text) {
    super(wikipedia, window);
    this.from = from;
    this.page = page;
    this.initialText = text;
  }

  /**
   * @return Translated text.
   * @see org.wikipediacleaner.gui.swing.basic.BasicWorker#construct()
   */
  @Override
  public Object construct() {
    setText(GT._("Retrieving MediaWiki API"));
    API api = APIFactory.getAPI();
    String text = initialText;
    PageAnalysis analysis = new PageAnalysis(page, text);

    // Replacing all links
    try {
      Collection<PageElementInternalLink> links = analysis.getInternalLinks();
      Map<String, String> interwikis = new HashMap<String, String>();
      StringBuilder newText = new StringBuilder();
      int lastPosition = 0;
      for (PageElementInternalLink link : links) {
        setText(GT._("Retrieving interwiki for {0}", link.getLink()));
        String linkPage = link.getLink();
        String translated = null;
        if (!interwikis.containsKey(linkPage)) {
          translated = api.getLanguageLink(from, getWikipedia(), linkPage);
          interwikis.put(linkPage, translated);
        } else {
          translated = interwikis.get(linkPage);
        }
        if ((translated != null) && !Page.areSameTitle(linkPage, translated)) {
          if (link.getBeginIndex() > lastPosition) {
            newText.append(text.substring(lastPosition, link.getBeginIndex()));
            lastPosition = link.getBeginIndex();
          }
          newText.append("[[");
          newText.append(translated);
          newText.append("|");
          newText.append(link.getDisplayedText());
          newText.append("]]");
          lastPosition = link.getEndIndex();
        }
      }
      if (lastPosition < text.length()) {
        newText.append(text.substring(lastPosition));
        lastPosition = text.length();
      }
      text = newText.toString();
    } catch (APIException e) {
      return null;
    }

    return text;
  }
}