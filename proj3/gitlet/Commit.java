package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedHashMap;

/** Commit class for Gitlet.
 *  @author Rishit Gupta
 */
public class Commit implements Serializable {
    /** Fields. */

    /** Commit message. */
    private String _message;

    /** Metadata for when commit was created. */
    private String _time;

    /** SHA-1 ID for parent commit. */
    private String _parent;

    /** Branch pointer. */
    private String _branch;

    /** SHA-1 ID code for this commit. */
    private String _code;

    /** Hash set that stores blobs (file info). */
    private LinkedHashMap<String, String> _blobs;

    /** MESSAGE, PARENT, BLOBS. */
    public Commit(String message, String parent,
                  LinkedHashMap<String, String> blobs) {
        Formatter fmt = new Formatter();
        _message = message;
        _parent = parent;
        Date time = new Date(0);
        if (_parent != null) {
            long millis = System.currentTimeMillis();
            time = new Date(millis);
        }
        fmt.format("%ta %tb %td %tT %tY %tz", time, time, time, time, time,
                time);
        _time = fmt.toString();
        _blobs = blobs;
        byte[] b = Utils.serialize(this);
        _code = Utils.sha1(b);
    }

    /** Returns message. */
    public String getMessage() {
        return _message;
    }

    /** Returns time. */
    public String getTimestamp() {
        return _time;
    }

    /** Returns parent. */
    public String getParent() {
        return _parent;
    }

    /** Returns branch. */
    public String getBranch() {
        return _branch;
    }

    /** Returns code. */
    public String getCode() {
        return _code;
    }

    /** Returns blobs. */
    public LinkedHashMap<String, String> getBlobs() {
        return _blobs;
    }
}




