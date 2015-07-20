package com.intrbiz.lamplighter.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.Cacheable;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.DatabaseConnection;
import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLOrder;
import com.intrbiz.data.db.compiler.meta.SQLParam;
import com.intrbiz.data.db.compiler.meta.SQLPatch;
import com.intrbiz.data.db.compiler.meta.SQLSchema;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.data.db.compiler.meta.ScriptType;
import com.intrbiz.data.db.compiler.util.SQLScript;
import com.intrbiz.lamplighter.model.CheckReading;
import com.intrbiz.lamplighter.model.StoredDoubleGaugeReading;
import com.intrbiz.lamplighter.model.StoredFloatGaugeReading;
import com.intrbiz.lamplighter.model.StoredIntGaugeReading;
import com.intrbiz.lamplighter.model.StoredLongGaugeReading;

@SQLSchema(
        name = "lamplighter", 
        version = @SQLVersion({1, 4, 0}),
        tables = {
            CheckReading.class,
            StoredDoubleGaugeReading.class,
            StoredLongGaugeReading.class,
            StoredIntGaugeReading.class,
            StoredFloatGaugeReading.class
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
    
    @SQLGetter(table = CheckReading.class, name = "get_check_reading", since = @SQLVersion({1, 0, 0}))
    public abstract CheckReading getCheckReading(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = CheckReading.class, name ="get_check_reading_by_name", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder("name"))
    public abstract CheckReading getCheckReadingByName(@SQLParam("check_id") UUID checkId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = CheckReading.class, name ="get_check_readings_for_check", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder("name"))
    public abstract List<CheckReading> getCheckReadingsForCheck(@SQLParam("check_id") UUID checkId);
    
    @SQLGetter(table = CheckReading.class, name = "list_check_readings", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder("name"))
    public abstract List<CheckReading> listCheckReadings();
    
    // reading management
    
    public int setupSiteReadings(UUID siteId)
    {
        return this.use((with) -> {
            try (PreparedStatement stmt = with.prepareStatement("SELECT lamplighter.new_site(?::UUID)"))
            {
              stmt.setObject(1, siteId);
              try (ResultSet rs = stmt.executeQuery())
              {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
              }
            }
            return null;
        });
    }
    
    public CheckReading getOrSetupDoubleGaugeReading(UUID checkId, String name, String unit, long pollInterval)
    {
        // does it already exist
        CheckReading reading = this.getCheckReadingByName(checkId, name);
        if (reading == null)
        {
            UUID siteId = Site.getSiteId(checkId);
            UUID readingId = Site.randomId(siteId);
            // setup
            this.setupDoubleGaugeReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, pollInterval);
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public int setupDoubleGaugeReading(UUID siteId, UUID readingId, UUID checkId, String name, String summary, String description, String unit, long pollInterval)
    {
        int r = this.use((with) -> {
            try (PreparedStatement stmt = with.prepareStatement("SELECT lamplighter.new_reading(?, ?, ?, ?, ?, ?, ?, 'double_gauge_reading', ?)"))
            {
              stmt.setObject(1, siteId);
              stmt.setObject(2, readingId);
              stmt.setObject(3, checkId);
              stmt.setString(4, name);
              stmt.setString(5, summary);
              stmt.setString(6, description);
              stmt.setString(7, unit);
              stmt.setLong(8, pollInterval);
              try (ResultSet rs = stmt.executeQuery())
              {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
              }
            }
            return null;
        });
        // invalidate caches
        this.getAdapterCache().removePrefix("get_check_reading_by_name." + checkId);
        this.getAdapterCache().removePrefix("get_check_readings_for_check." + checkId);
        return r;
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
            this.setupLongGaugeReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, pollInterval);
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public int setupLongGaugeReading(UUID siteId, UUID readingId, UUID checkId, String name, String summary, String description, String unit, long pollInterval)
    {
        int r = this.use((with) -> {
            try (PreparedStatement stmt = with.prepareStatement("SELECT lamplighter.new_reading(?, ?, ?, ?, ?, ?, ?, 'long_gauge_reading', ?)"))
            {
              stmt.setObject(1, siteId);
              stmt.setObject(2, readingId);
              stmt.setObject(3, checkId);
              stmt.setString(4, name);
              stmt.setString(5, summary);
              stmt.setString(6, description);
              stmt.setString(7, unit);
              stmt.setLong(8, pollInterval);
              try (ResultSet rs = stmt.executeQuery())
              {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
              }
            }
            return null;
        });
        // invalidate caches
        this.getAdapterCache().removePrefix("get_check_reading_by_name." + checkId);
        this.getAdapterCache().removePrefix("get_check_readings_for_check." + checkId);
        return r;
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
            this.setupIntGaugeReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, pollInterval);
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public int setupIntGaugeReading(UUID siteId, UUID readingId, UUID checkId, String name, String summary, String description, String unit, long pollInterval)
    {
        int r = this.use((with) -> {
            try (PreparedStatement stmt = with.prepareStatement("SELECT lamplighter.new_reading(?, ?, ?, ?, ?, ?, ?, 'int_gauge_reading', ?)"))
            {
              stmt.setObject(1, siteId);
              stmt.setObject(2, readingId);
              stmt.setObject(3, checkId);
              stmt.setString(4, name);
              stmt.setString(5, summary);
              stmt.setString(6, description);
              stmt.setString(7, unit);
              stmt.setLong(8, pollInterval);
              try (ResultSet rs = stmt.executeQuery())
              {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
              }
            }
            return null;
        });
        // invalidate caches
        this.getAdapterCache().removePrefix("get_check_reading_by_name." + checkId);
        this.getAdapterCache().removePrefix("get_check_readings_for_check." + checkId);
        return r;
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
            this.setupFloatGaugeReading(siteId, readingId, checkId, name, nameToSummary(name), null, unit, pollInterval);
            // get the metadata
            reading = this.getCheckReading(readingId);
        }
        return reading;
    }
    
    public int setupFloatGaugeReading(UUID siteId, UUID readingId, UUID checkId, String name, String summary, String description, String unit, long pollInterval)
    {
        int r = this.use((with) -> {
            try (PreparedStatement stmt = with.prepareStatement("SELECT lamplighter.new_reading(?, ?, ?, ?, ?, ?, ?, 'float_gauge_reading', ?)"))
            {
              stmt.setObject(1, siteId);
              stmt.setObject(2, readingId);
              stmt.setObject(3, checkId);
              stmt.setString(4, name);
              stmt.setString(5, summary);
              stmt.setString(6, description);
              stmt.setString(7, unit);
              stmt.setLong(8, pollInterval);
              try (ResultSet rs = stmt.executeQuery())
              {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
              }
            }
            return null;
        });
        // invalidate caches
        this.getAdapterCache().removePrefix("get_check_reading_by_name." + checkId);
        this.getAdapterCache().removePrefix("get_check_readings_for_check." + checkId);
        return r;
    }
    
    // gauges
    
    // double
    
    @SQLSetter(table = StoredDoubleGaugeReading.class, name = "store_double_gauge_reading", upsert = false, since = @SQLVersion({1, 0, 0}))
    public abstract void storeDoubleGaugeReading(StoredDoubleGaugeReading reading);
    
    @SQLGetter(table = StoredDoubleGaugeReading.class, name ="get_latest_double_gauge_readings", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredDoubleGaugeReading> getLatestDoubleGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(table = StoredDoubleGaugeReading.class, name ="get_double_gauge_readings_by_date", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredDoubleGaugeReading> getDoubleGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // long
    
    @SQLSetter(table = StoredLongGaugeReading.class, name = "store_long_gauge_reading", upsert = false, since = @SQLVersion({1, 0, 0}))
    public abstract void storeLongGaugeReading(StoredLongGaugeReading reading);
    
    @SQLGetter(table = StoredLongGaugeReading.class, name ="get_latest_long_gauge_readings", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredLongGaugeReading> getLatestLongGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(table = StoredLongGaugeReading.class, name ="get_long_gauge_readings_by_date", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredLongGaugeReading> getLongGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // int
    
    @SQLSetter(table = StoredIntGaugeReading.class, name = "store_int_gauge_reading", upsert = false, since = @SQLVersion({1, 0, 0}))
    public abstract void storeIntGaugeReading(StoredIntGaugeReading reading);
    
    @SQLGetter(table = StoredIntGaugeReading.class, name ="get_latest_int_gauge_readings", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredIntGaugeReading> getLatestIntGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(table = StoredIntGaugeReading.class, name ="get_int_gauge_readings_by_date", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredIntGaugeReading> getIntGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // float
    
    @SQLSetter(table = StoredFloatGaugeReading.class, name = "store_float_gauge_reading", upsert = false, since = @SQLVersion({1, 0, 0}))
    public abstract void storeFloatGaugeReading(StoredFloatGaugeReading reading);
    
    @SQLGetter(table = StoredFloatGaugeReading.class, name ="get_latest_float_gauge_readings", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredFloatGaugeReading> getLatestFloatGaugeReadings(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "limit", virtual = true) int limit);
    
    @SQLGetter(table = StoredFloatGaugeReading.class, name ="get_float_gauge_readings_by_date", since = @SQLVersion({1, 0, 0}))
    public abstract List<StoredFloatGaugeReading> getFloatGaugeReadingsByDate(@SQLParam("site_id") UUID siteId, @SQLParam("reading_id") UUID readingId, @SQLParam(value = "start", virtual = true) Timestamp start, @SQLParam(value = "end", virtual = true) Timestamp end, @SQLParam(value = "rollup", virtual = true) long rollup, @SQLParam(value = "agg", virtual = true) String agg);
    
    // custom SQL patches
    
    @SQLPatch(name = "create_helper_functions", index = 1, type = ScriptType.BOTH, version = @SQLVersion({1, 5, 0}))
    protected static SQLScript createHelperFunctions()
    {
        return new SQLScript(
                // get default owner for reading tables
                "CREATE OR REPLACE FUNCTION lamplighter.get_default_owner() RETURNS TEXT LANGUAGE SQL AS $body$ SELECT 'bergamot'::TEXT; $body$;",
                // get schema name
                "CREATE OR REPLACE FUNCTION lamplighter.get_schema(p_site_id UUID) RETURNS TEXT LANGUAGE SQL AS $body$ SELECT ('readings_' || $1)::TEXT; $body$;",
                // get table name
                "CREATE OR REPLACE FUNCTION lamplighter.get_table_name(p_type TEXT, p_reading_id UUID) RETURNS TEXT LANGUAGE sql AS $body$ SELECT (CASE WHEN ($2 IS NULL) THEN $1 ELSE 'reading_' || p_reading_id END)::TEXT $body$;"
        );
    }
    
    // setup
    
    @SQLPatch(name = "create_new_site", index = 2, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createNewSite()
    {
        return SQLScript.fromResource(LamplighterDB.class, "new_site.sql");
    }
    
    @SQLPatch(name = "create_new_reading", index = 3, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createNewReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "new_reading.sql");
    }
    
    // double
    
    @SQLPatch(name = "create_create_double_gauge_reading", index = 4, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createCreateDoubleGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "create_double_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_store_double_gauge_reading", index = 5, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceStoreDoubleGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "store_double_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_get_latest_double_gauge_readings", index = 6, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetLatestDoubleGaugeReadings()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_latest_double_gauge_readings.sql");
    }
    
    @SQLPatch(name = "replace_get_double_gauge_readings_by_date", index = 7, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetDoubleGaugeReadingsByDate()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_double_gauge_readings_by_date.sql");
    }
    
    // long
    
    @SQLPatch(name = "create_create_long_gauge_reading", index = 8, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createCreateLongGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "create_long_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_store_long_gauge_reading", index = 9, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceStoreLongGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "store_long_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_get_latest_long_gauge_readings", index = 10, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetLatestLongGaugeReadings()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_latest_long_gauge_readings.sql");
    }
    
    @SQLPatch(name = "replace_get_long_gauge_readings_by_date", index = 11, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetLongGaugeReadingsByDate()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_long_gauge_readings_by_date.sql");
    }
    
    // int
    
    @SQLPatch(name = "create_create_int_gauge_reading", index = 12, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createCreateIntGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "create_int_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_store_int_gauge_reading", index = 13, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceStoreIntGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "store_int_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_get_latest_int_gauge_readings", index = 14, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetLatestIntGaugeReadings()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_latest_int_gauge_readings.sql");
    }
    
    @SQLPatch(name = "replace_get_int_gauge_readings_by_date", index = 15, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetIntGaugeReadingsByDate()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_int_gauge_readings_by_date.sql");
    }
    
    // float
    
    @SQLPatch(name = "create_create_float_gauge_reading", index = 16, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createCreateFloatGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "create_float_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_store_float_gauge_reading", index = 17, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceStoreFloatGaugeReading()
    {
        return SQLScript.fromResource(LamplighterDB.class, "store_float_gauge_reading.sql");
    }
    
    @SQLPatch(name = "replace_get_latest_float_gauge_readings", index = 18, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetLatestFloatGaugeReadings()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_latest_float_gauge_readings.sql");
    }
    
    @SQLPatch(name = "replace_get_float_gauge_readings_by_date", index = 19, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript replaceGetFloatGaugeReadingsByDate()
    {
        return SQLScript.fromResource(LamplighterDB.class, "get_float_gauge_readings_by_date.sql");
    }
    
    @SQLPatch(name = "create_round_time", index = 20, type = ScriptType.BOTH, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript createRoundTime()
    {
        return SQLScript.fromResource(LamplighterDB.class, "round_time.sql");
    }
    
    @SQLPatch(name = "set_default_poll_interval", index = 21, type = ScriptType.UPGRADE, version = @SQLVersion({1, 1, 0}))
    protected static SQLScript setDefaultPollInterval()
    {
        return new SQLScript(
                "UPDATE lamplighter.check_reading SET poll_interval = 300000"
        );
    }
    
    @SQLPatch(name = "set_function_owner", index = 1000, type = ScriptType.BOTH, version = @SQLVersion({1, 5, 0}))
    protected static SQLScript setFunctionOWner()
    {
        return new SQLScript(
                // helpers
                "ALTER FUNCTION lamplighter.get_default_owner() OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_schema(UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_table_name(TEXT, UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.round_time(TIMESTAMP WITH TIME ZONE, BIGINT) OWNER TO bergamot;",
                // setup
                "ALTER FUNCTION lamplighter.new_site(UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.new_reading(UUID, UUID, UUID, TEXT, TEXT, TEXT, TEXT, TEXT, BIGINT) OWNER TO bergamot;",
                // double
                "ALTER FUNCTION lamplighter.create_double_gauge_reading(UUID, UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.store_double_gauge_reading(UUID, UUID, TIMESTAMP WITH TIME ZONE, DOUBLE PRECISION, DOUBLE PRECISION, DOUBLE PRECISION, DOUBLE PRECISION, DOUBLE PRECISION) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_latest_double_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_double_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // long
                "ALTER FUNCTION lamplighter.create_long_gauge_reading(UUID, UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.store_long_gauge_reading(UUID, UUID, TIMESTAMP WITH TIME ZONE, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_latest_long_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_long_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // int
                "ALTER FUNCTION lamplighter.create_int_gauge_reading(UUID, UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.store_int_gauge_reading(UUID, UUID, TIMESTAMP WITH TIME ZONE, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_latest_int_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_int_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;",
                // float
                "ALTER FUNCTION lamplighter.create_float_gauge_reading(UUID, UUID) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.store_float_gauge_reading(UUID, UUID, TIMESTAMP WITH TIME ZONE, REAL, REAL, REAL, REAL, REAL) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_latest_float_gauge_readings(UUID, UUID, INTEGER) OWNER TO bergamot;",
                "ALTER FUNCTION lamplighter.get_float_gauge_readings_by_date(UUID, UUID, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE, BIGINT, TEXT) OWNER TO bergamot;"
        );
    }
    
    @SQLPatch(name = "setup_existing_sites", index = 2000, type = ScriptType.INSTALL, version = @SQLVersion({1, 0, 0}))
    protected static SQLScript setupExistingSites()
    {
        return new SQLScript(
                "SELECT lamplighter.new_site(id) FROM bergamot.site"
        );
    }
    
    private static String nameToSummary(String name)
    {
        return Util.ucFirst(name.replace('-', ' ').replace('_', ' '));
    }
}
