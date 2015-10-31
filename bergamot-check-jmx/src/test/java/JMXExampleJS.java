import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.intrbiz.bergamot.check.jmx.JMXChecker;
import com.intrbiz.scripting.RestrictedScriptEngineManager;

public class JMXExampleJS
{
    public static void main(String[] args) throws Exception
    {
        JMXChecker checker = new JMXChecker();
        //
        ScriptEngineManager factory = new RestrictedScriptEngineManager();
        // setup the script engine
        ScriptEngine script = factory.getEngineByName("nashorn");
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("jmx", checker.createContext((t) -> {
            t.printStackTrace();
        }));
        script.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        // execute
        script.eval(
                "jmx.connect('127.0.0.1', 5010, function(con) {" +
                "  print('Connected');" +
                "  var mbeans = con.getMBeans();" +
                "  for (var i = 0; i < mbeans.length; i++) {" +
                "    print(mbeans[i].getName());" +
                "  }" +
                "  var memBean = con.getMBean('java.lang:type=Memory');" +
                "  for (var i = 0; i < memBean.getAttributes().length; i++) {" +
                "    print('Attr: ' + memBean.getAttributes()[i].getName() + ' :: ' + memBean.getAttributes()[i].getType());" +
                "    print('  -> ' + memBean.getAttributes()[i].getValue());" +
                "  }" +
                "  print(memBean.getAttributeValue('NonHeapMemoryUsage').committed);" +
                "  for (var i = 0; i < memBean.getOperations().length; i++) {" +
                "    print('Oper: ' + memBean.getOperations()[i].getName() + ' ' + memBean.getOperations()[i].getParameters().length);" +
                "    if (memBean.getOperations()[i].getParameters().length == 0) {" +
                "        print('  Invoke: ' + memBean.getOperations()[i].invoke());" +
                "    }" +
                "  }" +
                "});"
        );
    }
}
