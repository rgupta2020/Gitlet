package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;

/** Holds all methods for Gitlet.
 *  @author Rishit Gupta
 */
public class Gitlet {

    /** Fields. */

    /** Keeps track of current head pointer. */
    private String headPointer = "master";

    /** Instance variable for staging area. */
    private Stage stage;

    /** Instance variable that holds current working directory. */
    private File currDir;

    /** Gitlet constructor. */
    public Gitlet() {
        currDir = new File(System.getProperty("user.dir"));

        File f = new File(".gitlet/branches/headPointer.txt");
        if (f.exists()) {
            headPointer = Utils.readContentsAsString(f);
        }

        File g = new File(".gitlet/staging/stage.txt");
        if (g.exists()) {
            stage = Utils.readObject(g, Stage.class);
        }
    }

    /** Init method. */
    public void init() throws IOException {

        File gitlet = new File(".gitlet");
        File blobs = new File(".gitlet/blobs");
        File branches = new File(".gitlet/branches");
        File commits = new File(".gitlet/commits");
        File staging = new File(".gitlet/staging");
        File globalLog = new File(".gitlet/global-log");
        File untracked = new File(".gitlet/untracked");

        if (gitlet.exists()) {
            System.out.println("A Gitlet version-control system "
                   + "already exists in the current directory.");
        } else {
            gitlet.mkdir();
            blobs.mkdir();
            branches.mkdir();
            commits.mkdir();
            staging.mkdir();
            globalLog.mkdir();
            untracked.mkdir();

            LinkedHashMap<String, String> initBlob = new LinkedHashMap<>();
            Commit initComm = new Commit("initial commit",
                    null, initBlob);

            String commitPath = ".gitlet/commits/"
                    + initComm.getCode()
                    + ".txt";
            Utils.writeObject(new File(commitPath), initComm);

            Utils.writeContents(new File(".gitlet/branches"
                   + "/headPointer.txt"), "master");

            Utils.writeContents(new File(".gitlet/branches"
                   + "/master.txt"), initComm.getCode());

            stage = new Stage();
            Utils.writeObject(new File(
                    ".gitlet/staging/stage.txt"), stage);
        }

    }

    /** Add method that takes FILE. */
    public void add(String file) throws IOException {

        File f = new File(file);
        File c = new File(".gitlet/branches/" + headPointer + ".txt");

        String code = Utils.readContentsAsString(c);
        Commit currComm = Utils.readObject(new File(".gitlet/commits/"
                + code + ".txt"), Commit.class);

        File g = new File(".gitlet/staging/stage.txt");
        if (!g.exists()) {
            g.createNewFile();
        }

        if (!f.exists()) {
            System.out.println("File does not exist.");
        } else {
            if (currComm.getBlobs().get(file) != null && currComm.getBlobs().get
                    (file).equals(Utils.sha1(Utils.readContents(f)))) {
                if (stage.getRemoved().contains(file)) {
                    stage.getRemoved().remove(file);
                    Utils.writeObject(g, stage);
                }
                return;
            } else if (stage.getRemoved().contains(file)) {
                stage.getRemoved().remove(file);
            }

            String blobCode = Utils.sha1(Utils.readContents(f));
            File blob = new File(".gitlet/blobs/" + blobCode + ".txt");
            Utils.writeContents(blob, Utils.readContents(f));
            stage.add(file, blobCode);
            Utils.writeObject(g, stage);
        }

    }

