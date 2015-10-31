import java.util.Arrays;

import com.intrbiz.bergamot.check.jmx.JMXCheckContext;
import com.intrbiz.bergamot.check.jmx.JMXChecker;
import com.intrbiz.bergamot.check.jmx.JMXMBean;
import com.intrbiz.bergamot.check.jmx.JMXMBeanAttribute;
import com.intrbiz.bergamot.check.jmx.JMXMBeanOperation;

public class JMXExample
{
    public static void main(String[] args)
    {
        // the checker
        JMXChecker checker = new JMXChecker();
        // context
        JMXCheckContext context = checker.createContext();
        // connect
        context.connect("127.0.0.1", 5010, (con) -> {
            for (JMXMBean bean : con.getMBeans())
            {
                System.out.println("MBean: " + bean.getName());
            }
            JMXMBean memBean = con.getMBean("java.lang:type=Memory");
            for (JMXMBeanAttribute attr : memBean.getAttributes())
            {
                System.out.println("Attribute: " + attr.getName() + " " + attr.getType());
                System.out.println("  -> " + attr.getValue());
            }
            for (JMXMBeanOperation oper : memBean.getOperations())
            {
                System.out.println("Operation: " + oper.getName() + "(" + Arrays.asList(oper.getSignature()).toString() + ")");
                if (oper.getParameters().isEmpty()) System.out.println("  -> " + oper.invoke());
            }
        });
    }
}
