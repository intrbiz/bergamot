package com.intrbiz.lamplighter.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.Cacheable;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.DatabaseConnection;
import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.meta.SQLCustom;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLOrder;
import com.intrbiz.data.db.compiler.meta.SQLParam;
import com.intrbiz.data.db.compiler.meta.SQLPatch;
import com.intrbiz.data.db.compiler.meta.SQLSchema;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLUserDefined;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.data.db.compiler.meta.ScriptType;
import com.intrbiz.data.db.compiler.util.SQLScript;
import com.intrbiz.lamplighter.model.CheckReading;
import com.intrbiz.lamplighter.model.StoredDoubleGaugeReading;
import com.intrbiz.lamplighter.model.StoredFloatGaugeReading;
import com.intrbiz.lamplighter.model.StoredIntGaugeReading;
import com.intrbiz.lamplighter.model.StoredLongGaugeReading;
import com.intrbiz.lamplighter.model.StoredMeterReading;
import com.intrbiz.lamplighter.model.StoredTimerReading;

@SQLSchema(
        name = "lamplighter", 
        version = @SQLVersion({4, 0, 0}),
        tables = {
            CheckReading.class,
            StoredDoubleGaugeReading.class,
            StoredLongGaugeReading.class,
            StoredIntGaugeReading.class,
            StoredFloatGaugeReading.class,
            StoredMeterReading.class,
            StoredTimerReading.class
        }
)
public abstract class LamplighterDB extends DatabaseAdapter
{

    /**
     * Compile and register the Bergamot Database Adapter
     */
    static
    {
        DataManager.getInstance().registerDatabaseAdapter(
                LamplighterDB.class, 
                DatabaseAdapterCompiler.defaultPGSQLCompiler().compileAdapterFactory(LamplighterDB.class)
        );
    }
    
    public static void load()
    {
        // do nothing
    }
    
    /**
     * Install the Bergamot schema into the default database
     */
    public static void install()
    {
        Logger logger = Logger.getLogger(LamplighterDB.class);
        DatabaseConnection database = DataManager.getInstance().connect();
        DatabaseAdapterCompiler compiler =  DatabaseAdapterCompiler.defaultPGSQLCompiler().setDefaultOwner("bergamot");
        // check if the schema is installed
        if (! compiler.isSchemaInstalled(database, LamplighterDB.class))
        {
            logger.info("Installing database schema");
            compiler.installSchema(database, LamplighterDB.class);
        }
        else
        {
            // check the installed schema is upto date
            if (! compiler.isSchemaUptoDate(database, LamplighterDB.class))
            {
                logger.info("The installed database schema is not upto date");
                compiler.upgradeSchema(database, LamplighterDB.class);
            }
            else
            {
                logger.info("The installed database schema is upto date");
            }
        }
    }

    /**
     * Connect to the Bergamot database
     */
    public static LamplighterDB connect()
    {
        return DataManager.getInstance().databaseAdapter(LamplighterDB.class);
    }
    
    /**
     * Connect to the Bergamot database
     */
    public static LamplighterDB connect(DatabaseConnection connection)
    {
        return DataManager.getInstance().databaseAdapter(LamplighterDB.class, connection);
    }

    public LamplighterDB(DatabaseConnection connection, Cache cache)
    {
        super(connection, cache);
    }
    
    public static void main(String[] args) throws Exception
    {
        if (args.length == 1 && "install".equals(args[0]))
        {
            DatabaseAdapterCompiler.main(new String[] { "install", LamplighterDB.class.getCanonicalName() });
        }
        else if (args.length == 2 && "upgrade".equals(args[0]))
        {
            DatabaseAdapterCompiler.main(new String[] { "upgrade", LamplighterDB.class.getCanonicalName(), args[1] });
        }
        else
        {
            // interactive
            try (Scanner input = new Scanner(System.in))
            {
                for (;;)
                {
                    System.out.print("Would you like to generate the install or upgrade schema: ");
                    String action = input.nextLine();
                    // process the action
                    if ("exit".equals(action) || "quit".equals(action) || "q".equals(action))
                    {
                        System.exit(0);
                    }
                    else if ("install".equalsIgnoreCase(action) || "in".equalsIgnoreCase(action) || "i".equalsIgnoreCase(action))
                    {
                        DatabaseAdapterCompiler.main(new String[] { "install", LamplighterDB.class.getCanonicalName() });
                        System.exit(0);
                    }
                    else if ("upgrade".equalsIgnoreCase(action) || "up".equalsIgnoreCase(action) || "u".equalsIgnoreCase(action))
                    {
                        System.out.print("What is the current installed version: ");
                        String version = input.nextLine();
                        DatabaseAdapterCompiler.main(new String[] { "upgrade", LamplighterDB.class.getCanonicalName(), version });
                        System.exit(0);
                    }
                }
            }
        }
    }
    
    // reading metadata
    
    @SQLSetter(table = CheckReading.class, name = "set_check_reading", since = @SQLVersion({4, 0, 0}))
    public abstract void setCheckReading(CheckReading reading);
    
