import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.model.Site;
import com.intrbiz.data.DataManager;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.lamplighter.model.StoredDoubleGaugeReading;
import com.intrbiz.util.pool.database.DatabasePool;


public class ReadingsExample
{
    public static void main(String[] args) throws Exception
    {
       // database
       DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url("jdbc:postgresql://127.0.0.1:5432/bergamot").username("bergamot").password("bergamot").build());
       //
       LamplighterDB db = LamplighterDB.connect();
       System.out.println("Lamplighter Version: " + db.getVersion());
       //
       UUID siteId    = Site.randomSiteId();
       UUID checkId   = Site.randomId(siteId);
       UUID readingId = Site.randomId(siteId);
       //
       db.setupSiteReadings(siteId);
       db.setupDoubleGaugeReading(siteId, readingId, checkId, "test", "Test", "", "");
       System.out.println("Setup reading: " + siteId + "::" + readingId);
       //
       db.execute(() -> {
           long epoch = 1420070400000L;
           long start = System.currentTimeMillis();
           for (int i = 0; i < (60 * 24 * 365); i++)
           {
               db.storeDoubleGaugeReading(new StoredDoubleGaugeReading(siteId, readingId, new Timestamp(epoch), (double) i, 80D, 90D, 1D, 100D));
               epoch += 60_000L;
           }
           long end = System.currentTimeMillis();
           System.out.println("Added " + (60 * 24 * 365) + " reading to " + siteId + "::" + readingId + " in " + (end - start) + " ms");
       });
       //
       for (StoredDoubleGaugeReading reading : db.getLatestDoubleGaugeReadings(siteId, readingId, 100))
       {
           System.out.println(reading.getCollectedAt() + " => " + reading.getValue());
       }
    }
}
