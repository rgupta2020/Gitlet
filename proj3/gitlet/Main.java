package gitlet;

import java.io.IOException;

/**
 * Driver class for Gitlet.
 *
 * @author Rishit Gupta
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) throws IOException {
        Gitlet g = new Gitlet();

        if (args.length == 0 || args[0] == null) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        switch (args[0]) {
        case "init":
            if (args.length == 1) {
                g.init();
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "add":
            if (args.length == 2) {
                g.add(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "commit":
            if (args.length == 2) {
                g.commit(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "log":
            g.log();
            break;
        case "checkout":
            if (args.length == 2) {
                g.checkout1(args[1]);
            } else if (args.length == 3) {
                g.checkout2(args[1], args[2]);
            } else if (args.length == 4) {
                g.checkout3(args[1], args[2], args[3]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "rm":
            if (args.length == 2) {
                g.remove(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        default:
            main2(args);
        }
        System.exit(0);
    }

    /** Main2 method that takes ARGS. */
    public static void main2(String... args) throws IOException {
        Gitlet g = new Gitlet();
        switch (args[0]) {
        case "status":
            if (args.length == 1) {
                g.status();
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "find":
            if (args.length == 2) {
                g.find(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "global-log":
            if (args.length == 1) {
                g.globalLog();
            } else {
                System.out.print("Invalid operands.");
            }
            break;
        case "branch":
            if (args.length == 2) {
                g.branch(args[1]);
            } else {
                System.out.print("Invalid operands.");
            }
            break;
        case "rm-branch":
            if (args.length == 2) {
                g.removeBranch(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "reset":
            if (args.length == 2) {
                g.reset(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        case "merge":
            if (args.length == 2) {
                g.merge(args[1]);
            } else {
                System.out.println("Invalid operands.");
            }
            break;
        default:
            System.out.print("No command with that name exists.");
        }
        System.exit(0);
    }

}
