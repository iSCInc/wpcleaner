/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.check.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.wikipediacleaner.api.API;
import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.APIFactory;
import org.wikipediacleaner.api.check.CheckErrorResult;
import org.wikipediacleaner.api.check.CheckErrorResult.ErrorLevel;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.constants.WPCConfiguration;
import org.wikipediacleaner.api.data.DataManager;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.data.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementInternalLink;
import org.wikipediacleaner.i18n.GT;
import org.wikipediacleaner.utils.Configuration;
import org.wikipediacleaner.utils.ConfigurationValueInteger;


/**
 * Algorithm for analyzing error 526 of check wikipedia project.
 * Error 526: Incorrect link
 */
public class CheckErrorAlgorithm526 extends CheckErrorAlgorithmBase {

  public CheckErrorAlgorithm526() {
    super("Incorrect date link");
  }

  /** Minimum length of the year */
  private static final int MIN_LENGTH = 3;

  /** Maximum length of the year */
  private static final int MAX_LENGTH = 4;

  /**
   * Analyze a page to check if errors are present.
   * 
   * @param analysis Page analysis.
   * @param errors Errors found in the page.
   * @param onlyAutomatic True if analysis could be restricted to errors automatically fixed.
   * @return Flag indicating if the error was found.
   */
  @Override
  public boolean analyze(
      PageAnalysis analysis,
      Collection<CheckErrorResult> errors, boolean onlyAutomatic) {
    if ((analysis == null) || (analysis.getPage() == null)) {
      return false;
    }

    // Analyze each internal link
    List<PageElementInternalLink> links = analysis.getInternalLinks();
    if ((links == null) || links.isEmpty()) {
      return false;
    }
    boolean result = false;
    String contents = analysis.getContents();
    for (PageElementInternalLink link : links) {

      // Decide if link is an error
      String target = link.getFullLink();
      String text = link.getText();
      boolean isProblematic = false;
      if ((target != null) &&
          (text != null) &&
          !Page.areSameTitle(target, text)) {

        // Check text first (only digits)
        int yearDisplayed = -1;
        if ((text.length() >= MIN_LENGTH) &&
            (text.length() <= MAX_LENGTH)) {
          boolean onlyDigits = true;
          for (int pos = 0; pos < text.length(); pos++) {
            if (!Character.isDigit(text.charAt(pos))) {
              onlyDigits = false;
            }
          }
          if (onlyDigits) {
            yearDisplayed = Integer.valueOf(text);
          }
        }

        // Check link if needed
        if (yearDisplayed > 0) {
          int nbDigits = 0;
          while ((nbDigits < target.length()) &&
                 (Character.isDigit(target.charAt(nbDigits)))) {
            nbDigits++;
          }
          int yearLinked = -1;
          if ((nbDigits >= MIN_LENGTH) &&
              (nbDigits <= MAX_LENGTH)) {
            yearLinked = Integer.valueOf(target.substring(0, nbDigits));
          }
          if ((yearLinked > 0) && (yearLinked != yearDisplayed)) {
            if (target.length() == nbDigits) {
              isProblematic = true;
            } else {
              if (target.charAt(nbDigits) == ' ') {
                boolean incorrectCharacter = false;
                for (int pos = nbDigits + 1; pos < target.length(); pos++) {
                  if (Character.isDigit(target.charAt(pos))) {
                    incorrectCharacter = true;
                  }
                }
                if (!incorrectCharacter) {
                  isProblematic = true;
                }
              }
            }
          }
        }
      }

      // Report error
      if (isProblematic) {
        if (errors == null) {
          return true;
        }
        result = true;

        // Create error
        ErrorLevel errorLevel = ErrorLevel.ERROR;
        if ((link.getEndIndex() < contents.length()) &&
            (contents.charAt(link.getEndIndex()) == '{')) {
          errorLevel = ErrorLevel.WARNING;
        }
        CheckErrorResult errorResult = createCheckErrorResult(
            analysis, link.getBeginIndex(), link.getEndIndex(), errorLevel);
        errorResult.addReplacement(PageElementInternalLink.createInternalLink(target, target));
        errorResult.addReplacement(PageElementInternalLink.createInternalLink(text, text));
        String askHelp = getSpecificProperty("ask_help", true, true, false);
        if (askHelp != null) {
          List<String> askHelpList = WPCConfiguration.convertPropertyToStringList(askHelp, false);
          if (askHelpList != null) {
            boolean firstReplacement = true;
            for (String askHelpElement : askHelpList) {
              int pipeIndex = askHelpElement.indexOf('|');
              if ((pipeIndex > 0) && (pipeIndex < askHelpElement.length())) {
                String suffix = askHelpElement.substring(pipeIndex + 1);
                boolean botReplace = false;
                Page page = analysis.getPage();
                if (page.isArticle() && page.isInMainNamespace() &&
                    suffix.startsWith("{{") &&
                    (link.getEndIndex() < contents.length())) {
                  char nextChar = contents.charAt(link.getEndIndex());
                  if (nextChar != '{') {
                    if ((target != null) &&
                        (target.indexOf('#') < 0) &&
                        (target.indexOf('(') < 0) &&
                        (target.indexOf(')') < 0)) {
                      botReplace = true;
                    }
                  }
                }
                String replacement =
                    analysis.getContents().substring(link.getBeginIndex(), link.getEndIndex()) +
                    suffix;
                errorResult.addReplacement(
                    replacement,
                    askHelpElement.substring(0, pipeIndex),
                    false, firstReplacement && botReplace);
                firstReplacement = false;
              }
            }
          }
        }
        errors.add(errorResult);
      }
    }

    return result;
  }

