package upgrader;

/**
 * The upgrader application runs in either of following 4 states at a time:
 * <p>
 * <ol>
 * <li>{@link upgrader.SearchLibraryState}
 * <li>{@link upgrader.LocateReplacementState}
 * <li>{@link upgrader.LocateClasspathEntriesState}
 * <li>{@link upgrader.MakeReplacementState}
 * </ol>
 * <p>
 * All of above states implement IState interface.
 * 
 * @author bsanchin
 */
public interface IState {

  /**
   * Proceeds with state specific implementation.
   */
  public void process();
}
