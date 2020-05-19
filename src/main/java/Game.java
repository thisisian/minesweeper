import java.util.Scanner;

public class Game {

    private static class Command {
       private enum Cmd {MARK, SWEEP, HELP}
       Cmd cmd;
       int x;
       int y;
       private Command(Cmd cmd, int x, int y) {
           this.cmd = cmd;
           this.x = x;
           this.y = y;
       }
    }

    public static String stringDisplay(Board b) {
        StringBuilder sb = new StringBuilder(b.getHeight() * b.getHeight() + b.getHeight());
        for (int i = 0; i < b.getHeight(); ++i) {
            for (int j = 0; j < b.getWidth(); ++j) {
                switch (b.getCellState(j, i)) {
                    case MINE -> sb.append("*");
                    case HIDDEN -> sb.append("~");
                    case NUM -> {
                        var n = b.adjacentMines(j, i);
                        if (n == 0) {
                            sb.append("_");
                        } else {
                            sb.append(n);
                        }
                    }
                    case BOOM -> sb.append("#");
                    case FLAG -> sb.append("P");
                    case QMARK -> sb.append("?");
                    case WRONG -> sb.append("X");
                    default -> throw new RuntimeException("Invalid case: " + b.getCellState(j, i));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void printHelpAndExit() {
        System.out.println("Usage: minesweeper [WIDTH] [HEIGHT] [NUM MINES]");
        System.exit(0);
    }

    private static Command parseCommand(Scanner s) {
        if (s.hasNext("M")) {
            s.next();
            // Toggle Marking
            if (s.hasNextInt()) {
                var x = s.nextInt();
                if (s.hasNextInt()) {
                    var y = s.nextInt();
                    return new Command(Command.Cmd.MARK, x, y);
                }
            }
        } else if (s.hasNextInt()) {
            // Sweep this coordinate
            var x = s.nextInt();
            if (s.hasNextInt()) {
                var y = s.nextInt();
                return new Command(Command.Cmd.SWEEP, x, y);
            }
        }
        return new Command(Command.Cmd.HELP, 0, 0);
    }

    private static Board parseBoard(String[] args) {
        int width;
        int height;
        int numMines;
        try {
            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            numMines = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return null;
        }
        return new Board(width, height, numMines);
    }

    private static void printCommandHelp() {
        System.out.println("Command Help");
        System.out.println("Toggle marking:");
        System.out.println("M [x-coordinate] [y-coordinate]");
        System.out.println("Sweep square:");
        System.out.println("[x-coordinate] [y-coordinate]");
    }

    public static void main(String[] args) {

        if (args.length != 3) {
            printHelpAndExit();
        }

        var board = parseBoard(args);
        assert (board != null);

        var stdin = new Scanner(System.in);
        System.out.print(stringDisplay(board));
        for (;;) {
            System.out.println("Enter command:");
            System.out.print("> ");

            if (stdin.hasNextLine()) {
                var cmd = parseCommand(new Scanner(stdin.nextLine()));
                switch (cmd.cmd) {
                    case MARK -> {
                        board.toggleMark(cmd.x, cmd.y);
                        System.out.print(stringDisplay(board));
                    }
                    case SWEEP -> {
                        board.sweep(cmd.x, cmd.y);
                        System.out.print(stringDisplay(board));
                    }
                    case HELP -> printCommandHelp();
                }

                switch (board.getState()) {
                    case WIN -> {
                        System.out.println("You win!");
                        System.exit(0);
                    }
                    case LOSE -> {
                        System.out.println("BOOM!");
                        System.exit(0);
                    }
                    default -> {}
                }
            } else {
                System.exit(0);
            }
        }

    }


}
