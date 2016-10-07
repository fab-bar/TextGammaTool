package corpus;

import static org.junit.Assert.*;

import org.junit.Test;

import corpus.TextUnit;
import corpus.Unit;

public class TextUnitTest {

	@Test
	public void testEqualsObject() {
		TextUnit tok1 = new TextUnit(null, 0, 1, null, "a");
		TextUnit tok2 = new TextUnit(null, 0, 1, null, "a");
		TextUnit tok3 = new TextUnit(null, 0, 1, null, null);
		
		assertTrue(tok1.equals(tok2));
		assertFalse(tok2.equals(tok3));
		
		Unit tok4 = new Unit(null, "textunit", 0, 1, null);
		
		assertFalse(tok1.equals(tok4));
		assertFalse(tok4.equals(tok1));
	}

}
