package corpus.merge.textalignment;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// align the annotations of two texts using dynamic programming
public class PairwiseDPTextAlignment extends TextAlignment {

	char[] textA;
	char[] textB;
	int wGap;

	DiagonalTableStrip<Integer> alignmentMatrix;
	boolean validTable = false;
	int diff;

	List<String[]> alignments = null;

	char alignChar;
	char openUnit;
	char closeUnit;
	List<Character> metaChars;

	public PairwiseDPTextAlignment(char[] textA, char[] textB, char alignChar, char openUnit, char closeUnit) {
		this(textA, textB, alignChar, openUnit, closeUnit, 1);
	}

	public PairwiseDPTextAlignment(char[] textA, char[] textB, char alignChar, char openUnit, char closeUnit, int wGap) {

		this.textA = textA;
		this.textB = textB;

		this.alignChar = alignChar;

		this.openUnit = openUnit;
		this.closeUnit = closeUnit;
		this.metaChars = new ArrayList<Character>(2);
		this.metaChars.add(openUnit);
		this.metaChars.add(closeUnit);

		this.wGap = wGap;
	}

	private void fillMatrix(int k) {

		alignmentMatrix = new DiagonalTableStrip<>(textA.length + 1, textB.length + 1, k);

		// initialize first row and column
		for (int i = 0; i <= alignmentMatrix.getMaxRowIndex(0); i++) {
			alignmentMatrix.set(i, 0, i*wGap);
		}
		for (int j = 0; j <= alignmentMatrix.getMaxColumnIndex(0); j++) {
			alignmentMatrix.set(0, j, j*wGap);
		}

		for (int i = 1; i <= textA.length; i++) {
			for (int j = Math.max(1, alignmentMatrix.getMinColumnIndex(i)); j <= alignmentMatrix.getMaxColumnIndex(i); j++) {
				int costAlign = alignmentMatrix.get(i-1, j-1) + weight(i, j);
				int costGapB = Integer.MAX_VALUE;
				if (alignmentMatrix.checkCoordinates(i, j-1))
					costGapB = alignmentMatrix.get(i, j-1) + wGap;
				int costGapA = Integer.MAX_VALUE;
				if (alignmentMatrix.checkCoordinates(i-1, j))
					costGapA = alignmentMatrix.get(i-1, j) + wGap;
				alignmentMatrix.set(i, j, Math.min(Math.min(costAlign, costGapB), costGapA));
			}
		}

		if (alignmentMatrix.checkCoordinates(textA.length, textB.length) &&	alignmentMatrix.get(textA.length, textB.length) <= k) {
			this.validTable = true;
		}
	}

	class BacktrackPath {

		private final String path1;
		private final String path2;

		public BacktrackPath(String path1, String path2) {
			this.path1 = path1;
			this.path2 = path2;
		}

		public BacktrackPath(BacktrackPath old_path, char add1, char add2) {
			this.path1 = old_path.getPathList().get(0) + add1;
			this.path2 = old_path.getPathList().get(1) + add2;
		}

		public List<String> getPathList() {
			List<String> ret = new ArrayList<>(2);
			ret.add(0, path1);
			ret.add(1, path2);
			return ret;
		}

		@Override
		public String toString() {
			return this.path1 + "\n" + this.path2;
		}
	}

	private String[] reverseStrings(List<String> x) {
		String[] t = new String[x.size()];
		for (int i = 0; i < t.length; i++) {
			t[i] = new StringBuilder(x.get(i)).reverse().toString();
		}
		return t;
	}

	private void addPathstoCell(Point cell, List<BacktrackPath> paths,
			char a, char b,
			Map<Point, List<BacktrackPath>> current_paths) {
		if (!current_paths.containsKey(cell)) {
			current_paths.put(cell, new LinkedList<>());
		}
		for	(BacktrackPath p: paths) {
			current_paths.get(cell).add(new BacktrackPath(p, a, b));
		}
	}

