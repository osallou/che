/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.machineperspective;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class MachineTerminal {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public MachineTerminal(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static String LINE_HIGHLIGHTED_GREEN = "rgba(6, 152, 154, 1)";

  private interface Locators {
    String TERMINAL_CONSOLE_CONTAINER_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal') and @active]/div[2]";
    String TERMINAL_DEFAULT_TAB_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal')]"
            + "/preceding::div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='Terminal']";
    String TERMINAL_TAB_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal')]"
            + "/preceding::div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[contains(text(),'Terminal%s')]";
    String TERMINAL_FOCUS_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal') and @active]"
            + "//div[contains(@class, 'terminal xterm xterm-theme-default focus')]";
  }

  @FindBy(xpath = Locators.TERMINAL_DEFAULT_TAB_XPATH)
  WebElement defaultTermTab;

  @FindBy(xpath = Locators.TERMINAL_CONSOLE_CONTAINER_XPATH)
  WebElement defaultTermContainer;

  /** wait default terminal tab */
  public void waitTerminalTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(defaultTermTab));
  }

  /** wait default terminal tab */
  public void waitTerminalTab(int termNumber) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(getTerminalTabXPath(termNumber))));
  }

  /** wait appearance the main terminal container */
  public void waitTerminalConsole() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(defaultTermContainer));
  }

  public boolean terminalIsPresent() {
    return seleniumWebDriver.findElements(By.xpath(Locators.TERMINAL_DEFAULT_TAB_XPATH)).size() > 0;
  }

  public void waitTerminalIsNotPresent(int termNumber) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(getTerminalTabXPath(termNumber))));
  }

  /**
   * wait appearance the main terminal container
   *
   * @param timeWait time of waiting terminal container in seconds
   */
  public void waitTerminalConsole(int timeWait) {
    new WebDriverWait(seleniumWebDriver, timeWait).until(visibilityOf(defaultTermContainer));
  }

  /** wait appearance the main terminal container */
  public String getVisibleTextFromTerminal() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(defaultTermContainer))
        .getText();
  }

  /**
   * wait text into the terminal
   *
   * @param expectedText expected text into terminal
   */
  public void waitExpectedTextIntoTerminal(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver input) -> getVisibleTextFromTerminal().contains(expectedText));
  }

  /**
   * wait expected text is not present in the terminal
   *
   * @param expectedText expected text
   */
  public void waitExpectedTextNotPresentTerminal(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> !(getVisibleTextFromTerminal().contains(expectedText)));
  }

  /**
   * wait text into the terminal
   *
   * @param expectedText expected text into terminal
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitExpectedTextIntoTerminal(String expectedText, int definedTimeout) {
    new WebDriverWait(seleniumWebDriver, definedTimeout)
        .until((WebDriver input) -> getVisibleTextFromTerminal().contains(expectedText));
  }

  public void waitTerminalIsNotEmpty() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver input) -> getVisibleTextFromTerminal().length() > 0);
  }

  public void selectFocusToActiveTerminal() {
    waitTerminalConsole();

    if (seleniumWebDriver.findElements(By.xpath(Locators.TERMINAL_FOCUS_XPATH)).size() == 0) {
      defaultTermContainer.click();
    }
  }

  /**
   * send user information into active terminal console
   *
   * @param command the user info.
   */
  public void typeIntoTerminal(String command) {
    selectFocusToActiveTerminal();
    defaultTermContainer.findElement(By.tagName("textarea")).sendKeys(command);
    loader.waitOnClosed();
  }

  /** select default terminal tab */
  public void selectTerminalTab() {
    waitTerminalTab();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(defaultTermTab))
        .click();
  }

  /**
   * scroll terminal by pressing key 'End'
   *
   * @param item is the name of the highlighted item
   */
  public void moveDownListTerminal(String item) {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.END.toString()).perform();
    WaitUtils.sleepQuietly(2);

    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(element));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver input) ->
                element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN));
  }

  /**
   * scroll terminal by pressing key 'Home'
   *
   * @param item is the name of the highlighted item
   */
  public void moveUpListTerminal(String item) {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.HOME.toString()).perform();
    WaitUtils.sleepQuietly(2);
    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(element));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver input) ->
                element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN));
  }

  private String getTerminalTabXPath(int terminalNumber) {
    return terminalNumber == 1
        ? format(Locators.TERMINAL_TAB_XPATH, "")
        : format(Locators.TERMINAL_TAB_XPATH, "-" + terminalNumber);
  }
}
