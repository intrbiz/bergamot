package com.intrbiz.lamplighter.reading;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.result.matcher.Matcher;
import com.intrbiz.bergamot.result.matcher.Matchers;
import com.intrbiz.data.DataException;
import com.intrbiz.gerald.polyakov.MeterReading;
import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.ReadingSnapshot;
import com.intrbiz.gerald.polyakov.TimerReading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.lamplighter.model.CheckReading;
import com.intrbiz.lamplighter.model.StoredDoubleGaugeReading;
import com.intrbiz.lamplighter.model.StoredFloatGaugeReading;
import com.intrbiz.lamplighter.model.StoredIntGaugeReading;
import com.intrbiz.lamplighter.model.StoredLongGaugeReading;
import com.intrbiz.lamplighter.model.StoredMeterReading;
import com.intrbiz.lamplighter.model.StoredTimerReading;
import com.intrbiz.lamplighter.reading.scaling.ReadingScalers;

public class DefaultReadingProcessor extends AbstractReadingProcessor
{
    private static final Logger logger = Logger.getLogger(DefaultReadingProcessor.class);
    
    private Matchers matchers = new Matchers();
    
    private ReadingScalers scalers = new ReadingScalers();

    public DefaultReadingProcessor()
    {
        super();
    }

    @Override
    public void process(ReadingParcelMO readings)
    {
        if (logger.isTraceEnabled()) logger.trace("Processing readings: " + readings);
        // store the readings
        try (LamplighterDB db = LamplighterDB.connect())
        {
            db.execute(() -> {
                Check<?, ?> check = this.matchCheck(readings);
                if (check != null)
                {
                    // guess poll interval for this reading, default to 5 minutes
                    long pollInterval = 300_000L;
                    // for an active check, use the worst case poll interval
                    if (check instanceof ActiveCheck<?,?>)
                    {
                        pollInterval = Math.max(Math.max(((ActiveCheck<?,?>) check).getCheckInterval(), ((ActiveCheck<?,?>) check).getRetryInterval()), ((ActiveCheck<?,?>) check).getChangingInterval());
                    }
                    // store the readings
                    for (Reading reading : readings.getReadings())
                    {
                        if (reading instanceof DoubleGaugeReading)
                        {
                            CheckReading metadata = db.getOrSetupDoubleGaugeReading(check.getId(), reading.getName(), reading.getUnit(), pollInterval);
                            reading = this.preProcessReading(metadata, reading);
                            db.storeDoubleGaugeReading(new StoredDoubleGaugeReading(
                                    metadata.getSiteId(),
                                    metadata.getId(),
                                    new Timestamp(readings.getCaptured()),
                                    ((DoubleGaugeReading) reading).getValue() == null ? 0 : ((DoubleGaugeReading) reading).getValue(),
                                    ((DoubleGaugeReading) reading).getWarning() == null ? 0 : ((DoubleGaugeReading) reading).getWarning(),
                                    ((DoubleGaugeReading) reading).getCritical() == null ? 0 : ((DoubleGaugeReading) reading).getCritical(),
                                    ((DoubleGaugeReading) reading).getMin() == null ? 0 : ((DoubleGaugeReading) reading).getMin(),
                                    ((DoubleGaugeReading) reading).getMax() == null ? 0 : ((DoubleGaugeReading) reading).getMax()
                            ));
                        }
                        else if (reading instanceof LongGaugeReading)
                        {
                            CheckReading metadata = db.getOrSetupLongGaugeReading(check.getId(), reading.getName(), reading.getUnit(), pollInterval);
                            reading = this.preProcessReading(metadata, reading);
                            db.storeLongGaugeReading(new StoredLongGaugeReading(
                                    metadata.getSiteId(),
                                    metadata.getId(),
                                    new Timestamp(readings.getCaptured()),
                                    ((LongGaugeReading) reading).getValue() == null ? 0 : ((LongGaugeReading) reading).getValue(),
                                    ((LongGaugeReading) reading).getWarning() == null ? 0 : ((LongGaugeReading) reading).getWarning(),
                                    ((LongGaugeReading) reading).getCritical() == null ? 0 : ((LongGaugeReading) reading).getCritical(),
                                    ((LongGaugeReading) reading).getMin() == null ? 0 : ((LongGaugeReading) reading).getMin(),
                                    ((LongGaugeReading) reading).getMax() == null ? 0 : ((LongGaugeReading) reading).getMax()
                            ));
                        }
                        else if (reading instanceof FloatGaugeReading)
                        {
                            CheckReading metadata = db.getOrSetupFloatGaugeReading(check.getId(), reading.getName(), reading.getUnit(), pollInterval);
                            reading = this.preProcessReading(metadata, reading);
                            db.storeFloatGaugeReading(new StoredFloatGaugeReading(
                                    metadata.getSiteId(),
                                    metadata.getId(),
                                    new Timestamp(readings.getCaptured()),
                                    ((FloatGaugeReading) reading).getValue() == null ? 0 : ((FloatGaugeReading) reading).getValue(),
                                    ((FloatGaugeReading) reading).getWarning() == null ? 0 : ((FloatGaugeReading) reading).getWarning(),
                                    ((FloatGaugeReading) reading).getCritical() == null ? 0 : ((FloatGaugeReading) reading).getCritical(),
                                    ((FloatGaugeReading) reading).getMin() == null ? 0 : ((FloatGaugeReading) reading).getMin(),
                                    ((FloatGaugeReading) reading).getMax() == null ? 0 : ((FloatGaugeReading) reading).getMax()
                            ));
                        }
                        else if (reading instanceof IntegerGaugeReading)
                        {
                            CheckReading metadata = db.getOrSetupIntGaugeReading(check.getId(), reading.getName(), reading.getUnit(), pollInterval);
                            reading = this.preProcessReading(metadata, reading);
                            db.storeIntGaugeReading(new StoredIntGaugeReading(
                                    metadata.getSiteId(),
                                    metadata.getId(),
                                    new Timestamp(readings.getCaptured()),
                                    ((IntegerGaugeReading) reading).getValue() == null ? 0 : ((IntegerGaugeReading) reading).getValue(),
                                    ((IntegerGaugeReading) reading).getWarning() == null ? 0 : ((IntegerGaugeReading) reading).getWarning(),
                                    ((IntegerGaugeReading) reading).getCritical() == null ? 0 : ((IntegerGaugeReading) reading).getCritical(),
                                    ((IntegerGaugeReading) reading).getMin() == null ? 0 : ((IntegerGaugeReading) reading).getMin(),
                                    ((IntegerGaugeReading) reading).getMax() == null ? 0 : ((IntegerGaugeReading) reading).getMax()
                            ));
                        }
                        else if (reading instanceof MeterReading)
                        {
                            CheckReading metadata = db.getOrSetupMeterReading(check.getId(), reading.getName(), reading.getUnit(), pollInterval);
                            MeterReading meterReading = this.preProcessReading(metadata, (MeterReading) reading);
                            db.storeMeterReading(new StoredMeterReading(
                                    metadata.getSiteId(),
                                    metadata.getId(),
                                    new Timestamp(readings.getCaptured()),
                                    meterReading.getCount(),
                                    meterReading.getMeanRate(),
                                    meterReading.getOneMinuteRate(),
                                    meterReading.getFiveMinuteRate(),
                                    meterReading.getFifteenMinuteRate()
                            ));
                        }
                        else if (reading instanceof TimerReading)
                        {
                            CheckReading metadata = db.getOrSetupTimerReading(check.getId(), reading.getName(), reading.getUnit(), pollInterval);
                            TimerReading timerReading = this.preProcessReading(metadata, (TimerReading) reading);
                            ReadingSnapshot snapshot = timerReading.getSnapshot();
                            db.storeTimerReading(new StoredTimerReading(
                                    metadata.getSiteId(),
                                    metadata.getId(),
                                    new Timestamp(readings.getCaptured()),
                                    timerReading.getCount(),
                                    timerReading.getMeanRate(),
                                    timerReading.getOneMinuteRate(),
                                    timerReading.getFiveMinuteRate(),
                                    timerReading.getFifteenMinuteRate(),
                                    snapshot.getMedian(),
                                    snapshot.getMean(),
                                    snapshot.getMin(),
                                    snapshot.getMax(),
                                    snapshot.getStdDev(),
                                    snapshot.getThe75thPercentile(),
                                    snapshot.getThe95thPercentile(),
                                    snapshot.getThe98thPercentile(),
                                    snapshot.getThe99thPercentile(),
                                    snapshot.getThe999thPercentile()
                            ));
                        }
                    }
                }
                else
                {
                    logger.info("Failed to match check for readings " + readings.getId() + " ignoring.");
                }
            });
        }
        catch (DataException e)
        {
            logger.error("Failed to store readings: " + readings, e);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Check<?, ?> matchCheck(ReadingParcelMO readings)
    {
        // use the match engine to find the result
        Matcher<?> matcher = this.matchers.buildMatcher(readings.getMatchOn());
        if (matcher == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return ((Matcher) matcher).match(db, readings.getMatchOn(), readings);
        }
    }
    
    protected <T extends Reading> T preProcessReading(CheckReading checkReading, T reading)
    {
        // apply the scaling rules
        reading = this.scalers.scale(reading, checkReading.getUnit());
        return reading;
    }
    
}
