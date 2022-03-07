package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/** Stage class for Gitlet.
 *  @author Rishit Gupta
 */
public class Stage implements Serializable {

    /** Fields. */

    /** Hash set of all staged files. */
    private LinkedHashMap<String, String> stagedFiles;

    /** ArrayList of all removed files. */
    private ArrayList<String> removed;

    /** Constructor class for Stage. */
    public Stage() {
        stagedFiles = new LinkedHashMap<>();
        removed = new ArrayList<>();
    }

    /** Add method for staged files that takes String FILE,
     * and a String SHA1. */
    public void add(String file, String sha1) {
        stagedFiles.put(file, sha1);
    }

    /** Add method for removed files that takes a String FILE. */
    public void removedAdd(String file) {
        removed.add(file);
    }

    /** Getter for staged files that returns a
     * HashMap<String, String> stagedFiles. */
    public HashMap<String, String> getStagedFiles() {
        return stagedFiles;
    }

    /** Getter for removed files that returns ArrayList<String> removed. */
    public ArrayList<String> getRemoved() {
        return removed;
    }

    /** Clear stage that returns void. */
    public void reset() {
        stagedFiles.clear();
        removed.clear();
    }


}
