import java.util.ArrayList;
import java.util.HashSet;

public class Cell {
    /* A list of the cell's neighbors */
    private final ArrayList<Cell> neighbors;
    /* The current state of this cell. */
    private State state;
    /* Number of mines adjacent to this cell. */
    private short neighborMines;
    /* True if the cell contains a mine */
    private boolean hasMine;

    /**
     * By default, cell is created in the HIDDEN state with no mines and no neighbors.
     */
    protected Cell() {
        this.state = State.HIDDEN;
        this.neighborMines = 0;
        this.hasMine = false;
        this.neighbors = new ArrayList<>(8);
    }

    /**
     * Traverse the input array of cells and initialize each cell's neighbors
     * accordingly.
     *
     * @param a Input array of cells.
     */
    protected static void mkNeighbors(Cell[][] a) {
        int height = a.length;
        int width = a[0].length;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                a[i][j].mkNeighbors(a, j, i);
            }
        }
    }

    /**
     * Clicks a cell. If a cell is already revealed, nothing happens. If
     * a cell contains a mine, it explodes. If a cell is clicked with no neighbors
     * and no mine, the neighboring cells are revealed if they also have no
     * neighboring mines nor a mine itself. This happens recursively.
     *
     * @return An object containing the resulting cell state
     * along with the number of cells revealed.
     */
    protected ClickResult clickCell() {
        int cellsRevealed = 0;
        if (this.state.isHidden() && this.state != State.FLAG) {
            cellsRevealed += 1;
            if (this.hasMine) {
                this.state = State.BOOM;
            } else {
                this.state = State.NUM;
                if (this.neighborMines == 0) {
                    var visited = new HashSet<Cell>();
                    visited.add(this);
                    cellsRevealed += this.revealBlanks(visited);
                }
            }
        }
        return new ClickResult(cellsRevealed, this.state);
    }

    /**
     * Propagates clicks on safe cells with no adjacent mines;
     *
     * @param visited Cells already visited
     * @return Number of cells revealed.
     */
    private int revealBlanks(HashSet<Cell> visited) {
        int cellsRevealed = 0;
        for (Cell c : neighbors) {
            if (!visited.contains(c)) {
                visited.add(c);
                if (!c.hasMine && c.getState() != State.FLAG) {
                    ++cellsRevealed;
                    c.state = State.NUM;
                    if (c.neighborMines == 0) {
                        cellsRevealed += c.revealBlanks(visited);
                    }
                }
            }
        }
        return cellsRevealed;
    }

    protected Cell.State toggleMark() {
        if (this.state == State.HIDDEN) {
            this.state = State.FLAG;
        } else if (this.state == State.FLAG) {
            this.state = State.QMARK;
        } else if (this.state == State.QMARK) {
            this.state = State.HIDDEN;
        }
        return this.state;
    }

    /**
     * Reveals a cell without clicking on it. Thus, avoids triggering a game loss.
     * Used when revealing the board after a game ends.
     */
    protected void revealCell() {
        if (this.state.isHidden()) {
            if (this.hasMine) {
                if (this.state == State.FLAG) {
                    this.state = State.WRONG;
                } else if (this.state != State.BOOM) {
                    this.state = State.MINE;
                }
            } else {
                this.state = State.NUM;
            }
        }
    }

    /**
     * Place a mine at the cell and increments the {@link #neighborMines} for each
     * neighboring cell.
     */
    protected void placeMine() {
        if (this.hasMine) {
            throw new RuntimeException("Mine already exists at cell");
        }
        this.hasMine = true;
        for (var n : this.neighbors) {
            ++n.neighborMines;
        }
    }

    protected int adjacentMines() {
        if (this.state.isHidden()) {
            throw new RuntimeException("Cell is not yet revealed");
        } else {
            return (this.neighborMines);
        }
    }

    protected State getState() {
        return this.state;
    }

    /**
     * Set a single cell's neighbors./
     *
     * @param a Array of cells
     * @param x x-coordinate
     * @param y y-coordinate
     */
    private void mkNeighbors(Cell[][] a, int x, int y) {
        var height = a.length;
        var width = a[y].length;
        if (x > 0) {
            neighbors.add(a[y][x - 1]);
            if (y > 0) {
                neighbors.add(a[y - 1][x - 1]);
            }
            if (y < height - 1) {
                neighbors.add(a[y + 1][x - 1]);
            }
        }
        if (x < width - 1) {
            neighbors.add(a[y][x + 1]);
            if (y > 0) {
                neighbors.add(a[y - 1][x + 1]);
            }
            if (y < height - 1) {
                neighbors.add(a[y + 1][x + 1]);
            }
        }
        if (y > 0) {
            neighbors.add(a[y - 1][x]);
        }
        if (y < height - 1) {
            neighbors.add(a[y + 1][x]);
        }
    }

    /**
     * A enumeration of the various states a cell can exist in.
     */
    public enum State {
        HIDDEN, FLAG, QMARK, MINE, WRONG, BOOM, NUM;

        /**
         * Check if the cell is hidden.
         *
         * @return True if the cell is hidden.
         */
        public boolean isHidden() {
            return (this == State.HIDDEN || this==State.QMARK || this == State.FLAG);
        }
    }

    /**
     * Class for returned object from {@link #clickCell()}
     */
    protected final static class ClickResult {
        private final int numCellsRevealed;
        private final Cell.State resultState;

        private ClickResult(int cellsRevealed, Cell.State resultState) {
            this.numCellsRevealed = cellsRevealed;
            this.resultState = resultState;
        }

        protected int getNumCellsRevealed() {
            return numCellsRevealed;
        }

        protected Cell.State getResultState() {
            return resultState;
        }
    }
}
