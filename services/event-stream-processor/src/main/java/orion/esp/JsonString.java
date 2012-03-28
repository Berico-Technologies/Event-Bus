package orion.esp;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Generate a readable JSON-like display for objects.
 *
 * @author israel
 *
 */
public class JsonString {

	private static final String NEWLINE = "\n";
	private static final String DOUBLE_QUOTE = "\"";

	int level = 0;
	StringBuffer sb = new StringBuffer();

	public JsonString() {
	}

	private void indent() {
		for (int i = 0; i < level; i++) {
			sb.append(" ");
		}
	}

	public JsonString start() {
		indent();
		sb.append("{" + NEWLINE);
		level += 2;
		return this;
	}

	public JsonString end() {
		level -= 2;
		indent();
		sb.append("}," + NEWLINE);
		return this;
	}

	public String toString() {
		return sb.toString();
	}

	public static String quote(String str) {
		return DOUBLE_QUOTE + str + DOUBLE_QUOTE;
	}

	private void startLine(String name) {
		indent();
		sb.append(name + ": " );
	}

	private void endLine() {
		sb.append("," + NEWLINE);
	}

    public JsonString add(String name, String val) {
        if (val != null) {
            String outstr = quote(val);
            if (name.toLowerCase().contains(":time")) {
                try {
                    outstr = timeString(new Date(Long.parseLong(val)));
                } catch (NumberFormatException e) {
                    // ignore errors; revert to regular treatment
                }
            }
            addline(name, outstr);
        }
        return this;
    }

    private void addline(String name, String outstr) {
        startLine(name);
        sb.append(outstr);
        endLine();
    }

	public JsonString add(String name, Map<String, String> headers) {
		startLine(name);
		start();
		for (String hdr : headers.keySet()) {
			add(hdr, headers.get(hdr));
		}
		end();
		return this;
	}

	public JsonString add(String name, int val) {
		return add(name,"" + val);
	}

	public JsonString add(String name, byte[] body) {
	    return add(name,"" + body);
	}

	public JsonString add(String name, Date timestamp) {
		if (timestamp != null) {
		    addline(name, timeString(timestamp));
		}
		return this;
	}

	private String timeString(Date timestamp) {
		return String.format("%d /* %s */", timestamp.getTime(), timestamp.toString());
	}

	public JsonString add(String name, UUID id) {
		if (id != null) {
			add(name, id.toString());
		}
		return this;
	}
}