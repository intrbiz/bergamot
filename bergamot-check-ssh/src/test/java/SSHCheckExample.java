import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.intrbiz.bergamot.check.ssh.ExecStat;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.check.ssh.SSHChecker;

public class SSHCheckExample
{
    public static void main(String[] args) throws Exception
    {
        SSHChecker checker = new SSHChecker();
        SSHCheckContext context = checker.createContext();
        // setup id
        context.addIdentity(new String(readFile(new File("/home/cellis/.ssh/id_rsa"))), new String(readFile(new File("/home/cellis/.ssh/id_rsa.pub"))));
        // connect
        context.connect("cellis", "127.0.0.1", 22, (session) -> {
            ExecStat stat;
            System.out.println("Connected");
            //
            stat = session.exec("/usr/bin/echo \"Test\"");
            System.out.println("Exit: " + stat.getExit());
            System.out.println(stat.getStdOut());
            //
            stat = session.exec("/usr/bin/w");
            System.out.println("Exit: " + stat.getExit());
            System.out.println(stat.getStdOut());
            //
            stat = session.exec("/usr/bin/cat /proc/cpuinfo");
            System.out.println("Exit: " + stat.getExit());
            System.out.println(stat.getStdOut());
        });
        // exit
        System.exit(0);
    }
    
    private static byte[] readFile(File file) throws IOException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            try (FileInputStream in = new FileInputStream(file))
            {
                byte[] buf = new byte[1024];
                int r;
                while ((r = in.read(buf)) != -1)
                {
                    baos.write(buf, 0, r);
                }
            }
            return baos.toByteArray();
        }
    }
}