  /**
   * @return True if the error has a special list of pages.
   */
  @Override
  public boolean hasSpecialList() {
    return (getAbuseFilter() != null) || (getDumpAnalysis() != null);
  }

  /**
   * @return Abuse filter.
   */
  private Integer getAbuseFilter() {
    String abuseFilter = getSpecificProperty("abuse_filter", true, true, false);
    if ((abuseFilter != null) &&
        (abuseFilter.trim().length() > 0)) {
      try {
        return Integer.valueOf(abuseFilter);
      } catch (NumberFormatException e) {
        // Nothing to do
      }
    }
    return null;
  }

  /**
   * @return Page name containing a dump analysis for this error.
   */
  private String getDumpAnalysis() {
    return getSpecificProperty("dump_analysis", true, true, false);
  }

  /**
   * Retrieve the list of pages in error.
   * 
   * @param wiki Wiki.
   * @param limit Maximum number of pages to retrieve.
   * @return List of pages in error.
   */
  @Override
  public List<Page> getSpecialList(EnumWikipedia wiki, int limit) {
    List<Page> result = new ArrayList<>();

    // Use abuse filter
    Integer abuseFilter = getAbuseFilter();
    if (abuseFilter != null) {
      API api = APIFactory.getAPI();
      Configuration config = Configuration.getConfiguration();
      int maxDays = config.getInt(wiki, ConfigurationValueInteger.MAX_DAYS_ABUSE_LOG);
      try {
        List<Page> tmpResult = api.retrieveAbuseLog(wiki, abuseFilter, maxDays);
        if (tmpResult != null) {
          result.addAll(tmpResult);
        }
      } catch (APIException e) {
        //
      }
    }

    // Use internal links
    String dumpAnalysis = getDumpAnalysis();
    if (dumpAnalysis != null) {
      API api = APIFactory.getAPI();
      Page page = DataManager.getPage(wiki, dumpAnalysis, null, null, null);
      try {
        api.retrieveLinks(wiki, page, null, null, false, false);
        if (page.getLinks() != null) {
          result.addAll(page.getLinks());
        }
      } catch (APIException e) {
        //
      }
    }

    Collections.sort(result);

    // Limit result size
    while (result.size() > limit) {
      result.remove(result.size() - 1);
    }

    return result;
  }

  /**
   * Return the parameters used to configure the algorithm.
   * 
   * @return Map of parameters (Name -> description).
   */
  @Override
  public Map<String, String> getParameters() {
    Map<String, String> parameters = super.getParameters();
    parameters.put(
        "abuse_filter",
        GT._("An identifier of an abuse filter that is triggered by incorrect year links."));
    parameters.put(
        "ask_help",
        GT._("Text added after the link to ask for help."));
    parameters.put(
        "dump_analysis",
        GT._("A page containing a dump analysis for this error."));
    return parameters;
  }

  /**
   * @param analysis Page analysis
   * @return Modified page content after bot fixing.
   * @see org.wikipediacleaner.api.check.algorithm.CheckErrorAlgorithmBase#internalBotFix(org.wikipediacleaner.api.data.PageAnalysis)
   */
  @Override
  protected String internalBotFix(PageAnalysis analysis) {
    return fixUsingAutomaticBotReplacement(analysis);
  }
}
