package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.TimePeriod;

public class ManualTestWheelScheduler extends WheelScheduler
{
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
        sch.scheduleJob(UUID.randomUUID(), UUID.randomUUID(), 1, 60_000L, 0L, new TimePeriod(), new Runnable() {
            public void run()
            {
                System.out.println("Run: " + System.currentTimeMillis());
            }
        });
        //
        sch.start();
    }
}
