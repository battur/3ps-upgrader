package upgrader;

import static upgrader.Upgrader.getUserInputThenLog;
import static upgrader.Upgrader.plog;

import java.nio.file.Path;
import java.util.*;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * The initial state of the application. The whole purpose of this state is to locate libraries that need an
 * upgrade.
 * @author bsanchin
 */
public class SearchLibraryState
    extends AbstractApplicationState {

  protected Upgrader up;
  private String query;

  SearchLibraryState(Upgrader argUp) {
    this.up = argUp;
  }

  @Override
  public void process() {

    if (query == null) {
      plog("Enter a library you want to upgrade.");
      query = getUserInputThenLog().toLowerCase();
      if (query.isEmpty()) {
        process();
        return;
      }
    }

    // Look for jar files in libraries.dir whose names contain user query.
    TreeSet<Path> matchingFiles = walkTreeAndRetrieveFiles(up.getLibrariesDir(), ".jar", query);

    // We will be grouping the matching files based on their filename.
    // Assumption here is all jar files having the same name are identical.
    TreeMap<String, List<Path>> groups = new TreeMap<String, List<Path>>();

    for (Path jarfile : matchingFiles) {
      List<Path> list = groups.get(jarfile.getFileName().toString());
      if (list == null) {
        list = new ArrayList<Path>();
      }
      list.add(jarfile);
      groups.put(jarfile.getFileName().toString(), list);
    }

    int count = 0;

    // Prompt user to select from the options.
    if (groups.size() > 0) {
      plog("Select a group to proceed with upgrade.");
    }
    else {
      // No file is found, we need do another search.
      plog("No files found.");
      query = null;
      process();
      return;
    }

    // Show the groups of file to the screen.
    HashMap<Integer, String> options = new HashMap<Integer, String>();
    for (String filename : groups.keySet()) {
      options.put(count, filename);
      List<Path> list = groups.get(filename);
      plog("[" + count + "] " + list.get(0).toString());
      if (list.size() > 1) {
        for (int i = 1; i < list.size(); i++ ) {
          plog("    " + list.get(i).toString());
        }
      }
      plog();
      count++ ;
    }

    // Get the user selection.
    String userInput = getUserInputThenLog().trim();

    // If user input is a number and is within options, go locate the replacement
    if (NumberUtils.isNumber(userInput) && Integer.parseInt(userInput) >= 0
        && Integer.parseInt(userInput) <= count) {
      LocateReplacementState newState = new LocateReplacementState(up);
      newState.oldQuery = query;
      newState.oldFilename = options.get(new Integer(userInput));
      newState.oldFiles = groups.get(newState.oldFilename);

      plog("Selected: " + newState.oldFilename);

      up.currentState = newState;
      up.currentState.process();
    }
    // User wants to search for another library.
    else {
      query = userInput;
      process();
    }
  }

}
