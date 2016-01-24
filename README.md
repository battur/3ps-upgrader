# 3ps-upgrader
A interactive cli tool that makes 3rd party libraries upgrade easier, faster, and auditable. You may find this tool helpful if you have 50+ libraries for an upgrade and each might affect 10+ java projects.


The upgrader application runs in either of following 4 states at a time:

1. SearchLibraryState
2. LocateReplacementState
3. LocateClasspathEntriesState
4. MakeReplacementState 

The normal application flow (state transition) could be:

1. User searches for a library (*.jar file) in old.libraries.dir. The application displays options based on the search. User selects the library to be upgraded. (If the query did not return any result, the user can change it and search again.)
2. User searches for a newer version of the library in new.libraries.dir. The application displays options based on the search. User selects the correct library (new version) within the options. (If the query did not return any result, the user can change it and search again.)
3. The applications retrieves .classpath files in libraries.dir where they have references to the old library. The application will display a confirmation page with
  * the old libraries to be replaced (removed).
  * the new library to be placed in place of the old ones.
  * .classpath files (including lines) to be updated with a reference to new library. 
If the user declines to confirm, then the application goes to the very initial state--searching of old libraries.
4. If the user confirms, then the application executes with the upgrade/replacement plan. 

If you have 100+ libraries to upgrade for 100+ projects, this tool should come handy. Some of the libraries may not be API compatible, thus the projects may fail to build after an upgrade. Sometimes, a library upgrade is not desirable if it needs a lot of work (e.g,. code changes, testing) or has high risks associated. In this case, you can revert the upgrade by doing the opposite of what you would do for an upgrade:

* Take copies of old libraries and place them under new.libraries.dir.
* Search for the new libraries.
* After selection, search for old libraries.
* Then replace.
* Rinse & repeat. 

A nice addition with this small tool is, what you would see and type on the cli is what is written to the upgrader.log file. Hence, the upgrade is auditable :)

This application uses open source libraries [commons-lang3-3.4](http://commons.apache.org/proper/commons-lang/) and [log4j-1.2.17](https://logging.apache.org/log4j/1.2/).

Author: Battur Sanchin (battursanchin@gmail.com)

License: Apache 2.0