    public void newReading(UUID siteId, UUID readingId, UUID checkId, String name, String summary, String description, String unit, String readingType, long pollInterval)
    {
        this.setCheckReading(new CheckReading(readingId, siteId, checkId, name, summary, description, unit, readingType, pollInterval));
        // invalidate caches
        this.getAdapterCache().removePrefix("get_check_reading_by_name." + checkId);
        this.getAdapterCache().removePrefix("get_check_readings_for_check." + checkId);
    }

    
    @Cacheable
    @SQLGetter(table = CheckReading.class, name = "get_check_reading", since = @SQLVersion({4, 0, 0}))
    public abstract CheckReading getCheckReading(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = CheckReading.class, name ="get_check_reading_by_name", since = @SQLVersion({4, 0, 0}), orderBy = @SQLOrder("name"))
    public abstract CheckReading getCheckReadingByName(@SQLParam("check_id") UUID checkId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = CheckReading.class, name ="get_check_readings_for_check", since = @SQLVersion({4, 0, 0}), orderBy = @SQLOrder("name"))
    public abstract List<CheckReading> getCheckReadingsForCheck(@SQLParam("check_id") UUID checkId);
    
    @SQLGetter(table = CheckReading.class, name = "list_check_readings", since = @SQLVersion({4, 0, 0}), orderBy = @SQLOrder("name"))
    public abstract List<CheckReading> listCheckReadings();
    
