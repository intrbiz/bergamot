package com.intrbiz.bergamot.worker.engine.dummy;

import java.util.Calendar;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.bergamot.timerange.TimeRangeParser;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Execute a timed dummy check, which changes state based on time ranges
 */
public class TimedExecutor extends AbstractCheckExecutor<DummyEngine>
{
    public static final String NAME = "timed";

    public TimedExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        long start = System.nanoTime();
        ActiveResult result = new ActiveResult().fromCheck(executeCheck);
        String output = executeCheck.getParameter("output", "");
        // parse time ranges
        TimeRange warning = TimeRangeParser.parseTimeRange(executeCheck.getParameter("warning", "01:00-02:00, 05:00-06:00, 08:00-09:00, 11:00-12:00, 14:00-15:00, 17:00-18:00, 20:00-21:00, 23:00-24:00"));
        TimeRange critical = TimeRangeParser.parseTimeRange(executeCheck.getParameter("critical", "02:00-03:00, 06:00-07:00, 09:00-10:00, 12:00-13:00, 15:00-16:00, 18:00-19:00, 21:00-22:00"));
        Calendar now = Calendar.getInstance();
        // compute the check state
        if (critical.isInTimeRange(now))
        {
            result.critical(output);
        }
        else if (warning.isInTimeRange(now))
        {
            result.warning(output);
        }
        else
        {
            result.ok(output);
        }
        context.publishActiveResult(result.runtime(((double)(System.nanoTime() - start)) / 1_000_000D));
    }
}
