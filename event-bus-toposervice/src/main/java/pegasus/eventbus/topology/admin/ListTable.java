package pegasus.eventbus.topology.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class ListTable {

    private Dictionary<String, List<String>> columns = new Hashtable<String, List<String>>();
    private List<List<String>>               rows    = new ArrayList<List<String>>();

    public ListTable(String rawTableString) {
        parseRawTableString(rawTableString);
    }

    private void parseRawTableString(String rawTableString) {
        ArrayList<String> rawRows = new ArrayList<String>(Arrays.asList(rawTableString.split("\\n")));

        // chop top header boundary line
        rawRows.remove(0);
        // chop the column header titles
        String titleRow = rawRows.remove(0);
        // chop bottom header boundary line
        rawRows.remove(0);
        // init table keys with the column titles
        String[] titles = titleRow.split("|");
        for (int i = 0; i < titles.length; i++) {
            titles[i] = titles[i].trim();
            columns.put(titles[i], new ArrayList<String>());
        }
        // chop bottom table boundary line
        rawRows.remove(rawRows.size() - 1);
        // split and store each row into table columns
        for (String fieldRow : rawRows) {
            rows.add(new ArrayList<String>());
            String[] fields = fieldRow.split("|");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = fields[i].trim();
                columns.get(titles[i]).add(fields[i]);
                rows.get(rows.size() - 1).add(fields[i]);
            }
        }
    }

    public Collection<String> getRow(int index) {
        return rows.get(index);
    }

    public Collection<String> getColumn(String title) {
        return columns.get(title);
    }

    public String get(String title, int index) {
        return columns.get(title).get(index);
    }

}