    public CheckReading getOrSetupDoubleGaugeReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.newReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, "double_gauge_reading", pollInterval);
            this.commit();
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public CheckReading getOrSetupLongGaugeReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.newReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, "long_gauge_reading", pollInterval);
            this.commit();
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public CheckReading getOrSetupIntGaugeReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.newReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, "int_gauge_reading", pollInterval);
            this.commit();
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public CheckReading getOrSetupFloatGaugeReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.newReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, "float_gauge_reading", pollInterval);
            this.commit();
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public CheckReading getOrSetupMeterReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.newReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, "meter_reading", pollInterval);
            this.commit();
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public CheckReading getOrSetupTimerReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.newReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, "timer_reading", pollInterval);
            this.commit();
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    // gauges
    
    // double
    
    @SQLSetter(table = StoredDoubleGaugeReading.class, name = "store_double_gauge_reading", upsert = false, since = @SQLVersion({4, 0, 0}))
    public abstract void storeDoubleGaugeReading(StoredDoubleGaugeReading reading);
    
    @SQLGetter(
        table = StoredDoubleGaugeReading.class, name ="get_latest_double_gauge_readings", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_latest_double_gauge_readings.sql")
    )
    public abstract List<StoredDoubleGaugeReading> getLatestDoubleGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(
        table = StoredDoubleGaugeReading.class, name ="get_double_gauge_readings_by_date", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_double_gauge_readings_by_date.sql")
    )
    public abstract List<StoredDoubleGaugeReading> getDoubleGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // long
    
    @SQLSetter(table = StoredLongGaugeReading.class, name = "store_long_gauge_reading", upsert = false, since = @SQLVersion({4, 0, 0}))
    public abstract void storeLongGaugeReading(StoredLongGaugeReading reading);
    
    @SQLGetter(
        table = StoredLongGaugeReading.class, name ="get_latest_long_gauge_readings", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_latest_long_gauge_readings.sql")
    )
    public abstract List<StoredLongGaugeReading> getLatestLongGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(
        table = StoredLongGaugeReading.class, name ="get_long_gauge_readings_by_date", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_long_gauge_readings_by_date.sql")
    )
    public abstract List<StoredLongGaugeReading> getLongGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // int
    
    @SQLSetter(table = StoredIntGaugeReading.class, name = "store_int_gauge_reading", upsert = false, since = @SQLVersion({4, 0, 0}))
    public abstract void storeIntGaugeReading(StoredIntGaugeReading reading);
    
    @SQLGetter(
        table = StoredIntGaugeReading.class, name ="get_latest_int_gauge_readings", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_latest_int_gauge_readings.sql")
    )
    public abstract List<StoredIntGaugeReading> getLatestIntGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(
        table = StoredIntGaugeReading.class, name ="get_int_gauge_readings_by_date", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_int_gauge_readings_by_date.sql")
    )
    public abstract List<StoredIntGaugeReading> getIntGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // float
    
    @SQLSetter(table = StoredFloatGaugeReading.class, name = "store_float_gauge_reading", upsert = false, since = @SQLVersion({4, 0, 0}))
    public abstract void storeFloatGaugeReading(StoredFloatGaugeReading reading);
    
    @SQLGetter(
        table = StoredFloatGaugeReading.class, name ="get_latest_float_gauge_readings", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_latest_float_gauge_readings.sql")
    )
    public abstract List<StoredFloatGaugeReading> getLatestFloatGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(
        table = StoredFloatGaugeReading.class, name ="get_float_gauge_readings_by_date", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_float_gauge_readings_by_date.sql")
    )
    public abstract List<StoredFloatGaugeReading> getFloatGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // meter

    @SQLSetter(table = StoredMeterReading.class, name = "store_meter_reading", upsert = false, since = @SQLVersion({4, 0, 0}))
    public abstract void storeMeterReading(StoredMeterReading reading);
    
    @SQLGetter(
        table = StoredMeterReading.class, name ="get_latest_meter_readings", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_latest_meter_readings.sql")
    )
    public abstract List<StoredMeterReading> getLatestMeterReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(
        table = StoredMeterReading.class, name ="get_meter_readings_by_date", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_meter_readings_by_date.sql")
    )
    public abstract List<StoredMeterReading> getMeterReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // timer
   
    @SQLSetter(table = StoredTimerReading.class, name = "store_timer_reading", upsert = false, since = @SQLVersion({4, 0, 0}))
    public abstract void storeTimerReading(StoredTimerReading reading);
    
    @SQLGetter(
        table = StoredTimerReading.class, name ="get_latest_timer_readings", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_latest_timer_readings.sql")
    )
    public abstract List<StoredTimerReading> getLatestTimerReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(
        table = StoredTimerReading.class, name ="get_timer_readings_by_date", 
        since = @SQLVersion({4, 0, 0}),
        userDefined = @SQLUserDefined(resources = "get_timer_readings_by_date.sql")
    )
    public abstract List<StoredTimerReading> getTimerReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // custom helper functions
    
    @SQLCustom(
            since = @SQLVersion({4, 0, 0}),
            userDefined = @SQLUserDefined(resources = "round_time.sql")
    )
    public Timestamp roundTime(Timestamp time, long round)
    {
        return this.use((con) -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT lamplighter.round_time(?, ?)"))
            {
                stmt.setTimestamp(1, time);
                stmt.setLong(2, round);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next()) return rs.getTimestamp(1);
                }
            }
            return null;
        });
    }
    
    @SQLPatch(name = "set_function_owner", index = 1, type = ScriptType.BOTH_LAST, version = @SQLVersion({4, 0, 0}))
    protected static SQLScript setFunctionOWner()
    {
        return new SQLScript(
                // helpers
                "ALTER FUNCTION lamplighter.round_time(TIMESTAMP WITH TIME ZONE, BIGINT) OWNER TO bergamot;",
                // double
                "ALTER FUNCTION lamplighter.get_latest_double_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_double_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // long
                "ALTER FUNCTION lamplighter.get_latest_long_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_long_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // int
                "ALTER FUNCTION lamplighter.get_latest_int_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_int_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // float
                "ALTER FUNCTION lamplighter.get_latest_float_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_float_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // meter
                "ALTER FUNCTION lamplighter.get_latest_meter_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_meter_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // timer
                "ALTER FUNCTION lamplighter.get_latest_timer_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_timer_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;"
        );
    }
    
    @SQLPatch(name = "create_reading_partitions", index = 2, type = ScriptType.INSTALL_LAST, version = @SQLVersion({4, 0, 0}), skip = false)
    public static SQLScript createReadingPartitions()
    {
        // Allow the number of readings hash partitions to be configured
        int modulus = Integer.parseInt(Util.coalesceEmpty(System.getenv("LAMPLIGHTER_DB_READINGS_HASH_MODULUS"), System.getProperty("lamplighter.db.readings.hash.modulus"), "4"));
        // Raw Readings partitioned by hash and then weekly
        return new SQLScript(
            Arrays.asList("double_gauge_reading", "float_gauge_reading", "int_gauge_reading", "long_gauge_reading", "meter_reading", "timer_reading").stream().map(readingName -> 
            "WITH level0 (suffix, table_name) AS (\n" + 
            "  SELECT h.r::TEXT, lamplighter.create_" + readingName + "_partition_0(h.r::TEXT, " + modulus + ", h.r, NULL)\n" + 
            "  FROM generate_series(0, " + (modulus - 1) + ", 1) h(r)\n" + 
            ")\n" + 
            "SELECT level0.table_name, lamplighter.create_" + readingName + "_partition_1(level0.table_name, level0.suffix || '_' || to_char(qq.start, 'YYYY_MM_DD'), qq.start, qq.end, NULL)\n" + 
            "FROM level0,\n" + 
            "( \n" + 
            "  SELECT q.start, lead(q.start) OVER () AS end  \n" + 
            "  FROM generate_series(date_trunc('week', now() - '1 week'::interval)::date, date_trunc('week', now() + '5 years'::interval)::date, '1 week'::interval) q(start)  \n" + 
            ") qq \n" + 
            "WHERE qq.end IS NOT NULL"
            ).collect(Collectors.toList()).toArray(new String[] {})
        );
    }
    
    private static String nameToSummary(String name)
    {
        return Util.ucFirst(name.replace('-', ' ').replace('_', ' ').replace('.', ' '));
    }
}
