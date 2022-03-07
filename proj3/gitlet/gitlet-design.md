# Gitlet Design Document

**Name**: Rishit Gupta

## Classes and Data Structures

### Main:
This class has a main method that runs all the Gitlet commands including init, commit, add, etc.

### Stage:
This class reports the files or file changes that are not yet present in the .gitlet folder and are ready to be committed.

#### Fields:
HashSet <Files>: This field is a HashSet that creates a collection of cryptographic SHA-1 IDs and serialized versions of the file/director that we are trying to add to .gitlet.

### Commit
This class holds and sets information about versions of a file or directory. These versions can more intuitively be known as “snapshots” of these files and directories.

#### Fields:
1. String code: This field holds the cryptographic SHA-1 ID that refers to the commit.
2. String log: This field holds the log message at the commit (one of the components of the metadata).
3. String parentRef: This string holds the SHA-1 ID for parent commit in the tree.
4. Timestamp commitTime: This variable holds the metadata for the date and time that the commit was made.

### Gitlet:
This class manages the commit history and holds existing commits and pointers.

#### Fields
1. Map<String, Commit> commitMap: This field holds a mapping between all of the commits and the SHA-1 cryptographic keys.
2. String headPoint: This string holds the pointer to the current active commit, not necessarily the most recent change.

## Algorithms

### Main
public static void main (String[] args): This is the only method in Main that holds all calls to gitlet commands. This is where the commands are run from the command line.

### Stage
1. Public innit(): This method is the class constructor for Stage and Commit any time that a new file or directory is created.
2. Public static void add(File): This method adds the file to the stage, which reports that the file has changed, and needs to be committed to be added to the commit tree.

### Commit
1. Public String getMessage(): This method gets the metadata for the commit log message
2. Public void setMessage(String message): This method sets the metadata for the commit log message
3. Public String getId(): This method retrieves the cryptographic SHA-1 ID for the commit instance
4. Public void setId(String id): This method sets the cryptographic SHA-1 ID the commit instance
5. Public String getParentRef(): This method gets the cryptographic SHA-1 ID reference to the parent of the current commit instant
6. Public void setParentRef(String ref): This method sets the cryptographic SHA-1 ID reference to the parent of the current commit instant
7. Public Timestamp getCommitTime(): This method gets the timestamp to the date and time that the commit was created.
8. Public void setCommitTime(Timestamp t): This method sets the timestamp to the date and time that the commit was created.

## Persistence
In order to keep persistence while we run multiple commands in the terminal, we will need to run the current stage of the files to the disk. To do this,

1. Write the Commits class to the disk. We can serialize the current state of the files into bytes and write them to a file on disk. This can be done with the methods provided in Utils.java.


