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

package org.wikipediacleaner.gui.swing.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;

import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.InternalLinkNotification;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.data.PageContents;
import org.wikipediacleaner.api.data.PageElementInternalLink;
import org.wikipediacleaner.api.data.PageElementTemplate;
import org.wikipediacleaner.api.data.PageElementTitle;
import org.wikipediacleaner.api.data.PageUtilities;
import org.wikipediacleaner.api.data.TemplateMatcher;
import org.wikipediacleaner.gui.swing.action.FindTextAction;
import org.wikipediacleaner.gui.swing.action.ReplaceLinkAction;
import org.wikipediacleaner.gui.swing.basic.BasicWindow;


/**
 * A text component to colorize / edit MediaWiki text.
 */
public class MediaWikiPane
    extends JTextPane {

  /* Test for patterns
  public static void main(String[] args) {
    //Pattern pattern = Pattern.compile("\\{\\{(loupe(?:\\|((?:(?:[^\\{\\}]*)|(?:\\{\\{\\!\\}\\}))*))?)\\}\\}");
    Pattern pattern = Pattern.compile("\\{\\{(loupe(?:\\|((?:(?:[^\\{\\}])|(?:\\{\\{\\!\\}\\}))*))?)\\}\\}");
    Matcher matcher = pattern.matcher("{{loupe|c=U [[ABCDEFgénéral]] fut {{rom|II|2}}de .}} [[Lozère]]");
    System.out.println("Searching");
    long begin = System.currentTimeMillis();
    while (matcher.find()) {
      System.out.println("Found: " + matcher.start() + "," + matcher.end());
    }
    long end = System.currentTimeMillis();
    System.out.println("Done in " + (end - begin) + "ms");
  }*/

  private static final long serialVersionUID = 3225120886653438117L;

  public static final String PROPERTY_MODIFIED = "ModifiedProperty";

  private final EnumWikipedia wikipedia;
  private Page page;
  private final BasicWindow window;
  private List<Page> internalLinks;

  private static final KeyStroke lastLinkKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK);
  private static final KeyStroke lastReplaceKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK);

  private boolean isModified = false;
  boolean isInInternalModification = false;

  private transient MediaWikiPopupListener popupListener;

  private int undoLevels;
  private LinkedList<String> undoTexts;
  private LinkedList<String> redoTexts;
  private JButton undoButton;
  private transient ActionListener undoAction;
  private JButton redoButton;
  private transient ActionListener redoAction;

  public static KeyStroke getLastLinkKeyStroke() {
    return lastLinkKeyStroke;
  }

  public static KeyStroke getLastReplaceKeyStroke() {
    return lastReplaceKeyStroke;
  }

  /**
   * Construct a MediaWikiPane.
   * 
   * @param wikipedia Wikipedia.
   * @param page Page.
   * @param window Window containing the pane.
   */
  public MediaWikiPane(EnumWikipedia wikipedia, Page page, BasicWindow window) {
    super();
    this.wikipedia = wikipedia;
    this.page = page;
    this.window = window;
    this.undoLevels = 0;
    this.undoTexts = new LinkedList<String>();
    this.redoTexts = new LinkedList<String>();
    initialize();
  }

  /**
   * @param levels Number of undo levels.
   */
  public void setUndoLevels(int levels) {
    this.undoLevels = levels;
  }

  /**
   * Update status of Undo / Redo buttons 
   */
  private void updateUndoButtons() {
    if (undoButton != null) {
      undoButton.setEnabled(!undoTexts.isEmpty() && isModified);
    }
    if (redoButton != null) {
      redoButton.setEnabled(!redoTexts.isEmpty());
    }
  }

  /**
   * @param undo Undo button.
   */
  public void setUndoButton(JButton undo) {
    if ((undoButton != null) && (undoAction != null)) {
      undoButton.removeActionListener(undoAction);
    }
    undoButton = undo;
    if (undoButton != null) {
      undoAction = new ActionListener() {
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
          undo();
        }
      };
      undoButton.addActionListener(undoAction);
    }
    updateUndoButtons();
  }

  /**
   * Undo last change.
   */
  void undo() {
    if (undoTexts.isEmpty()) {
      return;
    }
    String newText = undoTexts.getLast();
    String oldText = getText();
    if (oldText.equals(newText)) {
      if (undoTexts.size() < 1) {
        return;
      }
      undoTexts.removeLast();
      newText = undoTexts.getLast();
    }
    undoTexts.removeLast();
    redoTexts.addLast(oldText);
    setTextModified(newText, false, false);
    updateUndoButtons();
  }

  /**
   * @param redo Redo button.
   */
  public void setRedoButton(JButton redo) {
    if ((redoButton != null) && (redoAction != null)) {
      redoButton.removeActionListener(redoAction);
    }
    redoButton = redo;
    if (redoButton != null) {
      redoAction = new ActionListener() {
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
          redo();
        }
      };
      redoButton.addActionListener(redoAction);
    }
    updateUndoButtons();
  }

  /**
   * Redo last change.
   */
  void redo() {
    if (redoTexts.isEmpty()) {
      return;
    }
    String newText = redoTexts.getLast();
    String oldText = getText();
    if (oldText.equals(newText)) {
      if (redoTexts.size() < 1) {
        return;
      }
      redoTexts.removeLast();
      newText = redoTexts.getLast();
    }
    redoTexts.removeLast();
    undoTexts.addLast(oldText);
    setTextModified(newText, false, false);
    updateUndoButtons();
  }

  /**
   * Memorize current text for undo / redo
   */
  private void validateCurrentText() {
    if (undoLevels <= 0) {
      return;
    }

    // Check if memorizing text is useful
    String currentText = getText();
    if (!undoTexts.isEmpty() && currentText.equals(undoTexts.getLast())) {
      return;
    }

    // Adding text
    undoTexts.addLast(currentText);
    while (undoTexts.size() > undoLevels) {
      undoTexts.removeFirst();
    }
    redoTexts.clear();
    updateUndoButtons();
  }

  /**
   * @param page Page.
   */
  public void setWikiPage(Page page) {
    this.page = page;
  }

  /**
   * @return page Page.
   */
  public Page getWikiPage() {
    return page;
  }

  /**
   * @return Flag indicating if the document has been modified.
   */
  public boolean isModified() {
    return isModified;
  }

  /**
   * @param modified New status of the document
   */
  public void setModified(boolean modified) {
    if (isModified != modified) {
      boolean oldValue = isModified;
      isModified = modified;
      updateUndoButtons();
      firePropertyChange(PROPERTY_MODIFIED, oldValue, isModified);
    }
  }

  /**
   * @param chk JCheckBox used to forcing adding a note in the Talk page.
   */
  public void setCheckBoxAddNote(JCheckBox chk) {
    if (popupListener != null) {
      popupListener.setCheckBoxAddNote(chk);
    }
  }

  /**
   * @param chk JCheckBox used for updating disambiguation warning in the Talk page.
   */
  public void setCheckBoxUpdateDabWarning(JCheckBox chk) {
    if (popupListener != null) {
      popupListener.setCheckBoxUpdateDabWarning(chk);
    }
  }

  /**
   * @param chk JCheckBox used for creating disambiguation warning in the Talk page.
   */
  public void setCheckBoxCreateDabWarning(JCheckBox chk) {
    if (popupListener != null) {
      popupListener.setCheckBoxCreateDabWarning(chk);
    }
  }

  /**
   * Initialize styles. 
   */
  private void initialize() {
    boolean oldState = isInInternalModification;
    isInInternalModification = true;
    this.setComponentOrientation(wikipedia.getComponentOrientation());
    DefaultStyledDocument doc = new DefaultStyledDocument();
    setStyledDocument(doc);
    doc.addDocumentListener(new DocumentListener() {

      /* (non-Javadoc)
       * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
       */
      public void changedUpdate(@SuppressWarnings("unused") DocumentEvent e) {
        changeDocument();
      }

      /* (non-Javadoc)
       * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
       */
      public void insertUpdate(@SuppressWarnings("unused") DocumentEvent e) {
        changeDocument();
      }

      /* (non-Javadoc)
       * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
       */
      public void removeUpdate(@SuppressWarnings("unused") DocumentEvent e) {
        changeDocument();
      }

      public void changeDocument() {
        if (!isModified() && !isInInternalModification) {
          setModified(true);
        }
      }
    });

    Style root = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    Style normalLink = addStyle(MediaWikiConstants.STYLE_NORMAL_LINK, root);
    StyleConstants.setBold(normalLink, true);
    StyleConstants.setForeground(normalLink, Color.BLUE);
    normalLink.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_NORMAL_LINK);

    Style normalTemplate = addStyle(MediaWikiConstants.STYLE_NORMAL_TEMPLATE, root);
    StyleConstants.setBold(normalTemplate, true);
    StyleConstants.setForeground(normalTemplate, Color.BLUE);
    normalTemplate.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_NORMAL_TEMPLATE);

    Style checkWikiError = addStyle(MediaWikiConstants.STYLE_CHECK_WIKI_ERROR, root);
    StyleConstants.setBold(checkWikiError, true);
    StyleConstants.setForeground(checkWikiError, Color.RED);
    checkWikiError.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_CHECK_WIKI_ERROR);

    Style checkWikiOk = addStyle(MediaWikiConstants.STYLE_CHECK_WIKI_OK, root);
    StyleConstants.setBold(checkWikiOk, true);
    StyleConstants.setForeground(checkWikiOk, Color.GREEN);
    checkWikiOk.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_CHECK_WIKI_OK);
    checkWikiOk.addAttribute(MediaWikiConstants.ATTRIBUTE_OCCURENCE, Boolean.FALSE);

    Style checkWikiWarning = addStyle(MediaWikiConstants.STYLE_CHECK_WIKI_WARNING, root);
    StyleConstants.setBold(checkWikiWarning, true);
    StyleConstants.setForeground(checkWikiWarning, Color.ORANGE);
    checkWikiWarning.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_CHECK_WIKI_WARNING);

    Style disambiguationLink = addStyle(MediaWikiConstants.STYLE_DISAMBIGUATION_LINK, root);
    StyleConstants.setBold(disambiguationLink, true);
    StyleConstants.setForeground(disambiguationLink, Color.RED);
    disambiguationLink.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_DISAMBIGUATION_LINK);

    Style disambiguationTemplate = addStyle(MediaWikiConstants.STYLE_DISAMBIGUATION_TEMPLATE, root);
    StyleConstants.setBold(disambiguationTemplate, true);
    StyleConstants.setForeground(disambiguationTemplate, Color.RED);
    disambiguationTemplate.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_DISAMBIGUATION_TEMPLATE);

    Style helpRequestedLink = addStyle(MediaWikiConstants.STYLE_HELP_REQUESTED_LINK, root);
    StyleConstants.setBold(helpRequestedLink, true);
    StyleConstants.setForeground(helpRequestedLink, Color.ORANGE);
    helpRequestedLink.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_HELP_REQUESTED_LINK);

    Style redirectLink = addStyle(MediaWikiConstants.STYLE_REDIRECT_LINK, root);
    StyleConstants.setBold(redirectLink, true);
    StyleConstants.setItalic(redirectLink, true);
    StyleConstants.setForeground(redirectLink, Color.CYAN);
    redirectLink.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_REDIRECT_LINK);

    Style missingLink = addStyle(MediaWikiConstants.STYLE_MISSING_LINK, root);
    StyleConstants.setBold(missingLink, true);
    StyleConstants.setForeground(missingLink, Color.ORANGE);
    StyleConstants.setStrikeThrough(missingLink, true);
    missingLink.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_MISSING_LINK);

    Style externalLink = addStyle(MediaWikiConstants.STYLE_EXTERNAL_LINK, root);
    StyleConstants.setForeground(externalLink, new Color(128, 128, 255));
    externalLink.addAttribute(MediaWikiConstants.ATTRIBUTE_TYPE, MediaWikiConstants.VALUE_EXTERNAL_LINK);

    ActionMap actionMap = getActionMap();
    InputMap inputMapFocused = getInputMap();
    InputMap inputMapInFocused = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    KeyStroke keyStroke = null;

    popupListener = new MediaWikiPopupListener(wikipedia, window);
    addKeyListener(popupListener);
    addMouseListener(popupListener);
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK);
    inputMapFocused.put(keyStroke, "find-text");
    actionMap.put("find-text", new FindTextAction());

    inputMapInFocused.put(lastLinkKeyStroke, "last-link");
    actionMap.put("last-link", new ReplaceLinkAction(false));

    inputMapInFocused.put(lastReplaceKeyStroke, "last-replace");
    actionMap.put("last-replace", new ReplaceLinkAction(true));

    isInInternalModification = oldState;
    setModified(false);
  }

  /* (non-Javadoc)
   * @see javax.swing.JEditorPane#setText(java.lang.String)
   */
  @Override
  public void setText(String t) {
    setTextModified(t, true, true);
  }

  /**
   * Enabling changing text without resetting the modified flag.
   * 
   * @param t New text.
   * @param resetModified Flag indicating if the modified flag should be reseted.
   */
  private void setTextModified(
      String t,
      boolean resetModified,
      boolean validate) {
    boolean oldState = isInInternalModification;
    isInInternalModification = true;
    super.setText(t);
    setCaretPosition(0);
    moveCaretPosition(0);
    resetAttributes();
    isInInternalModification = oldState;
    if (resetModified) {
      setModified(false);
      undoTexts.clear();
      redoTexts.clear();
    }
    if (validate) {
      validateCurrentText();
    }
  }

  /**
   * Replace all links.
   * 
   * @param from From.
   * @param to To.
   */
  public void replaceAllLinks(Page from, String to) {
    String text = getText();
    String fromText = from.getTitle();

    if (fromText.length() > 0) {
      StringBuilder expression = new StringBuilder();
      Pattern pattern = null;

      // Create the regular expression (simple form)
      expression.setLength(0);
      expression.append("\\[\\["); // [[
      expression.append("\\s*("); // Possible white characters
      PageUtilities.addPatternForTitle(expression, fromText);
      expression.append(")\\s*"); // Possible white characters
      expression.append("\\]\\]"); // ]]
      pattern = Pattern.compile(expression.toString());
      expression.setLength(0);
      expression.append("\\[\\["); // [[
      expression.append(to);
      expression.append("\\|$1\\]\\]");
      text = pattern.matcher(text).replaceAll(expression.toString());

      // Create the regular expression (complex form)
      expression.setLength(0);
      expression.append("\\[\\["); // [[
      expression.append("\\s*"); // Possible white characters
      PageUtilities.addPatternForTitle(expression, fromText);
      expression.append("\\s*"); // Possible white characters
      expression.append("\\|"); // Separator
      expression.append("([^\\|\\]]*)"); // Possible text
      expression.append("\\]\\]"); // ]]
      pattern = Pattern.compile(expression.toString());
      expression.setLength(0);
      expression.append("\\[\\["); // [[
      expression.append(to);
      expression.append("\\|$1\\]\\]");
      text = pattern.matcher(text).replaceAll(expression.toString());

      if (!text.equals(getText())) {
        setModified(true);
      }
      setTextModified(text, false, false);
    }
  }

  /**
   * Remove all links.
   * 
   * @param from From.
   */
  public void removeAllLinks(Page from) {
    String text = getText();
    String fromText = from.getTitle();

    if (fromText.length() > 0) {
      StringBuilder expression = new StringBuilder();
      Pattern pattern = null;

      // Create the regular expression (simple form)
      expression.setLength(0);
      expression.append("\\[\\["); // [[
      expression.append("\\s*("); // Possible white characters
      PageUtilities.addPatternForTitle(expression, fromText);
      expression.append(")\\s*"); // Possible white characters
      expression.append("\\]\\]"); // ]]
      pattern = Pattern.compile(expression.toString());
      text = pattern.matcher(text).replaceAll("$1");

      // Create the regular expression (complex form)
      expression.setLength(0);
      expression.append("\\[\\["); // [[
      expression.append("\\s*"); // Possible white characters
      PageUtilities.addPatternForTitle(expression, fromText);
      expression.append("\\s*"); // Possible white characters
      expression.append("\\|"); // Separator
      expression.append("([^\\|\\]]*)"); // Possible text
      expression.append("\\]\\]"); // ]]
      pattern = Pattern.compile(expression.toString());
      text = pattern.matcher(text).replaceAll("$1");

      if (!text.equals(getText())) {
        setModified(true);
      }
      setTextModified(text, false, false);
    }
  }

  /**
   * @return List of disambiguation links.
   */
  public List<Page> getInternalLinks() {
    return internalLinks;
  }

  /**
   * @param list List of disambiguation links.
   */
  public void setInternalLinks(List<Page> list) {
    internalLinks = list;
    resetAttributes();
  }

  /**
   * Select first occurence of text. 
   */
  public void selectFirstOccurence() {
    StyledDocument doc = getStyledDocument();
    int length = doc.getLength();
    int lastEnd = Integer.MAX_VALUE;
    for (int pos = 0; pos < length; pos = lastEnd) {
      Element run = doc.getCharacterElement(pos);
      lastEnd = run.getEndOffset();
      if (pos == lastEnd) {
        // offset + length beyond length of document, bail.
        break;
      }
      MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
      if ((attr != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_TYPE) != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_OCCURENCE) != Boolean.FALSE)) {
        select(run.getStartOffset(), run.getEndOffset());
        return;
      }
    }
    for (int pos = 0; pos < length; pos = lastEnd) {
      Element run = doc.getCharacterElement(pos);
      lastEnd = run.getEndOffset();
      if (pos == lastEnd) {
        // offset + length beyond length of document, bail.
        break;
      }
      MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
      if ((attr != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_TYPE) != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_OCCURENCE) != null)) {
        select(run.getStartOffset(), run.getEndOffset());
        return;
      }
    }
  }

  /**
   * Select previous occurence of text. 
   */
  public void selectPreviousOccurence() {
    StyledDocument doc = getStyledDocument();
    int lastStart = Integer.MIN_VALUE;
    for (int pos = getSelectionStart(); pos > 0; pos = lastStart) {
      Element run = doc.getCharacterElement(pos - 1);
      lastStart = run.getStartOffset();
      MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
      if ((attr != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_TYPE) != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_OCCURENCE) != Boolean.FALSE)) {
        select(run.getStartOffset(), run.getEndOffset());
        return;
      }
    }
    selectLastOccurence();
  }

  /**
   * Select next occurence of text. 
   */
  public void selectNextOccurence() {
    StyledDocument doc = getStyledDocument();
    int length = doc.getLength();
    int lastEnd = Integer.MAX_VALUE;
    for (int pos = getSelectionEnd() + 1; pos < length; pos = lastEnd) {
      Element run = doc.getCharacterElement(pos);
      lastEnd = run.getEndOffset();
      if (pos == lastEnd) {
        // offset + length beyond length of document, bail.
        break;
      }
      MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
      if ((attr != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_TYPE) != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_OCCURENCE) != Boolean.FALSE)) {
        select(run.getStartOffset(), run.getEndOffset());
        return;
      }
    }
    selectFirstOccurence();
  }

  /**
   * Select last occurence of text. 
   */
  public void selectLastOccurence() {
    StyledDocument doc = getStyledDocument();
    int lastStart = Integer.MIN_VALUE;
    for (int pos = doc.getLength(); pos > 0; pos = lastStart) {
      Element run = doc.getCharacterElement(pos - 1);
      lastStart = run.getStartOffset();
      MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
      if ((attr != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_TYPE) != null) &&
          (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_OCCURENCE) != Boolean.FALSE)) {
        select(run.getStartOffset(), run.getEndOffset());
        return;
      }
    }
  }

  /**
   * Notification of links found. 
   */
  class InternalLinkFound implements InternalLinkNotification {

    int startPosition = Integer.MAX_VALUE;
    int endPosition = Integer.MAX_VALUE;
    int secondStartPosition = Integer.MAX_VALUE;
    int secondEndPosition = Integer.MAX_VALUE;
    int thirdStartPosition = Integer.MAX_VALUE;
    int thirdEndPosition = Integer.MAX_VALUE;

    /**
     * Notification of a link found in an internal link.
     * 
     * @param link Link found.
     * @param internalLink Internal link in which the link is found.
     */
    public void linkFound(Page link, PageElementInternalLink internalLink) {
      int start = internalLink.getBeginIndex();
      int end = internalLink.getEndIndex();
      boolean disambiguation = Boolean.TRUE.equals(link.isDisambiguationPage());
      Style attr = getStyle(disambiguation ?
          MediaWikiConstants.STYLE_DISAMBIGUATION_LINK :
          link.isRedirect() ?
              MediaWikiConstants.STYLE_REDIRECT_LINK :
              link.isExisting() ?
                  MediaWikiConstants.STYLE_NORMAL_LINK :
                  MediaWikiConstants.STYLE_MISSING_LINK);
      attr = (Style) attr.copyAttributes();
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_PAGE, link);
      String text = internalLink.getDisplayedText();
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_TEXT, text);
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_UUID, UUID.randomUUID());
      StyledDocument doc = getStyledDocument();
      doc.setCharacterAttributes(start, end - start, attr, true);
      if (start < startPosition) {
        startPosition = start;
        endPosition = end;
      }
    }

    /**
     * Notification of a link found in a template.
     * 
     * @param link Link found.
     * @param template Template in which the link is found.
     * @param matcher Matcher used to find the link in the template.
     */
    public void linkFound(Page link, PageElementTemplate template,
        TemplateMatcher matcher) {
      int start = template.getBeginIndex();
      int end = template.getEndIndex();
      String styleName = null;
      if (matcher.isGood() || Boolean.FALSE.equals(link.isDisambiguationPage())) {
        styleName = MediaWikiConstants.STYLE_NORMAL_TEMPLATE;
      } else {
        if (matcher.isHelpNeeded()) {
          styleName = MediaWikiConstants.STYLE_HELP_REQUESTED_LINK;
        } else {
          styleName = MediaWikiConstants.STYLE_DISAMBIGUATION_TEMPLATE;
        }
      }
      Style attr = getStyle(styleName);
      attr = (Style) attr.copyAttributes();
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_PAGE, link);
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_PAGE_ELEMENT, template);
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_TEMPLATE_MATCHER, matcher);
      attr.addAttribute(MediaWikiConstants.ATTRIBUTE_UUID, UUID.randomUUID());
      if ((matcher.isHelpNeeded()) && (template.getParameterCount() > 0)) {
        attr.addAttribute(
            MediaWikiConstants.ATTRIBUTE_TEXT,
            (template.getParameterCount() > 1) ?
                template.getParameterValue(1) : template.getParameterValue(0));
      }
      StyledDocument doc = getStyledDocument();
      doc.setCharacterAttributes(start, end - start, attr, true);
      if (matcher.isGood() || !Boolean.FALSE.equals(link.isDisambiguationPage())) {
        if (start < thirdStartPosition) {
          thirdStartPosition = start;
          thirdEndPosition = end;
        }
      } else if (matcher.isHelpNeeded()) {
        if (start < secondStartPosition) {
          secondStartPosition = start;
          secondEndPosition = end;
        }
      } else {
        if (start < startPosition) {
          startPosition = start;
          endPosition = end;
        }
      }
    }
    
  }

  /**
   * Reset attributes of the document.
   * This method should be called after modifications are done.
   */
  public void resetAttributes() {

    boolean oldState = isInInternalModification;
    isInInternalModification = true;

    // First remove MediaWiki styles
    StyledDocument doc = getStyledDocument();
    int length = doc.getLength();
    int lastEnd = Integer.MAX_VALUE;
    for (int pos = 0; pos < length; pos = lastEnd) {
      Element run = doc.getCharacterElement(pos);
      lastEnd = run.getEndOffset();
      if (pos == lastEnd) {
        // offset + length beyond length of document, bail.
        break;
      }
      MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
      if ((attr != null) && (attr.getAttribute(MediaWikiConstants.ATTRIBUTE_TYPE) != null)) {
        doc.setCharacterAttributes(
            run.getStartOffset(),
            run.getEndOffset() - run.getStartOffset(),
            getStyle(StyleContext.DEFAULT_STYLE),
            true);
      }
    }

    // Look for links
    String contents = getText();
    InternalLinkFound notification = new InternalLinkFound();
    PageContents.findInternalLinks(
        wikipedia, page, contents, internalLinks, notification);

    // Move caret to force first element to be visible
    if (notification.startPosition < Integer.MAX_VALUE) {
      setCaretPosition(notification.startPosition);
      moveCaretPosition(notification.endPosition);
    } else if (notification.secondStartPosition < Integer.MAX_VALUE) {
      setCaretPosition(notification.secondStartPosition);
      moveCaretPosition(notification.secondEndPosition);
    } else if (notification.thirdStartPosition < Integer.MAX_VALUE) {
      setCaretPosition(notification.thirdStartPosition);
      moveCaretPosition(notification.thirdEndPosition);
    }

    isInInternalModification = oldState;

    if (!isInInternalModification) {
      validateCurrentText();
    }
  }

  /**
   * Retrieve current chapter hierarchy.
   * 
   * @param position Position in the text.
   * @return Chapters.
   */
  List<String> getChapterPosition(int position) {
    List<String> chapters = null;
    try {
      int currentLevel = Integer.MAX_VALUE;
      while ((position >= 0) && (currentLevel > 1)) {

        // Retrieving paragraph
        Element paragraph = getStyledDocument().getParagraphElement(position);
        int start = paragraph.getStartOffset();
        int end = paragraph.getEndOffset();
        position = start - 1;

        // Analyzing text
        String value = getText(start, end - start);
        boolean falseComment = false;
        while (((start = value.indexOf("<!--")) > 0) && !falseComment) {
          //start = value.indexOf("<!--");
          end = value.indexOf("-->", start + 4);
          if ((start != -1) && (end != -1)) {
            end += 3;
            if (end < value.length() - 1) {
              value = value.substring(0, start) + value.substring(end);
            } else {
              value = value.substring(0, start);
            }
          } else {
            falseComment = true;
          }
        }
        start = 0;
        end = value.length() - 1;
        while ((end > 0) && Character.isWhitespace(value.charAt(end))) {
          end--;
        }
        int level = 0;
        while ((start < end - 2) && (value.charAt(start) == '=') && (value.charAt(end) == '=')) {
          level++;
          start++;
          end--;
        }
        if ((level > 0) && (level < currentLevel)) {
          currentLevel = level;
          if (chapters == null) {
            chapters = new ArrayList<String>();
          }
          chapters.add(0, "" + (currentLevel - 1) + " - " + value.substring(start, end + 1).trim());
        }
      }
    } catch (BadLocationException e) {
      //
    }
    return chapters;
  }

  /**
   * @return Flag indicating if all the text can be displayed.
   */
  public boolean canDisplayAllText() {
    String text = getText();
    Font font = getFont();
    if ((text != null) && (font != null)) {
      return (font.canDisplayUpTo(text) == -1);
    }
    return true;
  }

  /**
   * @return List of fonts that can display all characters.
   */
  public List<Font> getPossibleFonts() {
    String text = getText();
    List<Font> possibleFonts = new ArrayList<Font>();
    Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    for (int i = 0; i < allFonts.length; i++) {
      if (allFonts[i].canDisplayUpTo(text) == -1) {
        possibleFonts.add(allFonts[i]);
      }
    }
    return possibleFonts;
  }

  // =========================================================================
  // Complex Pane management
  // =========================================================================

  private JSplitPane splitPane;
  private JTree treeToc;
  private TitleTreeNode rootToc;
  private boolean tocIsDisplayed;

  /**
   * A tree node for titles. 
   */
  private static class TitleTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private final PageElementTitle title;

    /**
     * @param title Title.
     */
    public TitleTreeNode(PageElementTitle title) {
      super((title != null) ? title : "Page");
      this.title = title;
    }

    /**
     * @return Title level.
     */
    public int getTitleLevel() {
      if (title != null) {
        return title.getFirstLevel();
      }
      return 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.DefaultMutableTreeNode#getAllowsChildren()
     */
    @Override
    public boolean getAllowsChildren() {
      return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.DefaultMutableTreeNode#toString()
     */
    @Override
    public String toString() {
      return (title != null) ? title.getTitle() : super.toString();
    }
  }

  /**
   * Construct a complex MediaWikiPane.
   * 
   * @param textPane Existing MediaWikiPane.
   * @return Complex component containing a MediaWikiPane.
   */
  public static JComponent createComplexPane(
      MediaWikiPane textPane) {
    if (textPane == null) {
      return null;
    }
    if (textPane.splitPane != null) {
      return textPane.splitPane;
    }

    // Text pane
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JScrollPane scrollContents = new JScrollPane(textPane);
    scrollContents.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    splitPane.setBottomComponent(scrollContents);

    // Table of contents
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.ipadx = 0;
    constraints.ipady = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;
    JPanel panelTOC = new JPanel(new GridBagLayout());
    TitleTreeNode rootToc = new TitleTreeNode(null);
    JTree treeToc = new JTree(rootToc);
    treeToc.setShowsRootHandles(true);
    constraints.fill = GridBagConstraints.BOTH;
    panelTOC.add(treeToc, constraints);
    constraints.gridx++;
    splitPane.setTopComponent(panelTOC);

    // Configure split pane
    splitPane.setDividerSize(0);
    splitPane.setDividerLocation(0);
    splitPane.setResizeWeight(0.0);
    textPane.splitPane = splitPane;
    textPane.treeToc = treeToc;
    textPane.rootToc = rootToc;

    return splitPane;
  }

  /**
   * Display or hide Table of Contents.
   */
  public void toggleToc() {
    if (tocIsDisplayed) {
      hideToc();
    } else {
      displayToc();
    }
  }

  /**
   * Display Table of Contents.
   */
  public void displayToc() {
    if (splitPane == null) {
      return;
    }
    if (!tocIsDisplayed) {
      rootToc.removeAllChildren();
      treeToc.setRootVisible(true);
      String contents = getText();
      int currentIndex = 0;
      TitleTreeNode lastNode = rootToc;
      while ((currentIndex < contents.length())) {
        PageElementTitle title = PageContents.findNextTitle(page, contents, currentIndex);
        if (title == null) {
          currentIndex = contents.length();
        } else {
          while ((lastNode != null) &&
                 (lastNode.getTitleLevel() >= title.getFirstLevel())) {
            lastNode = (TitleTreeNode) lastNode.getParent();
          }
          if (lastNode == null) {
            lastNode = rootToc;
          }
          TitleTreeNode tmpNode = new TitleTreeNode(title);
          lastNode.add(tmpNode);
          lastNode = tmpNode;
          currentIndex = title.getEndIndex();
        }
      }
      treeToc.expandRow(0);
      treeToc.setRootVisible(false);
      tocIsDisplayed = true;
    }
    splitPane.setDividerLocation(200);
    splitPane.setDividerSize(2);
  }

  /**
   * Hide Table of Contents.
   */
  public void hideToc() {
    if (splitPane == null) {
      return;
    }
    tocIsDisplayed = false;
    splitPane.setDividerLocation(0);
    splitPane.setDividerSize(0);
  }
}
