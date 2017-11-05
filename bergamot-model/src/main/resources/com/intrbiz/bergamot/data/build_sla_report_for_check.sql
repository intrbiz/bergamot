CREATE OR REPLACE FUNCTION bergamot.build_sla_report_for_check(p_check_id UUID)
RETURNS SETOF bergamot.sla_report
LANGUAGE plpgsql AS $$
BEGIN
  RETURN QUERY SELECT
    "check".id AS check_id,
    "check".name AS check_name,
    "check".summary AS check_summary,
    "check".description AS check_description,
    sla_period.sla_id,
    sla_period.sla_name,
    sla_period.sla_summary,
    sla_period.sla_description,
    sla_period.sla_target,
    sla_period.period_name,
    sla_period.period_summary,
    sla_period.period_description,
    (now() - sla_period.period_interval) AS period_start,
    now() AS period_end,
    downtime.alerts AS period_alerts,
    downtime.false_positives AS period_false_positives,
    ((extract(epoch FROM sla_period.period_interval) - coalesce(extract(epoch FROM downtime.downtime), 0))::DOUBLE PRECISION / extract(epoch FROM sla_period.period_interval)::DOUBLE PRECISION) AS period_value,
    (((extract(epoch FROM sla_period.period_interval) - coalesce(extract(epoch FROM downtime.downtime), 0))::DOUBLE PRECISION / extract(epoch FROM sla_period.period_interval)::DOUBLE PRECISION) < sla_period.sla_target) AS period_breached
    FROM (
        SELECT 
         s.id AS sla_id,
         s.check_id AS sla_check_id,
         s.name AS sla_name,
         s.summary AS sla_summary,
         s.description AS sla_description,
         s.target AS sla_target,
         r.name AS period_name,
         r.summary AS period_summary,
         r.description AS period_description,
         (CASE 
          WHEN r.granularity = 0 THEN '1 day' 
          WHEN r.granularity = 1 THEN '1 week' 
          WHEN r.granularity = 2 THEN '1 month' 
          WHEN r.granularity = 3 THEN '1 year' 
          ELSE null 
         END)::INTERVAL AS period_interval
        FROM bergamot.sla s
        JOIN bergamot.sla_rolling_period r ON (s.id = r.sla_id)
    ) sla_period
    JOIN LATERAL (
        SELECT count(*) AS alerts, (count(*) FILTER (WHERE a.false_positive)) AS false_positives, sum(coalesce(a.recovered_at, now()) - a.raised) AS downtime
        FROM bergamot.alert a
        WHERE a.check_id = sla_period.sla_check_id
        AND   a.raised BETWEEN now() - sla_period.period_interval AND now()
    ) downtime ON (true)
    JOIN (
        SELECT id, name, summary, description, group_ids FROM bergamot.host
        UNION ALL
        SELECT id, name, summary, description, group_ids FROM bergamot.service
        UNION ALL
        SELECT id, name, summary, description, group_ids FROM bergamot.trap
        UNION ALL
        SELECT id, name, summary, description, group_ids FROM bergamot.cluster
        UNION ALL
        SELECT id, name, summary, description, group_ids FROM bergamot.resource
    ) "check" ON ("check".id = sla_period.sla_check_id)
    WHERE "check".id = p_check_id
    ORDER BY check_name, sla_name, period_start DESC;
END;
$$;
