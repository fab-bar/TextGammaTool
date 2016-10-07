package corpus.merge.textalignment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class PairwiseDPTextAlignmentTest {

	@Test
	public void testGetAlignments() {
		char[] source = new char[5];
		source[0] = 't';
		source[1] = 'h';
		source[2] = 'e';
		source[3] = 'i';
		source[4] = 'r';
		char[] target = new char[5];
		target[0] = 't';
		target[1] = 'h';
		target[2] = 'e';
		target[3] = 'r';
		target[4] = 'e';
		TextAlignment ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		List<String[]> alignments = ta.getAlignments();
		assertEquals(1, alignments.size());

		source = new char[10];
		source[0] = '{';
		source[1] = 'a';
		source[2] = '}';
		source[3] = '{';
		source[4] = 'b';
		source[5] = 'b';
		source[6] = 'c';
		source[7] = 'c';
		source[8] = 'c';
		source[9] = '}';
		target = new char[10];
		target[0] = '{';
		target[1] = 'a';
		target[2] = 'b';
		target[3] = 'b';
		target[4] = '}';
		target[5] = '{';
		target[6] = 'c';
		target[7] = 'c';
		target[8] = 'c';
		target[9] = '}';

		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');
		alignments = ta.getAlignments();
		// alignment method excludes alignments where units are not aligned
		// if an optimal alignment exists where units are aligned.
		// this is returned:
		//{a--}{bbccc}
		//{abb}{--ccc}
		//this is excluded:
		//{a}{bb--ccc}
		//{a--bb}{ccc}

		assertEquals(1, alignments.size());

		source = new char[9];
		source[0] = '{';
		source[1] = 'a';
		source[2] = '}';
		source[3] = '{';
		source[4] = 'b';
		source[5] = 'c';
		source[6] = 'c';
		source[7] = 'c';
		source[8] = '}';
		target = new char[9];
		target[0] = '{';
		target[1] = 'a';
		target[2] = 'b';
		target[3] = '}';
		target[4] = '{';
		target[5] = 'c';
		target[6] = 'c';
		target[7] = 'c';
		target[8] = '}';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		alignments = ta.getAlignments();
		assertEquals(1, alignments.size());

		// Test example with many optimal alignments
		// alignment method only returns alignments with aligned tokens
		// (i.e. 4 in the example below)
		source = new char[]{'{', 't', '}', '{', 't', '}', '{', 't', '}', '{', 't', '}'};
		target = new char[]{'{', 'a', 'a', 'a', 'a', '}'};

		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');
		alignments = ta.getAlignments();
		assertEquals(4, alignments.size());

		ta = new PairwiseDPTextAlignment(target, source, '-', '{', '}');
		alignments = ta.getAlignments();
		assertEquals(4, alignments.size());

	}

	@Test
	public void testGetInsertions() {
		char[] source = {'T', 'e'};
		char[] target = {'T'};
		TextAlignment ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(0, ta.getInsertions());

		source = new char[3];
		source[0] = 'T';
		source[1] = 'e';
		source[2] = 's';
		target = new char[4];
		target[0] = 'T';
		target[1] = 'e';
		target[2] = 's';
		target[3] = 't';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(1, ta.getInsertions());

		source = new char[5];
		source[0] = 't';
		source[1] = 'h';
		source[2] = 'e';
		source[3] = 'i';
		source[4] = 'r';
		target = new char[5];
		target[0] = 't';
		target[1] = 'h';
		target[2] = 'e';
		target[3] = 'r';
		target[4] = 'e';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(1, ta.getInsertions());

	}

	@Test
	public void testGetDeletions() {
		char[] source = {'T', 'e'};
		char[] target = {'T'};
		TextAlignment ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(1, ta.getDeletions());

		source = new char[3];
		source[0] = 'T';
		source[1] = 'e';
		source[2] = 's';
		target = new char[4];
		target[0] = 'T';
		target[1] = 'e';
		target[2] = 's';
		target[3] = 't';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(0, ta.getDeletions());

		source = new char[5];
		source[0] = 't';
		source[1] = 'h';
		source[2] = 'e';
		source[3] = 'i';
		source[4] = 'r';
		target = new char[5];
		target[0] = 't';
		target[1] = 'h';
		target[2] = 'e';
		target[3] = 'r';
		target[4] = 'e';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(1, ta.getDeletions());
	}

	@Test
	public void testGetSubstitutions() {

		// substitutions are not allowed - always 0

		char[] source = {'T', 'e'};
		char[] target = {'T'};
		TextAlignment ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(0, ta.getSubstitutions());

		source = new char[3];
		source[0] = 'T';
		source[1] = 'e';
		source[2] = 's';
		target = new char[4];
		target[0] = 'T';
		target[1] = 'e';
		target[2] = 'r';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(0, ta.getSubstitutions());

		source = new char[5];
		source[0] = 't';
		source[1] = 'h';
		source[2] = 'e';
		source[3] = 'i';
		source[4] = 'r';
		target = new char[5];
		target[0] = 't';
		target[1] = 'h';
		target[2] = 'e';
		target[3] = 'r';
		target[4] = 'e';
		ta = new PairwiseDPTextAlignment(source, target, '-', '{', '}');

		assertEquals(0, ta.getSubstitutions());
	}
}
