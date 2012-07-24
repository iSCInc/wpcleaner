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

package org.wikipediacleaner.api.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.data.Page;


/**
 * MediaWiki embedded in requests.
 */
public class ApiEmbeddedInRequest extends ApiListRequest {

  // ==========================================================================
  // API properties
  // ==========================================================================

  /**
   * Property for Filter redirection.
   */
  public final static String PROPERTY_FILTERREDIR = "eifilterredir";

  /**
   * Property value for Filter redirection / All.
   */
  public final static String PROPERTY_FILTERREDIR_ALL = "all";

  /**
   * Property value for Filter redirection / Non redirects.
   */
  public final static String PROPERTY_FILTERREDIR_NON_REDIRECTS = "nonredirects";

  /**
   * Property value for Filter redirection / Redirects.
   */
  public final static String PROPERTY_FILTERREDIR_REDIRECTS = "redirects";

  /**
   * Property for Limit.
   */
  public final static String PROPERTY_LIMIT = "eilimit";

  /**
   * Property for Name space.
   */
  public final static String PROPERTY_NAMESPACE = "einamespace";

  /**
   * Property for Title.
   */
  public final static String PROPERTY_TITLE = "eititle";

  // ==========================================================================
  // Request management
  // ==========================================================================

  private final ApiEmbeddedInResult result;

  /**
   * @param result Parser for result depending on chosen format.
   */
  public ApiEmbeddedInRequest(ApiEmbeddedInResult result) {
    this.result = result;
  }

  /**
   * Load list of pages embedding a page.
   * 
   * @param page Page for list of embedding pages is requested.
   * @param namespaces List of name spaces to restrict result.
   * @return List of pages embedding the page.
   */
  public List<Page> loadEmbeddedIn(
      Page page, List<Integer> namespaces) throws APIException {
    Map<String, String> properties = getProperties(ACTION_QUERY, result.getFormat());
    properties.put(
        PROPERTY_LIST,
        PROPERTY_LIST_EMBEDDEDIN);
    properties.put(PROPERTY_LIMIT, LIMIT_MAX);
    if ((namespaces != null) && (namespaces.size() > 0)) {
      properties.put(PROPERTY_NAMESPACE, constructList(namespaces));
    }
    properties.put(PROPERTY_TITLE, page.getTitle());
    List<Page> list = new ArrayList<Page>();
    while (result.executeEmbeddedIn(properties, list)) {
      //
    }
    Collections.sort(list);
    return list;
  }
}