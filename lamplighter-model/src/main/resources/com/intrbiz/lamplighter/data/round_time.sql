CREATE OR REPLACE FUNCTION lamplighter.round_time(p_time TIMESTAMP WITH TIME ZONE, p_rnd BIGINT) 
RETURNS TIMESTAMP WITH TIME ZONE 
LANGUAGE SQL 
AS $$ 
 SELECT to_timestamp((floor(extract('epoch' FROM $1) / ($2/1000)::REAL)::BIGINT) * ($2/1000)) 
$$;