package corpus.merge.textalignment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

	private void backtrack_step(int i, int j, 
			String alignmentSeqA, String alignmentSeqB, List<String[]> alignments) {

		if (i > 0 || j > 0) {

			// if one meta-char
			if ((i > 0 && metaChars.contains(textA[i-1])) || (j > 0 && metaChars.contains(textB[j-1]))) {
				// if both opening: align
				if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j) && openUnit == textA[i-1]) {
					backtrack_step(i-1, j-1, alignmentSeqA + textA[i-1], alignmentSeqB + textB[j-1], alignments);
				}
				// if only one is opening: align the other
				else if (i > 0 && j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && openUnit == textA[i-1] && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
					backtrack_step(i, j-1, alignmentSeqA + alignChar, alignmentSeqB + textB[j-1], alignments);
				}
				else if (i > 0 && j > 0 && alignmentMatrix.checkCoordinates(i-1, j) && openUnit == textB[j-1] && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
					backtrack_step(i-1, j, alignmentSeqA + textA[i-1], alignmentSeqB + alignChar, alignments);
				}
				// else - try all possible alignments
				else {
					if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j)) {
						backtrack_step(i-1, j-1, alignmentSeqA + textA[i-1], alignmentSeqB + textB[j-1], alignments);
					}
					if (j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
						backtrack_step(i, j-1, alignmentSeqA + alignChar, alignmentSeqB + textB[j-1], alignments);
					}
					if (i > 0 && alignmentMatrix.checkCoordinates(i-1, j) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
						backtrack_step(i-1, j, alignmentSeqA + textA[i-1], alignmentSeqB + alignChar, alignments);
					}
				}
			}
			// otherwise: align if possible, or add gap in first sequence or in second sequence
			else if (i > 0 && j > 0 && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j-1) + weight(i, j)) {
				backtrack_step(i-1, j-1, alignmentSeqA + textA[i-1], alignmentSeqB + textB[j-1], alignments);
			}
			else if (j > 0 && alignmentMatrix.checkCoordinates(i, j-1) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i, j-1) + wGap) {
				backtrack_step(i, j-1, alignmentSeqA + alignChar, alignmentSeqB + textB[j-1], alignments);
			}
			else if (i > 0 && alignmentMatrix.checkCoordinates(i-1, j) && alignmentMatrix.get(i, j) == alignmentMatrix.get(i-1, j) + wGap) {
				backtrack_step(i-1, j, alignmentSeqA + textA[i-1], alignmentSeqB + alignChar, alignments);
			}

		}
		else {
			String[] ret = { new StringBuilder(alignmentSeqA).reverse().toString(), 
					new StringBuilder(alignmentSeqB).reverse().toString()};
			alignments.add(ret);
		}
	}

	private void backtrack() {
		alignments = new LinkedList<String[]>();
		backtrack_step(textA.length, textB.length, "", "", alignments);
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
