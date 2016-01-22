package upgrader;

import static upgrader.Upgrader.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * The application state it searches for references to old libraries and shows confirmation for the
 * upgrade/changes that would take.
 * @author bsanchin
 */
public class LocateClasspathEntriesState
    extends AbstractApplicationState {

  static final String CLASSPATH = ".classpath";
  static final String LINE_SEPARATOR = System.getProperty("line.separator");
  protected String oldFilename;
  protected List<Path> oldFiles;
  protected Path newFile;
  protected Upgrader up;

  LocateClasspathEntriesState(Upgrader argUp) {
    this.up = argUp;
  }

  @Override
  public void process() {

    // Search for references in all .classpath files in the library.dir.
    TreeSet<Path> matchingFiles = walkTreeAndRetrieveFiles(up.getLibrariesDir(), CLASSPATH, CLASSPATH);

    // We need to store information on where the occurences are.
    TreeMap<Path, List<String>> occurences = new TreeMap<Path, List<String>>();

    for (Path cpFile : matchingFiles) {
      byte[] bytes = new byte[] {};
      try {
        bytes = Files.readAllBytes(cpFile);
      }
      catch (IOException ex) {
        ex.printStackTrace();
        log(ex.getMessage());
        System.exit(1);
      }
      String fileContent = new String(bytes);
      // If the file has reference to the libary, we want to know on which line it is.
      if (fileContent.contains(oldFilename)) {
        String[] lines = fileContent.split(LINE_SEPARATOR);
        int lineNo = 1;
        for (String line : lines) {
          if (line.contains(oldFilename)) {
            List<String> list = occurences.get(cpFile);
            if (list == null) {
              list = new ArrayList<String>();
            }
            list.add(lineNo + ": " + line);
            occurences.put(cpFile, list);
          }
          lineNo++ ;
        }
      }
    }

    // Ask for a confirmation.
    plog();
    plog("Files to be replaced (" + oldFilename + "):");
    for (Path f : oldFiles) {
      plog("  " + f);
    }

    plog();
    plog("Replacement file: ");
    plog("  " + newFile);

    if (occurences.size() > 0) {
      plog();
      plog("Classpath entries will be affected");
      for (Path cp : occurences.keySet()) {
        for (String occ : occurences.get(cp)) {
          plog("  " + cp + ":" + occ);
        }
      }
    }

    plog();
    plog("Shall we proceed with the replacement task? (Yes|No)");
    String userInput = getUserInputThenLog();

    // User approves the upgrade plan, so let's do the upgrade/replacement.
    if (userInput.equals("yes") || userInput.equals("y")) {
      MakeReplacementState newState = new MakeReplacementState(up);
      newState.oldFilename = oldFilename;
      newState.oldFiles = oldFiles;
      newState.newFile = newFile;
      newState.occurences = occurences;
      up.currentState = newState;
      up.currentState.process();
    }
    // User did not approve, therefore, let's start a brand new search.
    else {
      SearchLibraryState newState = new SearchLibraryState(up);
      up.currentState = newState;
      up.currentState.process();
    }

  }

}
