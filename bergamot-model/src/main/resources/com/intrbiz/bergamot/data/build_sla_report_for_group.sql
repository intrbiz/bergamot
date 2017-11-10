CREATE OR REPLACE FUNCTION bergamot.build_sla_report_for_group(p_group_id UUID, p_status BOOLEAN)
RETURNS SETOF bergamot.t_sla_report
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
    sla_period.sla_target :: double precision,
    sla_period.period_name,
    sla_period.period_summary,
    sla_period.period_description,
    sla_period.period_start,
    sla_period.period_end,
    downtime.alerts :: integer AS period_alerts,
    downtime.false_positives :: integer AS period_false_positives,
    (greatest(extract(epoch FROM (sla_period.period_end - sla_period.period_start)) - coalesce(extract(epoch FROM downtime.downtime), 0), 0.0)::DOUBLE PRECISION / extract(epoch FROM (sla_period.period_end - sla_period.period_start))::DOUBLE PRECISION) AS period_value,
    ((greatest(extract(epoch FROM (sla_period.period_end - sla_period.period_start)) - coalesce(extract(epoch FROM downtime.downtime), 0), 0.0)::DOUBLE PRECISION / extract(epoch FROM (sla_period.period_end - sla_period.period_start))::DOUBLE PRECISION) < sla_period.sla_target) AS period_breached
    FROM (
        SELECT 
         s.id AS sla_id,
         s.check_id AS sla_check_id,
         s.name AS sla_name,
         s.summary AS sla_summary,
         s.description AS sla_description,
         s.target AS sla_target,
         p.name AS period_name,
         p.summary AS period_summary,
         p.description AS period_description,
         p.status AS period_status,
         p.start as period_start,
         p.end as period_end
        FROM bergamot.sla s
        JOIN (
             SELECT 
              sla_id, name, summary, description, status,
              (now() - (CASE WHEN granularity = 0 THEN '1 day' WHEN granularity = 1 THEN '1 week' 
               WHEN granularity = 2 THEN '1 month' WHEN granularity = 3 THEN '1 year' ELSE null END)::INTERVAL) AS "start",
              now() AS "end"
             FROM bergamot.sla_rolling_period
             UNION ALL
             SELECT sla_id, name, summary, description, status, "start", "end"
             FROM bergamot.sla_fixed_period
        ) p ON (s.id = p.sla_id)
    ) sla_period
    JOIN LATERAL (
        SELECT 
         count(*) AS alerts, 
         (count(*) FILTER (WHERE a.false_positive)) AS false_positives, 
         sum(coalesce(a.recovered_at, now()) - a.raised) FILTER (WHERE a.false_positive IS NULL OR NOT a.false_positive) AS downtime
        FROM bergamot.alert a
        WHERE a.check_id = sla_period.sla_check_id
        AND tstzrange(a.raised, a.recovered_at, '[]') && tstzrange(sla_period.period_start, sla_period.period_end, '[]')
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
    WHERE p_group_id = ANY("check".group_ids)
    AND (NOT p_status OR sla_period.period_status = p_status)
    ORDER BY check_name, sla_name, period_start DESC;
END;
$$;