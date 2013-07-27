/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
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

package org.wikipediacleaner.api.request.xml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.DataManager;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.request.ApiAbuseLogResult;
import org.wikipediacleaner.api.request.ApiRequest;


/**
 * MediaWiki API XML abuse log results.
 */
public class ApiXmlAbuseLogResult extends ApiXmlResult implements ApiAbuseLogResult {

  /**
   * @param wiki Wiki on which requests are made.
   * @param httpClient HTTP client for making requests.
   */
  public ApiXmlAbuseLogResult(
      EnumWikipedia wiki,
      HttpClient httpClient) {
    super(wiki, httpClient);
  }

  /**
   * Execute abuse log request.
   * 
   * @param properties Properties defining request.
   * @param list List to be filled with abuse logs.
   * @return True if request should be continued.
   * @throws APIException
   */
  public boolean executeAbuseLog(
      Map<String, String> properties,
      List<Page> list) throws APIException {
    try {
      Element root = getRoot(properties, ApiRequest.MAX_ATTEMPTS);

      // Retrieve category members
      XPath xpa = XPath.newInstance("/api/query/abuselog/item");
      List results = xpa.selectNodes(root);
      Iterator iter = results.iterator();
      while (iter.hasNext()) {
        Element currentNode = (Element) iter.next();
        String title = currentNode.getAttributeValue("title");
        Page page = DataManager.getPage(getWiki(), title, null, null, null);
        list.add(page);
      }

      // Retrieve continue
      return false; // Not continuing
      /*return shouldContinue(
          root, "/api/query-continue/abuselog",
          properties);*/
    } catch (JDOMException e) {
      log.error("Error loading abuse filters list", e);
      throw new APIException("Error parsing XML", e);
    }
  }
}