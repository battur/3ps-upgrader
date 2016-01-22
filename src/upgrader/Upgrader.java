package upgrader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * A small cli tool that makes 3rd party libraries upgrade easier, faster, and auditable.<br>
 * You may find this tool helpful if you have 50+ libraries for an upgrade and each might affect 10+ projects.
 * <p>
 * The upgrader application runs in either of following 4 states at a time:
 * <p>
 * <ol>
 * <li>{@link upgrader.SearchLibraryState}
 * <li>{@link upgrader.LocateReplacementState}
 * <li>{@link upgrader.LocateClasspathEntriesState}
 * <li>{@link upgrader.MakeReplacementState}
 * </ol>
 * <p>
 * 
 * The normal application flow (state transition) could be:
 * <p>
 * <ol>
 * <li>User searches for a library (*.jar file) in old.libraries.dir. The application displays options based
 * on the search. User selects the library to be upgraded. <i>(If the query did not return any result, the
 * user can change it and search again.)</i>
 * <li>User searches for a newer version of the library in new.libraries.dir. The application displays options
 * based on the search. User selects the correct library (new version) within the options. <i>(If the query
 * did not return any result, the user can change it and search again.)</i>
 * <li>The applications retrieves .classpath files in libraries.dir where they have references to the old
 * library. The application will display a confirmation page with
 * <ul>
 * <li>the old libraries to be replaced (removed).
 * <li>the new library to be placed in place of the old ones.
 * <li>.classpath files (including lines) to be updated with a reference to new library.
 * </ul>
 * If the user declines to confirm, then the application goes to the very initial state--searching of old
 * libraries.
 * <li>If user confirms, then the application proceeds with the upgrade/replacement task.
 * </ol>
 * <p>
 * If you have 100+ libraries to upgrade for 100+ projects, this tool should come handy. Some of the libraries
 * may not be API compatible, thus the projects may fail to build after an upgrade. Sometimes, a library
 * upgrade may not be desirable if it needs a lot of work (e.g,. code changes, testing) or has serious risks.
 * In this case, you can revert the upgrade by doing the opposite of what you would do for an upgrade:
 * <ul>
 * <li>Take copies of old libraries and place them under new.libraries.dir.
 * <li>Search for the new libraries.
 * <li>After selection, search for old libraries.
 * <li>Then replace.
 * <li>Rinse & repeat.
 * </ol>
 * </ul>
 * <p>
 * A nice addition with this small tool is, what you would see and type on the cli is what is written to the
 * upgrader.log file. Hence, the upgrade is auditable :)
 * 
 * @author bsanchin
 */
public class Upgrader {

  protected static Logger logger;
  protected static Scanner scanner = new Scanner(System.in);

  /**
   * Scans user input from the console, logs the input, then returns the input.
   * @return user input
   */
  public static String getUserInputThenLog() {
    String input = scanner.next();
    logger.info("User entered: " + input);
    return input;
  }

  /**
   * Logs message to a log file.
   * @param argMessage message to be logged
   */
  public static void log(String argMessage) {
    logger.info(argMessage);
  }

  public static void main(String[] args) {
    System.setProperty("log4j.configuration", "resources/log4j.properties");
    Upgrader driver = new Upgrader();
    driver.drive(args);
  }

  /**
   * Prints and logs newline.
   */
  public static void plog() {
    System.out.println();
    logger.info("");
  }

  /**
   * Prints message to console output then logs it to a log file.
   * @param argMessage message to be printed and logged
   */
  public static void plog(String argMessage) {
    System.out.println(argMessage);
    logger.info(argMessage);
  }

  private Properties prop = new Properties();
  protected IState currentState;

  /**
   * Drives the upgrader.
   * @param argArgs the program arguments
   */
  public void drive(String[] argArgs) {

    logger = Logger.getLogger(Upgrader.class.getName());
    logger.info("============= Upgrader Start ==============");
    try (InputStream in = new FileInputStream("upgrader.properties")) {
      prop.load(in);

      if (getLibrariesDir() == null || !Files.exists(Paths.get(getLibrariesDir()))) {
        throw new RuntimeException("upgrader.properties should have an valid entry of 'old.libraries.dir'");
      }

      if (getReplacementLibrariesDir() == null || !Files.exists(Paths.get(getReplacementLibrariesDir()))) {
        throw new RuntimeException("upgrader.properties should have an valid entry of 'new.libraries.dir'");
      }

    }
    catch (Exception ex) {
      plog("Failed to load upgrader.properties file.");
      System.exit(1);
    }
    currentState = new SearchLibraryState(this);
    currentState.process();

  }

  /**
   * Returns the directory where the old libraries are stored in. The path this directory can be configured in
   * the upgrader.properties file.
   * @return the libraries directory
   */
  public String getLibrariesDir() {
    return prop.getProperty("old.libraries.dir");
  }

  /**
   * Returns the directory where the new libraries are stored in. The path this directory can be configured in
   * the upgrader.properties file.
   * @return the new libraries directory
   */
  public String getReplacementLibrariesDir() {
    return prop.getProperty("new.libraries.dir");
  }

}