    /** Commit method that takes MESSAGE. */
    public void commit(String message) throws IOException {
        if (message.length() <= 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        if (stage.getRemoved().isEmpty() && stage.getStagedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        File c = new File(".gitlet/branches/" + headPointer + ".txt");
        String code = Utils.readContentsAsString(c);
        Commit currComm = Utils.readObject(new File(".gitlet/commits/"
                + code + ".txt"), Commit.class);

        LinkedHashMap<String, String> temp = new LinkedHashMap<>();
        temp.putAll(currComm.getBlobs());
        LinkedHashMap<String, String> addFiles = new
                LinkedHashMap<>(stage.getStagedFiles());
        Set<String> addingFileKeys = addFiles.keySet();
        Object[] addingFiles = addingFileKeys.toArray();

        for (int i = 0; i < addFiles.size(); i++) {
            temp.put((String) addingFiles[i], stage.getStagedFiles()
                    .get(addingFiles[i]));
        }

        for (int i = 0; i < stage.getRemoved().size(); i++) {
            temp.remove(stage.getRemoved().get(i));
        }

        Commit newComm = new Commit(message, currComm.getCode(), temp);
        Utils.writeContents(new File(".gitlet/branches/"
                + headPointer + ".txt"), newComm.getCode());
        File t = new File(".gitlet/commits/"
                + newComm.getCode() + ".txt");
        t.createNewFile();
        Utils.writeObject(t, newComm);
        stage.reset();
        Utils.writeObject(new File(".gitlet/staging/stage.txt"), stage);
    }

    /** Log method. */
    public void log() {

        File c = new File(".gitlet/branches/" + headPointer + ".txt");
        String code = Utils.readContentsAsString(c);
        Commit currComm = Utils.readObject(new File(".gitlet/commits/"
                + code + ".txt"), Commit.class);

        while (currComm.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + currComm.getCode());
            System.out.println("Date: " + currComm.getTimestamp());
            System.out.println(currComm.getMessage());
            System.out.println();
            currComm = Utils.readObject(new File(".gitlet/commits/"
                    + currComm.getParent() + ".txt"), Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + currComm.getCode());
        System.out.println("Date: " + currComm.getTimestamp());
        System.out.println(currComm.getMessage());
        System.out.println();
    }

    /** Global log method. */
    public void globalLog() throws IOException {
        File[] commits = new File(".gitlet/commits").listFiles();
        for (File commit : commits) {
            Commit commObj = Utils.readObject(commit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commObj.getCode());
            System.out.println("Date: " + commObj.getTimestamp());
            System.out.println(commObj.getMessage());
            System.out.println();
        }
    }

    /** Branch checkout method that takes BNAME. */
    public void checkout1(String bName) {
        File branch = new File(".gitlet/branches/" + bName + ".txt");
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String commCode = Utils.readContentsAsString(branch);
        File head = new File(".gitlet/branches/" + headPointer + ".txt");
        File commFile = new File(".gitlet/commits/"
                + commCode + ".txt");
        Commit comm = Utils.readObject(commFile, Commit.class);

        List<File> currDirFiles = Arrays.asList(currDir.listFiles());
        ArrayList<File> txtFiles = new ArrayList<>();
        for (int i = 0; i < currDirFiles.size(); i++) {
            if (currDirFiles.get(i).getName().endsWith(".txt")) {
                txtFiles.add(currDirFiles.get(i));
            }
        }
        Commit checkout = comm;
        String code = Utils.readContentsAsString(head);
        File currCommFile = new File(".gitlet/commits/" + code + ".txt");
        Commit currCommit = Utils.readObject(currCommFile, Commit.class);

        for (int i = 0; i < txtFiles.size(); i++) {
            boolean hasName = currCommit.getBlobs().
                    containsKey(txtFiles.get(i).getName());
            if (!hasName && checkout.getBlobs().
                    containsKey(txtFiles.get(i).getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        ArrayList<String> files = new ArrayList<>();
        for (File f : txtFiles) {
            if (!checkout.getBlobs().containsKey(f.getName())
                    && currCommit.getBlobs().containsKey(f.getName())) {
                Utils.restrictedDelete(f);
            }
        }
        for (String f: checkout.getBlobs().keySet()) {
            String blobCode = checkout.getBlobs().get(f);
            File newFile = new File(currDir.getPath()
                    + "/.gitlet/blobs/" + blobCode + ".txt");
            File f1 = new File(f);
            Utils.writeContents(f1, Utils.readContents(newFile));
        }
        stage.reset();
        File stageFile = new File(currDir.getPath()
                + "/.gitlet/staging/stage.txt");
        Utils.writeObject(stageFile, stage);
        File headPoint = new File(".gitlet/branches/headPointer.txt");
        Utils.writeContents(headPoint, bName);
    }

    /** Checkout with file name that takes in DASH and FILENAME. */
    public void checkout2(String dash, String fileName) {

        if (!dash.equals("--")) {
            System.out.println("Invalid operands.");
            System.exit(0);
        }

        File c = new File(".gitlet/branches/" + headPointer + ".txt");
        String code = Utils.readContentsAsString(c);
        File commit = new File(".gitlet/commits/" + code + ".txt");
        Commit currComm = Utils.readObject(commit, Commit.class);
        File checkoutFile = new File(currDir.getPath() + fileName);

        if (!currComm.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        if (checkoutFile.exists()) {
            Utils.restrictedDelete(currDir.getPath() + fileName);
        }
        File currBlob = new File(currDir.getPath() + "/.gitlet/blobs/"
                + currComm.getBlobs().get(fileName) + ".txt");
        Utils.writeContents(new File(currDir.getPath(), fileName),
                Utils.readContents(currBlob));
    }

    /** Checkout that takes in COMMITCODE, DASH, FILEREF. */
    public void checkout3(String commitCode, String dash, String fileRef) {

        if (!dash.equals("--")) {
            System.out.println("Incorrect operands");
            System.exit(0);
        }

        File commitDir = new File(".gitlet/commits");
        List<String> commits = Arrays.asList(commitDir.list());

        for (int i = 0; i < commits.size(); i++) {
            if (commits.get(i).contains(commitCode)) {
                commitCode = commits.get(i);
                commitCode = commitCode.substring(0, commitCode.length() - 4);
                break;
            }
        }

        File curr = new File(".gitlet/commits/" + commitCode + ".txt");

        if (!curr.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit currComm = Utils.readObject(curr, Commit.class);
        if (!currComm.getBlobs().containsKey(fileRef)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File file = new File(currDir.getPath() + fileRef);
        if (file.exists()) {
            Utils.restrictedDelete(currDir.getPath() + fileRef);
        }

        File currBlob = new File(currDir.getPath() + "/.gitlet/blobs/"
                + currComm.getBlobs().get(fileRef) + ".txt");
        Utils.writeContents(new File(currDir.getPath(), fileRef),
                Utils.readContents(currBlob));
    }

    /** Remove method that takes FILENAME. */
    public void remove(String fileName) {
        boolean staged = stage.getStagedFiles().containsKey(fileName);

        String code = Utils.readContentsAsString(new File(
                ".gitlet/branches/" + headPointer + ".txt"));
        Commit curr = Utils.readObject(new File(
                ".gitlet/commits/" + code + ".txt"), Commit.class);

        boolean committed = false;
        ArrayList<String> committedFiles = new
                ArrayList<>(curr.getBlobs().keySet());
        for (int i = 0; i < committedFiles.size(); i++) {
            if (committedFiles.get(i).equals(fileName)) {
                committed = true;
                Utils.restrictedDelete(fileName);
                stage.removedAdd(fileName);
                if (staged) {
                    stage.getStagedFiles().remove(fileName);
                }
                Utils.writeObject(new File(""
                        + ".gitlet/staging/stage.txt"), stage);
                break;
            }
        }
        if (staged) {
            stage.getStagedFiles().remove(fileName);
            Utils.writeObject(new File(""
                    + ".gitlet/staging/stage.txt"), stage);
        }
        if (!staged && !committed) {
            System.out.println("No reason to remove the file.");
        }
    }

    /** Find method that takes MESSAGE. */
    public void find(String message) {

        boolean found = false;
        List<String> commits = Arrays.asList(
                new File(".gitlet/commits").list());
        for (int i = 0; i < commits.size(); i++) {
            Commit curr = Utils.readObject(new File(".gitlet/commits/"
                    + commits.get(i)), Commit.class);
            if (curr.getMessage().equals(message)) {
                System.out.println(curr.getCode());
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Status method returns void. */
    public void status() {
        if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory");
            System.exit(0);
        }

        File branchFolder = new File(".gitlet/branches");
        String[] branches = branchFolder.list();
        ArrayList<String> branchFiles = new ArrayList<>();
        for (int i = 0; i < branches.length; i++) {
            String name = branches[i];
            if (name.endsWith(".txt")) {
                name = name.substring(0, name.length() - 4);
            }
            branchFiles.add(name);
        }

        branchFiles.remove("headPointer");
        branchFiles.remove(headPointer);
        String add = "*" + headPointer;
        branchFiles.add(add);
        Collections.sort(branchFiles);
        List<String> removedFiles = stage.getRemoved();
        Collections.sort(removedFiles);

        List<String> staged = new ArrayList<String>();
        for (Map.Entry<String, String> entry
                : stage.getStagedFiles().entrySet()) {
            staged.add(entry.getKey());
        }

        System.out.println("=== Branches ===");
        branchFiles.forEach(branch -> {
            System.out.println(branch);
        });
        System.out.println();
        System.out.println("=== Staged Files ===");
        staged.forEach(stager -> {
            System.out.println(stager);
        });
        System.out.println();
        System.out.println("=== Removed Files ===");
        removedFiles.forEach(file -> {
            System.out.println(file);
        });
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /** Branch method that takes NAME. */
    public void branch(String name) {
        File branch = new File(".gitlet/branches/" + name + ".txt");
        File head = new File(".gitlet/branches/" + headPointer + ".txt");
        if (branch.exists()) {
            System.out.println("A branch with that name already exists");
            System.exit(0);
        } else {
            Utils.writeContents(branch, Utils.readContentsAsString(head));
        }
    }

    /** Remove branch method that takes NAME. */
    public void removeBranch(String name) {
        File branchHead = new File(".gitlet/branches/headPointer.txt");
        String branchHeadName = Utils.readContentsAsString(branchHead);
        if (name.equals(branchHeadName)) {
            System.out.print("Cannot remove the current branch.");
            System.exit(0);
        }
        File branchFile = new File(".gitlet/branches/" + name + ".txt");
        if (!branchFile.exists()) {
            System.out.print("A branch with that name does not exist.");
        } else {
            branchFile.delete();
        }
    }

    /** Reset method that take COMMCODE. */
    public void reset(String commCode) {
        File commFile = new File(".gitlet/commits/"
                + commCode + ".txt");

        if (!commFile.exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            String code = Utils.readContentsAsString(new File(""
                    + ".gitlet/branches/" + headPointer + ".txt"));
            Commit curr = Utils.readObject(new File(".gitlet/commits/"
                    + code + ".txt"), Commit.class);

            List<File> currDirFiles = Arrays.asList(currDir.listFiles());
            ArrayList<File> txtFiles = new ArrayList<>();
            for (int i = 0; i < currDirFiles.size(); i++) {
                if (currDirFiles.get(i).getName().endsWith(".txt")) {
                    txtFiles.add(currDirFiles.get(i));
                }
            }

            Commit checkoutComm = Utils.readObject(commFile, Commit.class);

            for (int i = 0; i < txtFiles.size(); i++) {
                File f = txtFiles.get(i);
                if (checkoutComm.getBlobs().containsKey(f.getName())
                        && !curr.getBlobs().containsKey(f.getName())) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first..");
                    System.exit(0);
                }
            }
            ArrayList<String> files = new ArrayList<>(checkoutComm
                    .getBlobs().keySet());
            for (int i = 0; i < txtFiles.size(); i++) {
                if (curr.getBlobs().containsKey(txtFiles.get(i).getName())) {
                    if (!checkoutComm.getBlobs().containsKey(txtFiles.
                            get(i).getName())) {
                        Utils.restrictedDelete(txtFiles.get(i));
                    }
                }
            }

            for (int i = 0; i < files.size(); i++) {
                String blobCode = checkoutComm.getBlobs().get(files.get(i));
                File blob = new File(".gitlet/blobs/" + blobCode + ".txt");
                File f = new File(files.get(i));
                Utils.writeContents(f, Utils.readContents(blob));
            }

            stage.reset();
            Utils.writeObject(new File(
                    ".gitlet/staging/stage.txt"), stage);
            Utils.writeContents(new File(".gitlet/branches/"
                    + headPointer + ".txt"), commCode);

        }
    }

    /** Merge error check takes BRANCHNAME. */
    public void mergeErrorCheck(String branchName) {
        File branch = new File(".gitlet/branches/" + branchName + ".txt");

        if (!stage.getStagedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!stage.getRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (branchName.equals(headPointer)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if  (!branch.exists()) {
            System.out.println("A branch with that name does not exist");
            System.exit(0);
        }
    }

    /** Merge method that takes BRANCHNAME. */
    public void merge(String branchName) throws IOException {
        mergeErrorCheck(branchName);
        List<File> currDirFiles = Arrays.asList(currDir.listFiles());
        ArrayList<File> txtFiles = new ArrayList<>();
        for (int i = 0; i < currDirFiles.size(); i++) {
            if (currDirFiles.get(i).getName().endsWith(".txt")) {
                txtFiles.add(currDirFiles.get(i));
            }
        }
        Commit curr = getCurrCom();
        String branchCode = Utils.readContentsAsString(new File(""
                + ".gitlet/branches/" + branchName + ".txt"));
        Commit branchComm = getBranchComm(branchName);
        for (File f: txtFiles) {
            if (!curr.getBlobs().containsKey(f.getName())
                    && branchComm.getBlobs().containsKey(f.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        Commit currCopy = curr;
        LinkedHashMap<String, Commit> commTree = new LinkedHashMap<>();
        while (currCopy != null
                && new File(".gitlet/commits/"
                + currCopy.getParent() + ".txt").exists()) {
            commTree.put(currCopy.getCode(), Utils.readObject(new File(""
                    + ".gitlet/commits/"
                    + currCopy.getCode() + ".txt"), Commit.class));
            currCopy = Utils.readObject(new File(".gitlet/commits/"
                    + currCopy.getParent() + ".txt"), Commit.class);
        }
        Commit branchCopy = branchComm;
        Commit split = null;
        while (branchCopy != null && new File("" + ".gitlet/commits/"
                + branchCopy.getParent() + ".txt").exists()) {
            if (commTree.containsKey(branchCopy.getCode())) {
                split = commTree.get(branchCopy.getCode());
                break;
            }
            branchCopy = Utils.readObject(new File(".gitlet/commits/"
                    + branchCopy.getParent() + ".txt"), Commit.class);
        }
        if (split == null) {
            System.out.println("There was an error finding the split point.");
            System.exit(0);
        }
        if (split.getCode().equals(curr.getCode())) {
            Utils.writeContents(new File(".gitlet/branches/"
                    + headPointer + ".txt"), branchCode);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else if (commTree.containsKey(branchCode)) {
            System.out.print("Given branch is an ancestor of the "
                    + "current branch.");
            System.exit(0);
        }
        mergeHelper(branchName, split, curr, branchComm, branchCode);

    }

    /** Merge helper method takes BRANCHNAME, SPLIT, CURR, BRANCHCOMM,
     * and BRANCHCODE. */
    public void mergeHelper(String branchName, Commit split, Commit curr,
                            Commit branchComm, String branchCode)
            throws IOException {
        boolean conflict = false;
        ArrayList<String> blobKeys = new ArrayList<>(curr.getBlobs().keySet());
        for (int i = 0; i < blobKeys.size(); i++) {
            String file = blobKeys.get(i);
            String cFileName = curr.getBlobs().get(file);
            String bFileName = branchComm.getBlobs().get(file);
            if (split.getBlobs().containsKey(file)
                    && branchComm.getBlobs().containsKey(file)) {
                if (!split.getBlobs().get(file).equals(bFileName)
                        && split.getBlobs().get(file).equals(cFileName)) {
                    checkout3(branchCode, "--", file);
                    add(file);
                    Utils.writeObject(new File(""
                            + ".gitlet/staging/stage.txt"), stage);
                }
                if (!split.getBlobs().get(file).equals(bFileName)) {
                    mHelper(cFileName, file, bFileName);
                    conflict = true;
                }
                if (!split.getBlobs().get(file).equals(cFileName)) {
                    mHelper(cFileName, file, bFileName);
                    conflict = true;
                }
                if (!bFileName.equals(cFileName)) {
                    mHelper(cFileName, file, bFileName);
                    conflict = true;
                }
            } else if (split.getBlobs().containsKey(file)
                    && !branchComm.getBlobs().containsKey(file)
                    && !split.getBlobs().get(file).equals(curr
                    .getBlobs().get(file))) {
                mHelper(cFileName, file, bFileName);
                conflict = true;
            }
        }
        ArrayList<String> sFiles = new ArrayList<>(split.getBlobs().keySet());
        ArrayList<String> branchFiles = new ArrayList<>(branchComm
                .getBlobs().keySet());
        for (int i = 0; i < branchFiles.size(); i++) {
            if (!sFiles.contains(branchFiles.get(i))) {
                checkout3(branchCode, "--", branchFiles.get(i));
            }
        }
        for (int i = 0; i < sFiles.size(); i++) {
            if (curr.getBlobs().containsKey(sFiles.get(i))) {
                if (split.getBlobs().get(sFiles.get(i)).equals(curr.getBlobs()
                        .get(sFiles.get(i)))) {
                    remove(sFiles.get(i));
                }
            }
        }
        if (!conflict) {
            commit("Merged " + branchName + " into " + headPointer + ".");
        } else {
            System.out.println("Encountered merge conflict.");
        }
    }

    /** Deep copy method take FIRST and SECOND and returns finalByte. */
    private byte[] deepCopy(byte[] first, byte[] second) {
        byte[] finalByte = new byte[first.length + second.length];
        System.arraycopy(first, 0, finalByte, 0, first.length);
        System.arraycopy(second, 0, finalByte, first.length, second.length);
        return finalByte;
    }

    /** Merge helper takes C, FNAME, B.*/
    public void mHelper(String c, String fName, String b) {
        File merge = new File(currDir.getPath() + "/" + fName);
        byte[] fullyMerged = deepCopy("<<<<<<< HEAD\n".getBytes(
                StandardCharsets.UTF_8),
                Utils.readContents(new File(".gitlet/blobs/"
                        + c + ".txt")));
        fullyMerged = deepCopy(fullyMerged, ("=======\n"
                + "").getBytes(StandardCharsets.UTF_8));
        fullyMerged = deepCopy(fullyMerged, Utils.readContents(
                new File(".gitlet/blobs/" + b + ".txt")));
        fullyMerged = deepCopy(fullyMerged, ">>>>>>>\n"
                .getBytes(StandardCharsets.UTF_8));
        Utils.writeContents(merge, fullyMerged);
    }

    /** Get current comm method that returns currComm. */
    public Commit getCurrCom() {
        String code = Utils.readContentsAsString(new File(""
                + ".gitlet/branches/" + headPointer + ".txt"));
        Commit currComm = Utils.readObject(new File(""
                + ".gitlet/commits/" + code + ".txt"), Commit.class);
        return currComm;
    }

    /** Get branch commit that takes BRANCHNAME and returns branchComm. */
    public Commit getBranchComm(String branchName) {
        String branchCode = Utils.readContentsAsString(new File(""
                + ".gitlet/branches/" + branchName + ".txt"));
        Commit branchComm = Utils.readObject(new File(".gitlet/commits/"
                + branchCode + ".txt"), Commit.class);
        return branchComm;
    }

}
