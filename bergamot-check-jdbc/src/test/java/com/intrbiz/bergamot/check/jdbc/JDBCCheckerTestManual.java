package com.intrbiz.bergamot.check.jdbc;

public class JDBCCheckerTestManual
{
    public static void main(String[] args)
    {
        JDBCChecker checker = new JDBCChecker();
        JDBCCheckContext ctx = checker.createContext();
        //
        ctx.connect("jdbc:postgresql://127.0.0.1:5432/bergamot", "bergamot", "bergamot", (con) -> {
            // Simple get
            Integer res = con.query("SELECT 1", (rs) -> {
               return rs.first().map((r) -> r.getInt(1)).orElse(null); 
            });
            System.out.println("Got: " + res);
            //
            int cons = con.query(
              "SELECT datname, count(pid), sum(count(pid)) OVER () FROM pg_stat_activity GROUP BY 1",
              (rs) -> {
                  int total = 0;
                  while (rs.next())
                  {
                      System.out.println("Connections: " + rs.getString(1) + " => " + rs.getInt(2));
                      total = rs.getInt(3);
                  }
                  return total;
              }
            );
            System.out.println("Total: " + cons);
            //
            Integer conns = con.prepare("SELECT count(pid) FROM pg_stat_activity WHERE datname = ?")
             .bindString("bergamot")
             .query((rs) -> rs.first().map((r) -> r.getInt(1)).orElse(null));
            System.out.println("Cons: " + conns);
        });
        //
        System.exit(1);
    }
}
