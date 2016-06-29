/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.constants.wiki;

import java.awt.ComponentOrientation;


/**
 * Configuration for <a href="http://www.wikiskripta.eu/index.php/Home">WikiSkripta</a>.
 */
public class WikiSkripta
  extends AbstractWikiSettings {

  /**
   * Default constructor.
   */
  public WikiSkripta() {
    super(
        "cs", "WikiSkripta",
        new String[] {
            "www.wikiskripta.eu",
        },
        "/api.php", "/index.php",
        "wikiskripta", null,
        ComponentOrientation.LEFT_TO_RIGHT);
  }

  /**
   * @return True if connection can be secured.
   */
  @Override
  protected boolean canBeSecured() {
    return false;
  }
}
