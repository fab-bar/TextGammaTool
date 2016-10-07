package corpus.merge.textalignment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// align the annotations of two texts using dynamic programming
public class PairwiseDPTextAlignment extends TextAlignment {

	char[] textA;
	char[] textB;
	int wGap;

	int[][] alignmentMatrix;
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

	private void fillMatrix() {

		alignmentMatrix = new int[textA.length + 1][textB.length + 1];

		// initialize first row and column
		for (int i = 0; i <= textA.length; i++) {
			alignmentMatrix[i][0] = i*wGap;
		}
		for (int j = 0; j <= textB.length; j++) {
			alignmentMatrix[0][j] = j*wGap;
		}

		for (int i = 1; i <= textA.length; i++) {
			for (int j = 1; j <= textB.length; j++) {
				int costAlign = alignmentMatrix[i-1][j-1] + weight(i, j);
				int costGapB = alignmentMatrix[i][j-1] + wGap;
				int costGapA = alignmentMatrix[i-1][j] + wGap;
				alignmentMatrix[i][j] = Math.min(Math.min(costAlign, costGapB), costGapA);
			}
		}
	}

	private void backtrack_step(int i, int j, 
			String alignmentSeqA, String alignmentSeqB, List<String[]> alignments) {

		if (i > 0 || j > 0) {

			// if one meta-char
			if ((i > 0 && metaChars.contains(textA[i-1])) || (j > 0 && metaChars.contains(textB[j-1]))) {
				// if both opening: align
				if (i > 0 && j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j-1] + weight(i, j) && openUnit == textA[i-1]) {
					backtrack_step(i-1, j-1, alignmentSeqA + textA[i-1], alignmentSeqB + textB[j-1], alignments);
				}
				// if only one is opening: align the other
				else if (i > 0 && j > 0 && openUnit == textA[i-1] && alignmentMatrix[i][j] == alignmentMatrix[i][j-1] + wGap) {
					backtrack_step(i, j-1, alignmentSeqA + alignChar, alignmentSeqB + textB[j-1], alignments);
				}
				else if (i > 0 && j > 0 && openUnit == textB[j-1] && alignmentMatrix[i][j] == alignmentMatrix[i-1][j] + wGap) {
					backtrack_step(i-1, j, alignmentSeqA + textA[i-1], alignmentSeqB + alignChar, alignments);
				}
				// else - try all possible alignments
				else {
					if (i > 0 && j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j-1] + weight(i, j)) {
						backtrack_step(i-1, j-1, alignmentSeqA + textA[i-1], alignmentSeqB + textB[j-1], alignments);
					}
					if (j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i][j-1] + wGap) {
						backtrack_step(i, j-1, alignmentSeqA + alignChar, alignmentSeqB + textB[j-1], alignments);
					}
					if (i > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j] + wGap) {
						backtrack_step(i-1, j, alignmentSeqA + textA[i-1], alignmentSeqB + alignChar, alignments);
					}
				}
			}
			// otherwise: align if possible, or add gap in first sequence or in second sequence
			else if (i > 0 && j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j-1] + weight(i, j)) {
				backtrack_step(i-1, j-1, alignmentSeqA + textA[i-1], alignmentSeqB + textB[j-1], alignments);
			}
			else if (j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i][j-1] + wGap) {
				backtrack_step(i, j-1, alignmentSeqA + alignChar, alignmentSeqB + textB[j-1], alignments);
			}
			else if (i > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j] + wGap) {
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
		this.fillMatrix();
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
			if (i > 0 && j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j-1] + weight(i, j)) {
				i = i-1;
				j = j-1;
			}
			else if (j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i][j-1] + wGap) {
				j = j-1;
				ins = ins+1;
			}
			else if (i > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j] + wGap) {
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
			if (i > 0 && j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j-1] + weight(i, j)) {
				i = i-1;
				j = j-1;
			}
			else if (j > 0 && alignmentMatrix[i][j] == alignmentMatrix[i][j-1] + wGap) {
				j = j-1;
			}
			else if (i > 0 && alignmentMatrix[i][j] == alignmentMatrix[i-1][j] + wGap) {
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
