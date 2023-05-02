package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args){
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs("find", args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Repository.status();
                break;
            case "checkout":
                validateNumArgs("checkout", args, 4);
                if (args.length == 2){
                    Repository.checkoutBranch(args[1]);
                } else if(args.length == 3){
                    Repository.checkoutFileName(args[2]);
                } else{
                    Repository.checkoutCommitIDAndFileName(args[1], args[3]);
                }
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                Repository.merge(args[1]);
                break;
            default:
                exitWithError("No command with that name exists.");
        }
    }

    /**
     * Prints out MESSAGE and exits with error code -1.
     * Note:
     *     The functionality for erroring/exit codes is different within Gitlet
     *     so DO NOT use this as a reference.
     *     Refer to the spec for more information.
     * @param message message to print
     */
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }

    private static void validateNumArgs(String cmd, String[] var1, int var2) {
        if (cmd.equals("checkout")){
            if (var1.length == 0 || var1.length > 4){
                exitWithError("Incorrect operands.");
            } else{
                if (var1.length == 3 && !var1[1].equals("--")){
                    exitWithError("Incorrect operands.");
                } else if(var1.length == 4 && !var1[2].equals("--")){
                    exitWithError("Incorrect operands.");
                }
            }
        } else{
            if (var1.length != var2) {
                if (cmd.equals("commit")){
                    exitWithError("Please enter a commit message.");
                }
                exitWithError("Incorrect operands.");
            }
        }
    }

}
