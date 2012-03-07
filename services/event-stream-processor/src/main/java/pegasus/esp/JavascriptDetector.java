package pegasus.esp;

import java.util.Collection;
import java.util.HashSet;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;

public class JavascriptDetector extends EventMonitor {

	private static final String language = "js";
	private static final ScriptEngineManager scriptEngineFactory = new ScriptEngineManager();
	private ScriptEngine engine;
	private String monitorDef;
	private Context cx;
	private Scriptable scope;
	private NativeObject eventMonitor = null;

	public JavascriptDetector(String monitorDef) throws ScriptException {
		super();
		setupEngine();
		this.monitorDef = monitorDef;
		Object evalresult = cx.evaluateString(scope, monitorDef, "<cmd>", 1, null);
		if (evalresult == null) {
			throw new RuntimeException("No monitor created from: " + monitorDef);
		} else if (evalresult instanceof NativeObject) {
			eventMonitor = (NativeObject) evalresult;
		} else {
			throw new RuntimeException("unknown monitor of class " + evalresult.getClass().getCanonicalName() + " returned.");
		}
	}
	
    @Override
    public InferredEvent receive(EventBean eventBean) {
        Envelope env = (Envelope) eventBean.get("env");
		Object[] args = {env};
		InferredEvent ie = 
				(InferredEvent) unwrap(ScriptableObject.callMethod(eventMonitor, "receive", args));
        return ie;
    }

	private Object unwrap(Object obj) {
		if (obj instanceof NativeJavaObject) {
			NativeJavaObject res = (NativeJavaObject) obj;
			return res.unwrap();
		}
		
		if (obj != null) {
			System.err.println("Cannot handle Javascript result of type " + obj.getClass().getCanonicalName());
		}
		return null;
	}


	@Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, "select env from Envelope as env", this);

        // @todo = this needs to be integrated
        return new HashSet<Publisher>();
    }

	public void setupEngine() throws ScriptException {
		engine = scriptEngineFactory.getEngineByName(language);
		if (engine == null) {
			throw new RuntimeException("No language support available for " + language);
		}
		cx = Context.enter();
		scope = cx.initStandardObjects();
		Object wrappedOut = Context.javaToJS(System.out, scope);
		ScriptableObject.putProperty(scope, "out", wrappedOut);
	}
}

