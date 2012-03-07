package pegasus.esp.data;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class ValueStreamsTest {

	private static final String CATEGORY = "Political Campaign Actions";
    private static final String ITEM_1 = "Badmouth Opponent";
	private static final String ITEM_2 = "Dodge Questions";
	private static final String ITEM_3 = "Help People";
    private int itmno;

	@Test
	public void testUnorderedStreamWithActiveRanges() {
	    ValueStreams sts = new ValueStreams(CATEGORY);
//		if (true) return;

	    sts.addPeriod(ValueStreams.seconds(2));
		sts.addPeriod(ValueStreams.minutes(1));
		sts.addPeriod(ValueStreams.minutes(5));
		sts.addPeriod(ValueStreams.hours(1));
		sts.addPeriod(ValueStreams.days(1));

		assertEquals(0, sts.getValues().size());

		sts.addValue(ITEM_1, 10, 8);
		assertEquals(1, sts.getValues().size());
		assertEquals(1, sts.getStream(ITEM_1).size());

		sts.addValue(ITEM_1, 100, 3);
		assertEquals(1, sts.getValues().size());
		assertEquals(2, sts.getStream(ITEM_1).size());

		sts.addValue(ITEM_1, 150, 97);
		assertEquals(3, sts.getStream(ITEM_1).size());

        sts.addValue(ITEM_2, 3333, 33);
        assertEquals(2, sts.getValues().size());
        assertEquals(1, sts.getStream(ITEM_2).size());

		sts.addValue(ITEM_1, 4800, 6);
		assertEquals(4, sts.getStream(ITEM_1).size());

		sts.addValue(ITEM_1, 5700, 42);
		assertEquals(5, sts.getStream(ITEM_1).size());

        sts.addValue(ITEM_2, 6250, 219);
        assertEquals(2, sts.getValues().size());
        assertEquals(2, sts.getStream(ITEM_2).size());

        sts.addValue(ITEM_1, 7500, 113);
        assertEquals(6, sts.getStream(ITEM_1).size());

        sts.addValue(ITEM_3, 15, 2);
        assertEquals(1, sts.getStream(ITEM_3).size());


		itmno = 0;
		Stream stream = sts.getStream(ITEM_1);
		assertStreamItemMatches(stream.get(itmno++), 10, 8);
		assertStreamItemMatches(stream.get(itmno++), 100, 3);
		assertStreamItemMatches(stream.get(itmno++), 150, 97);
		assertStreamItemMatches(stream.get(itmno++), 4800, 6);
		assertStreamItemMatches(stream.get(itmno++), 5700, 42);

        itmno = 0;
        stream = sts.getStream(ITEM_2);
        assertStreamItemMatches(stream.get(itmno++), 3333, 33);
        assertStreamItemMatches(stream.get(itmno++), 6250, 219);

        Set<String> values = sts.getValues();
        assertEquals(3, values.size());
        assertTrue(values.contains(ITEM_1));
        assertTrue(values.contains(ITEM_2));
        assertTrue(values.contains(ITEM_3));

        List<ActiveRange> activeRanges = sts.getStream(ITEM_1).getActiveRanges();

        assertEquals(5, activeRanges.size());

        Set<String> rangeNames = sts.getActiveRanges();
        assertEquals(5, rangeNames.size());
        String[] expectedRanges = {
                "Political Campaign Actions over last 2 seconds",
                "Political Campaign Actions over last one minute",
                "Political Campaign Actions over last 5 minutes",
                "Political Campaign Actions over last one day",
                "Political Campaign Actions over last one hour",
        };
        for (String expected : expectedRanges) {
            assertTrue(rangeNames.contains(expected));
        }

        for (String range : expectedRanges) {
            for (String item : values) {
                System.out.println(range + "  ||  " + item);
                ActiveRange ar = sts.getActiveRange(range, item);
                System.out.println(ar);
                if (ar.getPrev() != null)
                    System.out.println("  " + ar.getPrev());
                System.out.println(
                        String.format("category = %s, item = %s, tot = %s, count = %s, trend = %s",
                                range, item, ar.getTotal(), ar.getAdjustedSize(), ar.getTrend()));
                System.out.println();
            }
        }
	}

	private void assertStreamItemMatches(TimeEntry timeEntry, long timestamp, int value) {
		assertEquals(timeEntry.getTimestamp(), timestamp);
		assertEquals(timeEntry.getValue(), value);
	}
}

