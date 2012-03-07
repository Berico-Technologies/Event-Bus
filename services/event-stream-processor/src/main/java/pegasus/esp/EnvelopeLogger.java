package pegasus.esp;

import java.io.File;
import java.util.Date;

import org.joda.time.DateTime;

import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;

public class EnvelopeLogger extends EventMonitor {

    private String logdir = null;
    private String cond = null;
    private String logFile = null;
    private String jsonFile = null;


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
            String msg = "Logging started into " + logdir + " at " + new Date() + "\n";
            System.out.println(msg);
            fprint(logFile, msg);
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

    @Override
    public InferredEvent receive(EventBean eventBean) {
        if (logdir != null) {
            Envelope env = (Envelope) eventBean.get("resp");
            fprint(logFile, EnvelopeUtils.envelopeToReadableJson(env));
            fprint(jsonFile, EnvelopeUtils.toJson(env));
        }
        return null;
    }

    @Override
    public void registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, getPattern(), this);
        String name = this.getClass().getSimpleName();
        esp.flog("Registering " + name + " with " + esp +
                ", writing to " + logdir + ", pattern=" + getPattern());
    }

    private String getPattern() {
        String where = "";
        if (cond != null && cond.length() > 0) {
            where = " where %s".format(cond);
        }
        return "select resp from Envelope as resp" + where;
    }

    @Override
    public String getLabel() {
        return this.getClass().getSimpleName() + "(" + logdir + ")";
    }
}
