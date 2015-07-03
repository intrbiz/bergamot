import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.model.Site;
import com.intrbiz.data.DataManager;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.lamplighter.model.StoredDoubleGaugeReading;
import com.intrbiz.lamplighter.model.StoredFloatGaugeReading;
import com.intrbiz.lamplighter.model.StoredIntGaugeReading;
import com.intrbiz.lamplighter.model.StoredLongGaugeReading;
import com.intrbiz.util.pool.database.DatabasePool;


public class ReadingsExample
{
    public static void main(String[] args) throws Exception
    {
       // database
       DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url("jdbc:postgresql://172.30.13.48:5432/bergamot").username("bergamot").password("bergamot").build());
       //
       LamplighterDB.install();
       //
       LamplighterDB db = LamplighterDB.connect();
       System.out.println("Lamplighter Version: " + db.getVersion());
       //
       UUID siteId    = Site.randomSiteId();
       UUID checkId   = Site.randomId(siteId);
       UUID readingIdD = Site.randomId(siteId);
       UUID readingIdL = Site.randomId(siteId);
       UUID readingIdI = Site.randomId(siteId);
       UUID readingIdF = Site.randomId(siteId);
       //
       db.setupSiteReadings(siteId);
       db.setupDoubleGaugeReading(siteId, readingIdD, checkId, "testd", "Test", "", "", 300_000L);
       db.setupLongGaugeReading(siteId, readingIdL, checkId, "testl", "Test", "", "", 300_000L);
       db.setupIntGaugeReading(siteId, readingIdI, checkId, "testi", "Test", "", "", 300_000L);
       db.setupFloatGaugeReading(siteId, readingIdF, checkId, "testf", "Test", "", "", 300_000L);
       System.out.println("Setup reading: " + siteId + "::" + readingIdD + "/" + readingIdL + "/" + readingIdI + "/" + readingIdF);  
       //
       db.execute(() -> {
           long epoch = 1420070400000L;
           long start = System.currentTimeMillis();
           for (int i = 0; i < ((60/5) * 24 * 365); i++)
           {
               db.storeDoubleGaugeReading(new StoredDoubleGaugeReading(siteId, readingIdD, new Timestamp(epoch), (double) i, 80D, 90D, 1D, 100D));
               db.storeLongGaugeReading(new StoredLongGaugeReading(siteId, readingIdL, new Timestamp(epoch), (long) i, 80L, 90L, 1L, 100L));
               db.storeIntGaugeReading(new StoredIntGaugeReading(siteId, readingIdI, new Timestamp(epoch), (i % 100), 80, 90, 1, 100));
               db.storeFloatGaugeReading(new StoredFloatGaugeReading(siteId, readingIdF, new Timestamp(epoch), (float) (i % 100), 80F, 90F, 1F, 100F));
               epoch += 300_000L;
           }
           long end = System.currentTimeMillis();
           System.out.println("Added " + (((60/5) * 24 * 365) * 4) + " readings in " + (end - start) + " ms");
       });
       //
       for (StoredDoubleGaugeReading reading : db.getLatestDoubleGaugeReadings(siteId, readingIdD, 10))
       {
           System.out.println(reading.getCollectedAt() + " => " + reading.getValue());
       }
       //
       for (StoredLongGaugeReading reading : db.getLatestLongGaugeReadings(siteId, readingIdL, 10))
       {
           System.out.println(reading.getCollectedAt() + " => " + reading.getValue());
       }
       //
       for (StoredIntGaugeReading reading : db.getLatestIntGaugeReadings(siteId, readingIdI, 10))
       {
           System.out.println(reading.getCollectedAt() + " => " + reading.getValue());
       }
       //
       for (StoredFloatGaugeReading reading : db.getLatestFloatGaugeReadings(siteId, readingIdF, 10))
       {
           System.out.println(reading.getCollectedAt() + " => " + reading.getValue());
       }
    }
}
