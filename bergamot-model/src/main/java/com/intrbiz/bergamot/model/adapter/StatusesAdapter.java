package com.intrbiz.bergamot.model.adapter;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class StatusesAdapter implements DBTypeAdapter<List<String>, List<Status>>
{    
    @Override
    public List<String> toDB(List<Status> value)
    {
        return value == null ? null : value.stream().map(Status::toString).collect(Collectors.toList());
    }

    @Override
    public List<Status> fromDB(List<String> value)
    {
        return value == null ? null : value.stream().map(Status::valueOf).collect(Collectors.toList());
    }
}
