package orion.esp.monitors;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.joda.time.DateTime;

import orion.esp.EnvelopeUtils;
import orion.esp.EventMonitor;
import orion.esp.EventStreamProcessor;
import orion.esp.InferredEvent;
import orion.esp.publish.Publisher;

import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;

public class EnvelopeLogger extends EventMonitor {

    private String logdir = null;
    private String cond = null;
    private String logFile = null;
    private String jsonFile = null;

    public EnvelopeLogger() {
    }

    public EnvelopeLogger(String logdir) {
        setlogdir(logdir);
    }

    public String getlogdir() {
        return logdir;
    }

    public void setlogdir(String logdir) {
        if (setupDir(logdir)) {
            this.logdir = logdir;
            String base = new DateTime().toString("yyyy-MM-dd");
            this.logFile = logdir + "/" + base + ".log";
            this.jsonFile = logdir + "/" + base + ".json";
            String msg = "Logging started into " + logFile + " at " + new Date() + "\n";
            System.out.println(msg);
        } else {
            System.err.println("Logging could not start into " +
                    logdir + " at " + new Date() + "\n");
        }
    }

    private boolean setupDir(String logdir) {
        File file = new File(logdir);
        if (file.canWrite()) { return true; }
        return file.mkdirs();
    }

    public String getCond() {
        return cond;
    }

    public void setCond(String cond) {
        this.cond = cond;
    }

    /**
     *  Write debugging output to a file
     *
     * @param fname the file to be written to
     * @param data The debug string to be written
     **/
    public static void fprint(String fname, String data) {
      java.io.PrintWriter file;
      boolean append = true;
      try {
        file = new java.io.PrintWriter(new java.io.FileOutputStream(fname, append));
      } catch (Exception exc) {
        return;
      }
      file.print(data + "\n");
      file.close();
    }

    @Override
    public InferredEvent receive(EventBean eventBean) {
        if (logdir != null) {
            Envelope env = (Envelope) eventBean.get("resp");
            if (env.getEventType().startsWith("dashboard.server.metric")) return null;
            fprint(logFile, EnvelopeUtils.envelopeToReadableJson(env));
            fprint(jsonFile, EnvelopeUtils.toJson(env));
        }
        return null;
    }

    @Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, getPattern(), this);

        return null;
    }

    private String getPattern() {
        String where = "";
        if (cond != null && cond.length() > 0) {
            where = String.format(" where %s", cond);
        }
        return "select resp from Envelope as resp" + where;
    }

    @Override
    public String getLabel() {
        return this.getClass().getSimpleName() + "(" + logdir + ")";
    }
}
