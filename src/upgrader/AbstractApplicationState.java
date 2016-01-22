package upgrader;

import static upgrader.Upgrader.log;

import java.io.IOException;
import java.nio.file.*;
import java.util.TreeSet;

/**
 * Base implementation for the application states. It has the implementation of walk the tree and get the
 * necessary files implementation.
 * @author bsanchin
 */
public abstract class AbstractApplicationState
    implements IState {

  /**
   * Visits given directory recursively and returns set of files that match given criterias.
   * @param rootDir root of the directory
   * @param fileExtension file extension that we would use for the search
   * @param filename portion of the filename we would use for the search
   * @return set of files that match the search criterias
   */
  protected TreeSet<Path> walkTreeAndRetrieveFiles(String rootDir, String fileExtension, String filename) {

    TreeSet<Path> matchingFiles = new TreeSet<Path>();
    try {
      Files.walkFileTree(Paths.get(rootDir), new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs)
            throws IOException {
          String currentFilename = file.getFileName().toString().toLowerCase();
          if (currentFilename.endsWith(fileExtension) && currentFilename.contains(filename)) {
            matchingFiles.add(file);
          }
          return FileVisitResult.CONTINUE;
        };
      });
    }
    // We are ok with dumping out some nasty exception.
    catch (IOException ex) {
      ex.printStackTrace();
      log(ex.getMessage());
      System.exit(1);
    }

    return matchingFiles;
  }

}
