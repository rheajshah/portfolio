package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Rhea Shah
 */
public class Repository {
    /** List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you. */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static File HEAD = join(GITLET_DIR, "HEAD");
    public static File branches = new File(GITLET_DIR, "branches");
    public static File commits = new File(GITLET_DIR, "commits");
    public static File objects = new File(GITLET_DIR, "objects");
    public static File addingStage = new File(GITLET_DIR, "addingStage");
    public static File removalStage = new File(GITLET_DIR, "removalStage");
    public static SimpleDateFormat simpleDate = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    public static void init(){
        if (!GITLET_DIR.exists()) {
            setupPersistence();
            String message = "initial commit";
            Date date0 = new Date(0);
            Commit firstCommit = new Commit(date0, message);
            try {
                HEAD.createNewFile();
                branches.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeContents(branches, "master=" + sha1(Utils.serialize(firstCommit)));
            Utils.writeContents(HEAD, "master");
            firstCommit.saveCommit();
        } else{
            System.out.println("A gitlet version-control system already exists in the current directory.");
        }
    }

    private static void setupPersistence(){
        GITLET_DIR.mkdir();
        commits.mkdir();
        objects.mkdir();
        try {
            addingStage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            removalStage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean gitletExists(){
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        return true;
    }

    public static void add(String fileName){
        File toBeAdded = new File(fileName);
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        } else if(!toBeAdded.exists()){ //errors if file passed doesn't exist
            System.out.print("File does not exist.");
            return;
        } else{
            HashMap<String, String> filesInRemovalStage = createHashMapFromFileContents(removalStage);
            if (filesInRemovalStage.containsKey(fileName)){ //if file is in removalStage, remove it
                filesInRemovalStage.remove(fileName);
                saveHashMapToFile(filesInRemovalStage, removalStage);
            } else {
                HashMap<String, String> arrayOfFilesInAddStage = createHashMapFromFileContents(addingStage);
                String toAddSha1 = sha1(Utils.readContents(new File(fileName))); //sha1 of contents of file passed in
                Commit currentHEADCommit = getCurrentCommit();

                if (arrayOfFilesInAddStage.containsKey(fileName)){ //if fileName already is in addingStage
                    if (arrayOfFilesInAddStage.get(fileName).equals(toAddSha1)){ //exit method if no changes have been made to file
                        return;
                    }
                    arrayOfFilesInAddStage.put(fileName, toAddSha1); //changes value to new sha1
                } else{ //if file doesn't already exist in addingStage
                    if (toAddSha1.equals(currentHEADCommit.getFilesInCommit().get(fileName))){
                        return;
                    }
                    arrayOfFilesInAddStage.put(fileName, toAddSha1); //add new key-value pair
                }

                File toAdd = Utils.join(objects, toAddSha1); //file containing sha1 added to objs folder
                Utils.writeContents(toAdd, Utils.readContentsAsString(toBeAdded));
                saveHashMapToFile(arrayOfFilesInAddStage, addingStage); //re-writes addingStage
            }
        }
    }

    private static HashMap<String, String> createHashMapFromFileContents(File fileName){
        HashMap<String, String> result = new HashMap<String, String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (scanner.hasNextLine()) { //reads each line of addingStage
            String nextLine = scanner.next();
            String[] keyValuePair = nextLine.split("="); //separates line into String[] split by =
            result.put(keyValuePair[0], keyValuePair[1]); //add each key-value pair to ArrayList
        }
        return result;
    }

    private static void saveHashMapToFile(HashMap<String, String> hashMap, File fileName){
        String newFileText = "";
        int entryNum = 0;
        for (Map.Entry<String, String> entry: hashMap.entrySet()){
            newFileText += (entry.getKey() + "=" + entry.getValue());
            if (entryNum < hashMap.size() - 1){
                newFileText += "\n";
            }
            entryNum++;
        }
        Utils.writeContents(fileName, newFileText);
    }

    /** Returns text to overwrite file contents with.
     * Takes in ArrayList of String[]
     * Returns a String formatted in key-value pair style*/
    private static String newFileText(ArrayList<String[]> stagedFileText){
        String newFileText = "";
        for (int i = 0; i < stagedFileText.size(); i++){
            newFileText += (stagedFileText.get(i))[0] + "=" + (stagedFileText.get(i))[1];
            if (i < stagedFileText.size() - 1){
                newFileText += "\n";
            }
        }
        return newFileText;
    }

    private static boolean isEmpty(File file){
        if (Utils.readContentsAsString(file).equals("")){
            return true;
        }
        return false;
    }

    private static void clearStages(){
        Utils.writeContents(addingStage, "");
        Utils.writeContents(removalStage, "");
    }

    private static Commit getCurrentCommit(){
        HashMap<String, String> filesInBranch = createHashMapFromFileContents(branches);
        String commitIDOfCurrentHEADCommit = filesInBranch.get(Utils.readContentsAsString(HEAD));
        return Commit.fromFile(commitIDOfCurrentHEADCommit);
    }

    private static Commit getCurrentCommit(String branchName){
        HashMap<String, String> filesInBranch = createHashMapFromFileContents(branches);
        String commitIDOfCurrentHEADCommit = filesInBranch.get(branchName);
        return Commit.fromFile(commitIDOfCurrentHEADCommit);
    }

    private static String getCurrentBranch(){
        return Utils.readContentsAsString(HEAD);
    }

    public static void commit(String message, String secondParentSha1) {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        if (isEmpty(addingStage) && isEmpty(removalStage)){
            Main.exitWithError("No changes added to the commit.");
            return;
        } else if(message.equals("")){
            Main.exitWithError("Please enter a commit message.");
        }

        HashMap<String, String> branchNames = createHashMapFromFileContents(branches);

        Commit currentHEADCommit = getCurrentCommit();
        HashMap<String, String> filesInHEADCommit = currentHEADCommit.getFilesInCommit();

        if (!isEmpty(removalStage)){
            HashMap<String, String> filesToBeRemovedFromCommit = createHashMapFromFileContents(removalStage);
            for (Map.Entry<String, String> entry: filesToBeRemovedFromCommit.entrySet()){
                filesInHEADCommit.remove(entry.getKey(), entry.getValue());
            }
        }
        if (!isEmpty(addingStage)){
            HashMap<String, String> filesToBeCommitted = createHashMapFromFileContents(addingStage);
            for (Map.Entry<String, String> entry: filesToBeCommitted.entrySet()){
                filesInHEADCommit.put(entry.getKey(), entry.getValue());
            }
        }

        Commit newCommit = new Commit(new Date(), message, filesInHEADCommit, branchNames.get(getCurrentBranch()));
        newCommit.setParent2CommitSha1(secondParentSha1);
        String newCommitSha1 = newCommit.saveCommit();
        branchNames.put(getCurrentBranch(), newCommitSha1);
        saveHashMapToFile(branchNames, branches);

        clearStages(); //clear the adding and removal stages after commit
    }

    public static void commit(String message) {
        commit(message, null);
    }

    public static void rm(String fileName) {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        HashMap<String, String> filesInAddingStage = createHashMapFromFileContents(addingStage);
        Commit currentHEADCommit = getCurrentCommit();

        if (filesInAddingStage.containsKey(fileName)){ //if file is in addingStage, remove it and re-add all files w/out that file to addingStage
            filesInAddingStage.remove(fileName);
            saveHashMapToFile(filesInAddingStage, addingStage);
        } else if ((currentHEADCommit.getFilesInCommit()).containsKey(fileName)){
            HashMap<String, String> filesInRemovalStage = createHashMapFromFileContents(removalStage);

            String sha1OfNewFile = currentHEADCommit.getFilesInCommit().get(fileName);
            filesInRemovalStage.put(fileName, sha1OfNewFile);
            saveHashMapToFile(filesInRemovalStage, removalStage);

            File toBeDeleted = new File(fileName);
            Utils.restrictedDelete(toBeDeleted);
        } else{
            System.out.println("No reason to remove the file.");
        }
    }

    public static void log() {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        HashMap<String, String> filesInBranch = createHashMapFromFileContents(branches);
        String currentSha1 = filesInBranch.get(getCurrentBranch());
        while (currentSha1 != null) {
            Commit pointer = Commit.fromFile(currentSha1);
            printLog(pointer, currentSha1);
            currentSha1 = pointer.getPrevSha1Commit();
        }
    }

    private static void printLog(Commit c, String currentCommitSha1) {
        System.out.println("===");
        System.out.println("commit " + currentCommitSha1);
        System.out.println("Date: " + simpleDate.format(c.getDate()));
        System.out.println(c.getMessage());
        System.out.println();
    }


    public static void globalLog() {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        List<String> allFilesInCommits = Utils.plainFilenamesIn(commits);
        for (String fileName: allFilesInCommits){
            Commit current = Commit.fromFile(fileName);
            printLog(current, fileName);
        }
    }

    public static void find(String commitMessage) {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        List<String> allFilesInCommits = Utils.plainFilenamesIn(commits);
        int numCommitsWithCommitMessage = 0;
        for (String commitID: allFilesInCommits){
            Commit current = Commit.fromFile(commitID);
            if ((current.getMessage()).equals(commitMessage)){
                System.out.println(commitID);
                numCommitsWithCommitMessage++;
            }
        }
        if (numCommitsWithCommitMessage == 0){
            Main.exitWithError("Found no commit with that message.");
        }
    }

    public static void status() {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        System.out.println("=== Branches ===");
        HashMap<String, String> allBranches = createHashMapFromFileContents(branches);
        List<String> branchNames = new ArrayList<>(allBranches.keySet());
        Collections.sort(branchNames);
        for (String branch: branchNames){
            if (branch.equals(getCurrentBranch())){
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        HashMap<String, String> filesInAddingStage = createHashMapFromFileContents(addingStage);
        List<String> addingStage = new ArrayList<>(filesInAddingStage.keySet());
        Collections.sort(addingStage);
        for (String fileName: addingStage){
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        HashMap<String, String> filesInRemovalStage = createHashMapFromFileContents(removalStage);
        List<String> removalStage = new ArrayList<>(filesInRemovalStage.keySet());
        Collections.sort(removalStage);
        for (String fileName: removalStage){
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkoutFileName(String fileName){
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        Commit pointer = getCurrentCommit();
        HashMap<String, String> filesInCommit = pointer.getFilesInCommit();
        if (filesInCommit.containsKey(fileName)){
            File toBeCheckedOut = Utils.join(CWD, fileName);
            try {
                toBeCheckedOut.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File sha1InObjects = Utils.join(objects, filesInCommit.get(fileName));
            Utils.writeContents(toBeCheckedOut, Utils.readContentsAsString(sha1InObjects));
        } else{
            Main.exitWithError("File does not exist in that commit.");
            return;
        }
    }

    public static void checkoutCommitIDAndFileName(String commitID, String fileName){
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        List<String> allFilesInCommits = plainFilenamesIn(commits);
        for (String commitName: allFilesInCommits){
            if (commitName.indexOf(commitID) == 0){
                Commit pointer = Commit.fromFile(commitName);
                HashMap<String, String> filesInCommit = pointer.getFilesInCommit();
                if (filesInCommit.containsKey(fileName)){
                    File toBeCheckedOut = Utils.join(CWD, fileName);
                    File sha1InObjects = Utils.join(objects, filesInCommit.get(fileName));
                    Utils.writeContents(toBeCheckedOut, Utils.readContentsAsString(sha1InObjects));
                    return;
                }
                else{
                    Main.exitWithError("File does not exist in that commit.");
                }

            }
        }
        Main.exitWithError("No commit with that id exists.");
    }

    public static void checkoutBranch(String branchName){
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        HashMap<String, String> branchesContent = createHashMapFromFileContents(branches);
        if (!branchesContent.containsKey(branchName)){
            Main.exitWithError("No such branch exists.");
        }
        if (getCurrentBranch().equals(branchName)){
            Main.exitWithError("No need to checkout the current branch.");
        }

        Commit currentHEADCommit = getCurrentCommit();
        Commit branchCommit = getCurrentCommit(branchName);
        HashMap<String, String> filesInCurrentHEADCommit = currentHEADCommit.getFilesInCommit();
        HashMap<String, String> filesInBranchCommit = branchCommit.getFilesInCommit();

        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        for (Map.Entry<String, String> entry: filesInBranchCommit.entrySet()){
            if (filesInCWD.contains(entry.getKey())
                    && !entry.getValue().equals(sha1(Utils.readContents(new File(CWD, entry.getKey()))))
                    && ! filesInCurrentHEADCommit.containsKey(entry.getKey())){
                Main.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        //Delete the files from current commit that are not in the branch
        for(Map.Entry<String, String> entry: filesInCurrentHEADCommit.entrySet()){
            if (!filesInBranchCommit.containsKey(entry.getKey())){
                Utils.restrictedDelete(new File(CWD, entry.getKey()));
            }
        }

        for (Map.Entry<String, String> entry: filesInBranchCommit.entrySet()){
            File toBeCheckedOut = Utils.join(CWD, entry.getKey());
            Utils.writeContents(toBeCheckedOut, Utils.readContentsAsString(Utils.join(objects, entry.getValue())));
        }

        Utils.writeContents(HEAD, branchName); //overwrite HEAD contents w/ new branch
    }

    public static void branch(String branchName) {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        HashMap<String, String> branchesContent = createHashMapFromFileContents(branches);
        if (branchesContent.containsKey(branchName)){
            Main.exitWithError("A branch with that name already exists.");
        } else{
            branchesContent.put(branchName, branchesContent.get(Utils.readContentsAsString(HEAD)));
            saveHashMapToFile(branchesContent, branches);
        }
    }

    public static void rmBranch(String branchName) {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        HashMap<String, String> branchesContent = createHashMapFromFileContents(branches);
        if (!branchesContent.containsKey(branchName)){
            Main.exitWithError("A branch with that name does not exist.");
        } else if (getCurrentBranch().equals(branchName)){
            Main.exitWithError("Cannot remove the current branch.");
        } else{
            branchesContent.remove(branchName);
            saveHashMapToFile(branchesContent, branches);
        }
    }

    public static void reset(String commitID) {
        if(gitletExists() == false){ //errors if .gitlet isn't initialized in CWD
            return;
        }
        List<String> allFilesInCommits = plainFilenamesIn(commits);
        if (!allFilesInCommits.contains(commitID)){
            Main.exitWithError("No commit with that id exists.");
        }
        Commit c = Commit.fromFile(commitID);
        HashMap<String, String> filesInCommit = c.getFilesInCommit();
        HashMap<String, String> trackedFiles = createHashMapFromFileContents(addingStage);
        Commit currentHEADCommit = getCurrentCommit();
        HashMap<String, String> filesInCurrentHEADCommit = currentHEADCommit.getFilesInCommit();

        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        for (Map.Entry<String, String> entry: filesInCommit.entrySet()){
            if (filesInCWD.contains(entry.getKey())
                    && !entry.getValue().equals(sha1(Utils.readContents(new File(CWD, entry.getKey()))))
                    && ! filesInCurrentHEADCommit.containsKey(entry.getKey())){
                Main.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        for (Map.Entry<String, String> entry: trackedFiles.entrySet()){
            if(! filesInCommit.containsKey(entry.getKey())){
                rm(entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry: filesInCommit.entrySet()){
            checkoutCommitIDAndFileName(commitID, entry.getKey());
        }

        HashMap<String, String> branchesContent = createHashMapFromFileContents(branches);
        branchesContent.put(getCurrentBranch(), commitID); //moves the current branch’s head to that commit node
        saveHashMapToFile(branchesContent, branches);

        clearStages();
    }

    private static boolean isNonEmpty(String sha1){
        return (sha1 != null && sha1.trim().length() > 0);
    }

    /**
     * Helper method to fetch current branches ancestors with the branch head as the first element
     * @param branchSHA1
     * @return
     */
    private static List<String> getBranchAncestorSHA1(String branchSHA1) {
        List<String> result = new ArrayList<>();
        String commitSHA1temp = branchSHA1;
        while (isNonEmpty(commitSHA1temp)) {
            result.add(commitSHA1temp);
            Commit commitTemp = Commit.fromFile(commitSHA1temp);
            commitSHA1temp = commitTemp.getPrevSha1Commit();
            if (commitTemp.getParent2CommitSHA1() != null) {
                result.add(commitTemp.getParent2CommitSHA1());
            }
        }
        return result;
    }

    public static void merge(String branchName){
        if (!getCurrentBranch().equals(branchName)) {//branch name has to be different than current head

            //Check if there is anything in staging
            HashMap<String, String> addLines = createHashMapFromFileContents(addingStage);
            HashMap<String, String> remLines = createHashMapFromFileContents(removalStage);

            if (addLines.isEmpty() && remLines.isEmpty()){
                HashMap<String, String> branchNames = createHashMapFromFileContents(branches);
                String headSHA1 = branchNames.get(getCurrentBranch());
                String branchSHA1 = branchNames.get(branchName);

                if (isNonEmpty(headSHA1) && isNonEmpty(branchSHA1)){
                    //Find the latest common ancestor
                    List<String> branchAncestors = getBranchAncestorSHA1(branchSHA1);
                    List<String> headAncestors = getBranchAncestorSHA1(headSHA1);
                    String lcaSHA1 = null;
                    for (String branchCommitSHA1 : branchAncestors) {
                        if (headAncestors.contains(branchCommitSHA1)) {
                            lcaSHA1 = branchCommitSHA1;
                            break;
                        }
                    }

                    //If the split point is the same commit as the given branch, then we do nothing; the merge is complete,
                    // and the operation ends with the message "Given branch is an ancestor of the current branch."
                    if (lcaSHA1.equals(branchSHA1)) {
                        Main.exitWithError("Given branch is an ancestor of the current branch.");
                    }
                    // If the split point is the current branch, then the effect is to check out the given branch,
                    // and the operation ends after printing the message Current branch fast-forwarded.
                    else if (lcaSHA1.equals(headSHA1)) {
                        checkoutBranch(branchName);
                        //TODO validate whether the branch should match current head
                        Main.exitWithError("Current branch fast-forwarded.");
                    }
                    //Continue the merge
                    else {
                        boolean mergeEncountered = merge(headSHA1, branchSHA1, lcaSHA1, addLines, remLines);
                        //Commit the modified version
                        commit(String.format("Merged %s into %s.", branchName, getCurrentBranch()), branchSHA1);
                        if (mergeEncountered){
                            System.out.println("Encountered a merge conflict.");
                        }
                        //TODO Validate
                        //Update branch to the current head SHA1
                        HashMap<String, String> filesInBranch = createHashMapFromFileContents(branches);
                        String commitIDOfCurrentHEADCommit = filesInBranch.get(Utils.readContentsAsString(HEAD));
                        branchNames.put(branchName, commitIDOfCurrentHEADCommit);
                    }


                } else {
                    Main.exitWithError("A branch with that name does not exist.");
                }
            } else {//there are pending staged items
                Main.exitWithError("You have uncommitted changes.");
            }
        } else {//current and merge branch names are same, throw error
            Main.exitWithError("Cannot merge a branch with itself.");
        }
    }

    private static boolean merge(String currentSHA1, String branchSHA1, String lacSHA1, HashMap<String, String> addLines, HashMap<String, String> remLines) {
        boolean mergeEncountered = false;
        //List<String> cwdFileNames = Utils.plainFilenamesIn(CWD);
        Map<String, String> hmCommitFiles = Commit.fromFile(currentSHA1).getFilesInCommit();
        Map<String, String> hmBranchFiles = Commit.fromFile(branchSHA1).getFilesInCommit();
        Map<String, String> hmLACFiles = Commit.fromFile(lacSHA1).getFilesInCommit();

        List<String> lstModifiedFiles = new ArrayList<>();
        List<String> lstUntrackedFiles = new ArrayList<>();

        //If an untracked file in the current commit would be overwritten or deleted by the merge,
        // print 'There is an untracked file in the way; delete it, or add and commit it first.' and exit;
        // perform this check before doing anything else.
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        for (Map.Entry<String, String> entry: hmBranchFiles.entrySet()){
            if (filesInCWD.contains(entry.getKey())
                    && !entry.getValue().equals(sha1(Utils.readContents(new File(CWD, entry.getKey()))))
                    && ! hmCommitFiles.containsKey(entry.getKey())){
                Main.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        //For each file in branch, check against commit & stage add
        for (Map.Entry<String, String> branchFile: hmBranchFiles.entrySet()) {
            String fileBranchName = branchFile.getKey();
            String fileBranchSHA1 = branchFile.getValue();
//            System.out.println(String.format("merge: reviewing branch file: %s", fileBranchName));
            if (hmCommitFiles.containsKey(fileBranchName)) {//if present in Commit
//                System.out.println(String.format("merge: branch file %s present in current commit", fileBranchName));
                if (!fileBranchSHA1.equals(hmCommitFiles.get(fileBranchName))){//different from commit
//                    System.out.println(String.format("merge: branch file %s different from current commit", fileBranchName));
                    //check if current commit version is same as LAC commit version
                    if (hmCommitFiles.get(fileBranchName).equals(hmLACFiles.get(fileBranchName))) {
//                        System.out.println(String.format("merge: checking out file %s from branch %s", fileBranchName, fileBranchSHA1));
                        checkoutCommitIDAndFileName(branchSHA1, fileBranchName);
                        add(fileBranchName);
                    }
                    //2. Any files that have been modified in the current branch but not in the given branch since the split point
                    // should stay as they are.
                    else if (hmBranchFiles.get(fileBranchName).equals(hmLACFiles.get(fileBranchName))) {
                        //Don't do anything
                    } else { //both branch and commit files have been modified since LAC, so merge them
                        StringBuffer combinedFile = new StringBuffer("<<<<<<< HEAD\n");
                        combinedFile.append(Utils.readContentsAsString(new File(objects, hmCommitFiles.get(fileBranchName))));
                        combinedFile.append("=======\n");
                        combinedFile.append(Utils.readContentsAsString(new File(objects, hmBranchFiles.get(fileBranchName))));
                        combinedFile.append(">>>>>>>\n");
                        File target = new File(CWD, fileBranchName);
                        Utils.writeContents(target, combinedFile.toString());
                        add(fileBranchName);
                        mergeEncountered = true;
                    }
                }
            } else { //branch file not present in current commit
                //5. Any files that were not present at the split point and are present only in the given branch
                // should be checked out and staged.
                if (!hmLACFiles.containsKey(fileBranchName)) {
                    checkoutCommitIDAndFileName(branchSHA1, fileBranchName);
                    add(fileBranchName);
                }

            }
        }

        //6. Any files present at the split point, unmodified in the current branch, and
        // absent in the given branch should be removed (and untracked).
        //For each file in LAC, check against branch for removal
        for (Map.Entry<String, String> lacFile: hmLACFiles.entrySet()) {
            String fileLACName = lacFile.getKey();
            String fileLACSHA1 = lacFile.getValue();
            if (!hmBranchFiles.containsKey(fileLACName)) {//if not present in branch
                //check if it is unmodified in current commit
                if (fileLACSHA1.equals(hmCommitFiles.get(fileLACName))) {//same as commit
                    //TODO validate that the file should be removed ands staged for removal
                    rm(fileLACName);
                } else {
                    //#8 “Modified in different ways” can mean that ..., or the contents of one are changed and the other file is deleted
                    if (hmCommitFiles.containsKey(fileLACName)) {
                        StringBuffer combinedFile = new StringBuffer("<<<<<<< HEAD\n");
                        combinedFile.append(Utils.readContentsAsString(new File(objects, hmCommitFiles.get(fileLACName))));
                        combinedFile.append("=======\n");
                        combinedFile.append(">>>>>>>\n");
                        File target = new File(CWD, fileLACName);
                        Utils.writeContents(target, combinedFile.toString());
                        add(fileLACName);
                        mergeEncountered = true;
                    }
                }
            } else { //LAC file is present in the branch
                //7.Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.
                if (fileLACSHA1.equals(hmBranchFiles.get(fileLACName))) {//unmodified in branch
                    if (!hmCommitFiles.containsKey(fileLACName)) {//absent in Commit
                        //leave it as it-is as per rule #7
                    }
                }
            }
        }
        return mergeEncountered;
    }
}
