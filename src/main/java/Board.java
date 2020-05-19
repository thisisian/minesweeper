import java.util.*;
import java.util.stream.IntStream;

import static java.util.Collections.shuffle;

/**
 * Main class which the API user interacts with.
 *
 * Represents a board of Minesweeper for a single game.
 */
public class Board {

    /**
     * Enumeration for the states the game can be in.
     *
     * {@link #START} is the initial state at the start of the game, mines have
     * not yet been distributed among the cells. Once a user selects a cell,
     * the state becomes {@link #IN_PROGRESS}. After this, the state can become
     * {@link #LOSE} or {@link #WIN}, depending on whether a mine is triggered or
     * the game is won by clearing all safe cells.
     *
     */
    public enum GameState {
        LOSE, WIN, IN_PROGRESS, START
    }

    private final int width;
    private final int height;
    private final int numMines;
    private final Cell[][] cells;
    private int hiddenCells;
    private GameState gameState;

    /**
     * Generate a board of given dimensions with a number of mines.
     *
     * @param width Width dimension of board
     * @param height Height dimension of board
     * @param numMines Number of mines existing in board.
     */
    public Board(int width, int height, int numMines) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid board size.");
        }
        if (numMines > (width*height - 1)) {
            throw new IllegalArgumentException("Too many mines.");
        }

        this.width = width;
        this.height = height;
        this.hiddenCells = width * height;
        this.numMines = numMines;
        this.gameState = GameState.START;

        this.cells = new Cell[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                cells[i][j] = new Cell();
            }
        }
        Cell.mkNeighbors(cells);
    }

    /**
     * Generate a game board with given mine layout.
     * @param width Width dimension of board
     * @param height Height dimension of board
     * @param mines Array of integers signifying where mines exist. Array is splayed
     *              across rows/columns of board. Any value != 0 signifies a mine.
     */
    public Board(int width, int height, int[] mines) {
        this(width, height, (int) Arrays.stream(mines).filter(b -> b != 0).count());
        int k = 0;
        for (var i : this.cells) {
            for (var c : i) {
                if (mines[k] != 0) {
                    c.placeMine();
                }
                ++k;
            }
        }
        this.gameState = GameState.IN_PROGRESS;
    }

    /** Click on a cell. If the game has just started, the mines
     * will be initialized such that the user's first click cannot
     * be a mine.
     *
     * If the user has selected a mine, the uesr has lost the game,
     * the board is revealed and the {@link GameState} changes accordingly.
     *
     * If a user has revealed all the safe cells, there game is won,
     * the board is revealed and the {@link GameState} changes to {@link #WON}.
     *
     * When a user clicks on safe cells who have no neighbors,
     * the similar cells nearby are also cleared0.
     *
     * @param x x-coordinate of click on board
     * @param y y-coordinate of click on board
     * @return resulting {@link GameState}
     */
    public GameState sweep(int x, int y) {
        if (this.gameState == GameState.START) {
            this.initializeMines(x, y);
            this.gameState = GameState.IN_PROGRESS;
        }

        var clickResult = cells[y][x].clickCell();
        hiddenCells -= clickResult.getNumCellsRevealed();
        if (clickResult.getResultState() == Cell.State.BOOM) {
            revealAllCells();
            this.gameState = GameState.LOSE;
        } else {
            if (hiddenCells == numMines) {
                revealAllCells();
                this.gameState = GameState.WIN;
            }
        }
        return this.gameState;
    }

    public void toggleMark(int x, int y) {
        cells[y][x].toggleMark();
    }

    private void revealAllCells() {
        for (int i = 0; i < cells.length; ++i) {
            for (int j = 0; j < cells[0].length; ++j) {
                cells[i][j].revealCell();
            }
        }
    }

    private void initializeMines(int initX, int initY) {
        List<Integer> coords =
                IntStream.range(0, width*height).boxed().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        shuffle(coords);
        int minesToPlace = this.numMines;
        for (int i = 0; minesToPlace > 0; ++i) {
            int x = coords.get(i) % width;
            int y = coords.get(i) / width;
            if (x != initX || y != initY) {
                cells[y][x].placeMine();
                --minesToPlace;
            }
        }
    }

    /**
     * Get the cell's state at a location on the board.
     * @param x x-coordinate of cell
     * @param y y-coordinate of cell
     * @return The cell's current state.
     */
    public Cell.State getCellState(int x, int y) {
        return this.cells[y][x].getState();
    }

    /**
     * Get
     * @param x x-coordinate of cell
     * @param y y-coordinate of cell
     * @return Count of adjacent mines.
     */
    public int adjacentMines(int x, int y) {
        return this.cells[y][x].adjacentMines();
    }

    /**
     * Get the board's height dimension.
     * @return The board's height dimension.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the board's width dimension.
     * @return The board's width dimension.
     */
    public int getWidth() {
        return this.width;
    }

    public GameState getState() {
        return this.gameState;
    }
}
