package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import static gitlet.Utils.*;
import java.util.Date;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Rhea Shah
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date date;
    private HashMap<String, String> filesToBeCommitted;
    private String prevCommitSha1;
    private String prevCommit2Sha1;

    public Commit(Date date, String message){
        this.message = message;
        this.date = date;
        this.filesToBeCommitted = new HashMap<String, String>();
    }

    public Commit(Date date, String message, HashMap<String, String> filesToBeCommitted, String prevCommitSha1){
        this.message = message;
        this.date = date;
        this.filesToBeCommitted = filesToBeCommitted;
        this.prevCommitSha1 = prevCommitSha1;
    }

    public String getMessage(){
        return message;
    }
    public Date getDate(){
        return date;
    }
    public HashMap<String, String> getFilesInCommit(){
        return filesToBeCommitted;
    }

    public String getPrevSha1Commit(){
        return prevCommitSha1;
    }

    public String getParent2CommitSHA1(){ return prevCommit2Sha1;}

    public void setParent2CommitSha1(String sha1){
        prevCommit2Sha1 = sha1;
    }

    public String saveCommit(){
        String commitSha1 = sha1(Utils.serialize(this));
        File f1 = new File (String.valueOf(Utils.join(Repository.commits, commitSha1)));
        try {
            f1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(f1, this);
        return commitSha1;
    }

    public static Commit fromFile(String commitName) {
        return Utils.readObject(new File(Repository.commits, commitName), Commit.class);
    }

}
