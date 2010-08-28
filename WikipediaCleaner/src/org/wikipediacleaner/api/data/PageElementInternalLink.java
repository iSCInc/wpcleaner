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

package org.wikipediacleaner.api.data;

import org.wikipediacleaner.api.constants.EnumWikipedia;


/**
 * Class containing informations about a complete internal link ([[link#anchor|text]]). 
 */
public class PageElementInternalLink {
  private final int beginIndex;
  private final int endIndex;
  private final String linkNotTrimmed;
  private final String link;
  private final String anchorNotTrimmed;
  private final String anchor;
  private final String textNotTrimmed;
  private final String text;

  /**
   * Analyze contents to check if it matches an internal link.
   * 
   * @param wikipedia Wikipedia.
   * @param contents Contents.
   * @param index Block start index.
   * @return Block details it there's a block.
   */
  public static PageElementInternalLink analyzeBlock(
      EnumWikipedia wikipedia, String contents, int index) {
    // Verify arguments
    if (contents == null) {
      return null;
    }

    // Look for '[['
    int tmpIndex = index;
    if ((tmpIndex >= contents.length()) ||
        (!contents.startsWith("[[", tmpIndex))) {
      return null;
    }
    tmpIndex += 2;
    int beginIndex = tmpIndex;

    // Possible whitespaces characters
    while ((tmpIndex < contents.length()) && (contents.charAt(tmpIndex) == ' ')) {
      tmpIndex++;
    }

    // Find elements of internal link
    if (tmpIndex >= contents.length()) {
      return null;
    }
    int endIndex = contents.indexOf("]]", tmpIndex);
    if (endIndex < 0) {
      return null;
    }
    int anchorIndex = contents.indexOf('#', tmpIndex);
    int pipeIndex = contents.indexOf('|', tmpIndex);

    // Create internal link
    if ((pipeIndex >= 0) && (pipeIndex < endIndex)) {
      if ((anchorIndex >= 0) && (anchorIndex < pipeIndex)) {
        return new PageElementInternalLink(
            index, endIndex + 2,
            contents.substring(beginIndex, anchorIndex),
            contents.substring(anchorIndex + 1, pipeIndex),
            contents.substring(pipeIndex + 1, endIndex));
      }
      return new PageElementInternalLink(
          index, endIndex + 2,
          contents.substring(beginIndex, pipeIndex),
          null,
          contents.substring(pipeIndex + 1, endIndex));
    }
    if ((anchorIndex >= 0) && (anchorIndex < endIndex)) {
      return new PageElementInternalLink(
          index, endIndex + 2,
          contents.substring(beginIndex, anchorIndex),
          contents.substring(anchorIndex + 1, endIndex), null);
    }
    return new PageElementInternalLink(
        index, endIndex + 2,
        contents.substring(beginIndex, endIndex),
        null, null);
  }

  public int getBeginIndex() {
    return beginIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public String getLink() {
    return link;
  }

  public String getAnchor() {
    return anchor;
  }

  public String getFullLink() {
    if (anchor == null) {
      return link;
    }
    return link + "#" + anchor;
  }

  public String getText() {
    return text;
  }

  private PageElementInternalLink(
      int beginIndex, int endIndex,
      String link, String anchor, String text) {
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
    this.linkNotTrimmed = link;
    this.link = (link != null) ? Page.getStringUcFirst(link.trim()) : null;
    this.anchorNotTrimmed = anchor;
    this.anchor = (anchor != null) ? anchor.trim() : null;
    this.textNotTrimmed = text;
    this.text = (text != null) ? text.trim() : null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[[");
    sb.append(linkNotTrimmed);
    if (anchorNotTrimmed != null) {
      sb.append('#');
      sb.append(anchorNotTrimmed);
    }
    if (textNotTrimmed != null) {
      sb.append('|');
      sb.append(textNotTrimmed);
    }
    sb.append("]]");
    return sb.toString();
  }
}