package orion.esp.datastreams;

import static org.junit.Assert.*;

import org.junit.Test;

import orion.esp.datastreams.Stream;
import orion.esp.datastreams.TimeEntry;
import orion.esp.datastreams.ValueStreams;

public class StreamTest {

	private int itmno;

	@Test
	public void testOrderedStream() {
		Stream st = new Stream("Test Data");

		assertEquals(0, st.size());

		st.addInfo(10, 8);
		assertEquals(1, st.size());

		st.addInfo(100, 3);
		assertEquals(2, st.size());

		st.addInfo(5700, 42);
		assertEquals(3, st.size());

		itmno = 0;
		assertStreamItemMatches(st.get(itmno++), 10, 8);
		assertStreamItemMatches(st.get(itmno++), 100, 3);
		assertStreamItemMatches(st.get(itmno++), 5700, 42);
	}

	@Test
	public void testUnorderedStream() {
		Stream st = new Stream("Test Data");

		assertEquals(0, st.size());

		st.addInfo(100, 3);
		assertEquals(1, st.size());

		st.addInfo(5700, 42);
		assertEquals(2, st.size());

		st.addInfo(10, 8);
		assertEquals(3, st.size());

		st.addInfo(150, 97);
		assertEquals(4, st.size());


		itmno = 0;
		assertStreamItemMatches(st.get(itmno++), 100, 3);
		assertStreamItemMatches(st.get(itmno++), 5700, 42);
		// timestamp changed to latest (5700) to keep it sequential
		assertStreamItemMatches(st.get(itmno++), 5700, 8);
		// timestamp changed to latest (5700) to keep it sequential
		assertStreamItemMatches(st.get(itmno++), 5700, 97);
	}

	@Test
	public void testUnorderedStreamWithActiveRanges() {
		Stream st = new Stream("Test Data");
//		if (true) return;


		st.addActiveRange(ValueStreams.minutes(1), "category", "item");
		st.addActiveRange(ValueStreams.minutes(5), "category", "item");
		st.addActiveRange(ValueStreams.hours(1), "category", "item");
		st.addActiveRange(ValueStreams.days(1), "category", "item");

		assertEquals(0, st.size());

		st.addInfo(100, 3);
		assertEquals(1, st.size());

		st.addInfo(5432, 42);
		assertEquals(2, st.size());

		st.addInfo(10, 8);
		assertEquals(3, st.size());

		st.addInfo(150, 97);
		assertEquals(4, st.size());


		itmno = 0;
		assertStreamItemMatches(st.get(itmno++), 100, 3);
		assertStreamItemMatches(st.get(itmno++), 5432, 42);
		// timestamp changed to latest (5700) to keep it sequential
		assertStreamItemMatches(st.get(itmno++), 5432, 8);
		// timestamp changed to latest (5700) to keep it sequential
		assertStreamItemMatches(st.get(itmno++), 5432, 97);
	}

	private void assertStreamItemMatches(TimeEntry timeEntry, long expectedtimestamp, int expectedvalue) {
		assertEquals(expectedtimestamp, timeEntry.getTimestamp());
		assertEquals(expectedvalue, timeEntry.getValue());
	}
}

