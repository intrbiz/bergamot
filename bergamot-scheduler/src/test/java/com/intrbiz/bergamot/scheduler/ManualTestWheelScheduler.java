package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.TimePeriod;

public class ManualTestWheelScheduler extends WheelScheduler
{
    public ManualTestWheelScheduler()
    {
        super(UUID.randomUUID(), null);
    }
    
    protected void startQueues() throws Exception
    {
    }

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        ManualTestWheelScheduler sch = new ManualTestWheelScheduler();
        //
        UUID j1 = UUID.randomUUID();
        sch.scheduleJob(j1, 0, UUID.randomUUID(), 1, 10_000L, sch.initialDelay.nextInt(10_000), new TimePeriod(), new Runnable() {
            public void run()
            {
                System.out.println("Run: " + System.currentTimeMillis());
            }
        });
        UUID j2 = UUID.randomUUID();
        sch.scheduleJob(j2, 1, UUID.randomUUID(), 1, 60_000L, sch.initialDelay.nextInt(60_000), new TimePeriod(), new Runnable() {
            public void run()
            {
                System.out.println("Run: " + System.currentTimeMillis());
            }
        });
        UUID j3 = UUID.randomUUID();
        sch.scheduleJob(j3, 2, UUID.randomUUID(), 1, 300_000L, sch.initialDelay.nextInt(300_000), new TimePeriod(), new Runnable() {
            public void run()
            {
                System.out.println("Run: " + System.currentTimeMillis());
            }
        });
        //
        sch.rescheduleJob(j3, 5_000L, null, null);
        sch.rescheduleJob(j3, 300_000L, null, null);
        //
        sch.rescheduleJob(j1, 1_000L, null, null);
        sch.rescheduleJob(j1, 30_000L, null, null);
        //
        sch.rescheduleJob(j2, 193_000L, null, null);
        sch.rescheduleJob(j2, 3_000L, null, null);
        //
        //sch.start();
    }
}
