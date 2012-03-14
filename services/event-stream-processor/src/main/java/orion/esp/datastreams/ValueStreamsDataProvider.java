package orion.esp.datastreams;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orion.esp.publish.DataProvider;

public class ValueStreamsDataProvider implements DataProvider {

    ValueStreams streams;
    String timecategory;

    public ValueStreamsDataProvider(ValueStreams streams, String timecategory) {
        super();
        this.streams = streams;
        this.timecategory = timecategory;
    }

//    public static void displayOneTimeCategory(ValueStreams streams, String timecategory) {
//        System.out.println(streams.getName() + ":");
//        System.out.println(String.format("\nTopN Label: %s", timecategory));
//        for (String item : streams.getValues()) {
//            ActiveRange activeRange = streams.getActiveRange(timecategory, item);
//            int total = activeRange.getTotal();
//            int trend = activeRange.getTrend();
//            String desc = activeRange.getTrendDesc();
//            System.out.println(String.format("  Trend Label: %s", item));
//            System.out.println(String.format("  Trend Value: %s", total));
//            System.out.println(String.format("  Trend info: %s", desc));
//            System.out.println(String.format("  Trend change: %s", trend));
//            System.out.println();
//        }
//        System.out.println();
//    }

    @Override
    public Map<String, Object> getData() {

        Map<String, Object> topNMetric =  new HashMap<String, Object>();
        topNMetric.put(DataProvider.LABEL, timecategory);
        List<Map<String, Object>> metrics = new ArrayList<Map<String, Object>>();
        for (String item : streams.getValues()) {
            ActiveRange activeRange = streams.getActiveRange(timecategory, item);
            int total = activeRange.getTotal();
            int trend = activeRange.getTrend();
            String desc = activeRange.getTrendDesc();
            Map<String, Object> trendMetric = new HashMap<String, Object>();
            trendMetric.put(DataProvider.LABEL, item);
            trendMetric.put(DataProvider.VALUE, total);
            trendMetric.put(DataProvider.TREND, trend);
            trendMetric.put(DataProvider.INFO, desc);
            metrics.add(trendMetric);
        }
        topNMetric.put(DataProvider.METRICS, metrics);
        return topNMetric;
    }

}