	private List<Point> addPathsFromCell(Point current_cell, Map<Point, List<BacktrackPath>> current_paths) {
		int i = current_cell.x;
		int j = current_cell.y;

		List<Point> ret = new ArrayList<>();

		if (!this.alignmentMatrix.checkCoordinates(current_cell.x, current_cell.y))
			return ret;

		List<BacktrackPath> paths_to_current_cell = current_paths.get(current_cell);
		if (paths_to_current_cell != null) {

			// if one meta-char
			if ((i > 0 && metaChars.contains(textA[i-1])) || (j > 0 && metaChars.contains(textB[j-1]))) {
				// if both opening: align
				if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j) && openUnit == textA[i-1]) {
					Point next_cell = new Point(i-1, j-1);
					ret.add(next_cell);
					this.addPathstoCell(next_cell,	paths_to_current_cell, textA[i-1], textB[j-1], current_paths);
				}
				// if only one is opening: align the other
				else if (i > 0 && j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && openUnit == textA[i-1] && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
					Point next_cell = new Point(i, j-1);
					ret.add(next_cell);
					this.addPathstoCell(next_cell,	paths_to_current_cell, alignChar, textB[j-1], current_paths);
				}
				else if (i > 0 && j > 0 && alignmentMatrix.checkCoordinates(i-1, j) && openUnit == textB[j-1] && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
					Point next_cell = new Point(i-1, j);
					ret.add(next_cell);
					this.addPathstoCell(next_cell,	paths_to_current_cell, textA[i-1], alignChar, current_paths);
				}
				// else - try all possible alignments
				else {
					// the order is relevant as it determines the order in which the cells are processed
					if (j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
						Point next_cell = new Point(i, j-1);
						ret.add(next_cell);
						this.addPathstoCell(next_cell,	paths_to_current_cell, alignChar, textB[j-1], current_paths);
					}
					if (i > 0 && alignmentMatrix.checkCoordinates(i-1, j) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
						Point next_cell = new Point(i-1, j);
						ret.add(next_cell);
						this.addPathstoCell(next_cell,	paths_to_current_cell, textA[i-1], alignChar, current_paths);
					}
					if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j)) {
						Point next_cell = new Point(i-1, j-1);
						ret.add(next_cell);
						this.addPathstoCell(next_cell,	paths_to_current_cell, textA[i-1], textB[j-1], current_paths);
					}
				}
			}
			// otherwise: align if possible, or add gap in first sequence or in second sequence
			else if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j)) {
				Point next_cell = new Point(i-1, j-1);
				ret.add(next_cell);
				this.addPathstoCell(next_cell,	paths_to_current_cell, textA[i-1], textB[j-1], current_paths);
			}
			else if (j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
				Point next_cell = new Point(i, j-1);
				ret.add(next_cell);
				this.addPathstoCell(next_cell,	paths_to_current_cell, alignChar, textB[j-1], current_paths);
			}
			else if (i > 0 && alignmentMatrix.checkCoordinates(i-1, j) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
				Point next_cell = new Point(i-1, j);
				ret.add(next_cell);
				this.addPathstoCell(next_cell,	paths_to_current_cell, textA[i-1], alignChar, current_paths);
			}
		}
		return ret;
	}

	private void backtrack() {

		int i = textA.length;
		int j = textB.length;

		Map<Point, List<BacktrackPath>> current_paths = new HashMap<>();
		{
			List<BacktrackPath> initial = new ArrayList<>(1);
			initial.add(new BacktrackPath("", ""));
			current_paths.put(new Point(i, j), initial);
		}

		// iterate over the cells in the table that are on valid paths
		// the list of valid cells is updated by addPaths
		List<Point> next_cells = new LinkedList<Point>();
		next_cells.add(new Point(i, j));
		while(!next_cells.get(0).equals(new Point(0,0))) {
			Point curr_cell = next_cells.remove(0);
			next_cells.addAll(this.addPathsFromCell(curr_cell, current_paths));
			current_paths.remove(curr_cell);
		}

		this.alignments = current_paths.get(new Point(0,0))
				.stream()
				.map(p -> this.reverseStrings(p.getPathList()))
				.collect(Collectors.toList());

	}

	private int weight(int i, int j) {
		if (textA[i - 1] == textB[j - 1]) {
			return 0;
		}
		else { 
			// only allow gaps!
			return wGap*2+1;
		}
	}

	private void computeAlignments() {

		// try increasing the upper bound on the distance between the texts
		// until the upper bound is >= the real distance
		// start with minimal value that allows a full table
		int k = Math.max(1, Math.abs(textA.length - textB.length));
		while (!this.validTable) {
			this.fillMatrix(k);
			k = k*2;
		}

		this.backtrack();
	}

	@Override
	public List<String[]> getAlignments() {
		if (alignments == null) {
			this.computeAlignments();
		}
		return alignments;
	}

	@Override
	public int getInsertions() {
		if (alignments == null) {
			this.computeAlignments();
		}

		int i = textA.length;
		int j = textB.length;

		int ins = 0;

		while (i > 0 || j > 0) {
			if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j)) {
				i = i-1;
				j = j-1;
			}
			else if (j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
				j = j-1;
				ins = ins+1;
			}
			else if (i > 0 && alignmentMatrix.checkCoordinates(i-1, j) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
				i = i-1;
			}
		}
		return ins;

	}

	@Override
	public int getDeletions() {
		if (alignments == null) {
			this.computeAlignments();
		}
		int i = textA.length;
		int j = textB.length;

		int del = 0;

		while (i > 0 || j > 0) {
			if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j)) {
				i = i-1;
				j = j-1;
			}
			else if (j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
				j = j-1;
			}
			else if (i > 0 && alignmentMatrix.checkCoordinates(i-1, j) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
				i = i-1;
				del = del+1;
			}
		}
		return del;
	}

	@Override
	public int getSubstitutions() {
		// the cost are set such that subs are not allowed!
		return 0;
	}

	@Override
	public int getLength() {
		if (alignments == null) {
			this.computeAlignments();
		}
		return alignments.get(0)[0].length();
	}


}

