package upgrader;

import static upgrader.Upgrader.getUserInputThenLog;
import static upgrader.Upgrader.plog;

import java.nio.file.Path;
import java.util.*;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * The application state where you locate the new library that is to replace old ones.
 * @author bsanchin
 */
public class LocateReplacementState
    extends AbstractApplicationState {

  protected Upgrader up;
  protected String oldQuery;
  protected String oldFilename;
  protected List<Path> oldFiles;
  protected Path newFile;

  public LocateReplacementState(Upgrader argUp) {
    this.up = argUp;
  }

  @Override
  public void process() {

    // Search for a library (*.jar) in new.library dir.
    TreeSet<Path> matchingFiles = walkTreeAndRetrieveFiles(up.getReplacementLibrariesDir(), ".jar", oldQuery);
    if (matchingFiles.size() > 0) {
      plog(
          "Select a replacement file to proceed with upgrade. Or simply search for other replacement library.");
    }
    else {
      plog("No files found.");
      plog("What do you want to do? (Retry|New Search)");
      String input = getUserInputThenLog().trim().toLowerCase();

      // The user wants to repeat the same search. Maybe he has placed a new jar file just a second ago.
      if (input.equals("r") || input.equals("retry")) {
        newFile = null;
        process();
      }
      // Let's start a brand new search.
      else {
        SearchLibraryState newState = new SearchLibraryState(up);
        up.currentState = newState;
        newState.process();
        return;
      }
    }

    int count = 0;
    HashMap<Integer, Path> options = new HashMap<Integer, Path>();

    // Show the possible candidates.
    for (Path jarfile : matchingFiles) {
      plog("[" + count + "] " + jarfile.toAbsolutePath().toString());
      options.put(count, jarfile);
      count++ ;
    }

    String userInput = getUserInputThenLog().trim();
    // User selected a library that would replace the old ones.
    if (NumberUtils.isNumber(userInput) && Integer.parseInt(userInput) >= 0
        && Integer.parseInt(userInput) <= count) {
      newFile = options.get(new Integer(userInput));
      plog("Selected: " + newFile.getFileName().toString());

      LocateClasspathEntriesState newState = new LocateClasspathEntriesState(up);
      newState.oldFilename = oldFilename;
      newState.oldFiles = oldFiles;
      newState.newFile = newFile;

      up.currentState = newState;
      up.currentState.process();
    }
    // The user wants to search for another candidate library.
    else {
      oldQuery = userInput;
      process();
    }
  }

}