// a class that stores only a diagonal strip of a table
// the cells are referenced as in the whole table
class DiagonalTableStrip<T> {

	T[][] table;
	final int columns;
	final int k;

	public DiagonalTableStrip(int rows, int columns, int k) {

		@SuppressWarnings("unchecked")
		T[][] t = (T[][]) new Object[rows][];

		// create the rows
		for (int i = 0; i < rows; i++) {
			// calculate the length of the row
			// 1. the cell on the main diagonal of the table
			int row_length = 1;
			// 2. the number of cells left of the main diagonal
			row_length += Math.min(i, k);
			// 3. the number of cells right of the main diagonal
			if (i+k <= columns)
				row_length += k;
			else
				row_length += Math.max(0, columns - i);
			@SuppressWarnings("unchecked")
			T[] c = (T[]) new Object[row_length];
			t[i] = c;
		}

		this.table = t;
		this.columns = columns;
		this.k = k;
	}

	// check if the coordinates are part of the stored strip
	public boolean checkCoordinates(int row, int col) {

		if (row >= this.table.length)
			return false;
		if (col >= columns)
			return false;
		if (row < 0 || col < 0)
			return false;

		if (Math.abs(row-col) <= this.k)
			return true;
		else
			return false;
	}

	public int getMaxColumnIndex(int row) {
		return Math.min(row+k, columns-1);
	}
	public int getMinColumnIndex(int row) {
		return Math.max(0, row-k);
	}
	public int getMaxRowIndex(int column) {
		return Math.min(column+k, this.table.length-1);
	}

	private int getOffset(int row) {
		return Math.max(0, row-k);
	}

	public T get(int row, int col) {

		if (!this.checkCoordinates(row, col))
			throw new IndexOutOfBoundsException("(" + row + "," + col + ") is not part of the Table strip.");

		return this.table[row][col-this.getOffset(row)];
	}

	public void set(int row, int col, T value) {

		if (!this.checkCoordinates(row, col))
			throw new IndexOutOfBoundsException("(" + row + "," + col + ") is not part of the Table strip.");

		this.table[row][col-this.getOffset(row)] = value;
	}
}
