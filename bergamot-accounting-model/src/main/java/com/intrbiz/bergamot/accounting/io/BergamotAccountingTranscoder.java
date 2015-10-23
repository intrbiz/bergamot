package com.intrbiz.bergamot.accounting.io;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.intrbiz.Util;
import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;
import com.intrbiz.bergamot.accounting.model.ExecuteCheckAccountingEvent;
import com.intrbiz.bergamot.accounting.model.LoginAccountingEvent;
import com.intrbiz.bergamot.accounting.model.ProcessResultAccountingEvent;
import com.intrbiz.bergamot.accounting.model.SendNotificationAccountingEvent;
import com.intrbiz.bergamot.accounting.model.SendNotificationToContactAccountingEvent;
import com.intrbiz.bergamot.accounting.model.SignAgentAccountingEvent;

public class BergamotAccountingTranscoder
{
    private static final BergamotAccountingTranscoder DEFAULT = new BergamotAccountingTranscoder();
    
    public static final BergamotAccountingTranscoder getDefault()
    {
        return DEFAULT;
    }
    
    private ConcurrentMap<UUID, Supplier<? extends BergamotAccountingEvent>> eventTypes = new ConcurrentHashMap<UUID, Supplier<? extends BergamotAccountingEvent>>();
    
    public BergamotAccountingTranscoder()
    {
        super();
        // register default types
        this.registerType(ExecuteCheckAccountingEvent.TYPE_ID, ExecuteCheckAccountingEvent::new);
        this.registerType(ProcessResultAccountingEvent.TYPE_ID, ProcessResultAccountingEvent::new);
        this.registerType(SendNotificationAccountingEvent.TYPE_ID, SendNotificationAccountingEvent::new);
        this.registerType(LoginAccountingEvent.TYPE_ID, LoginAccountingEvent::new);
        this.registerType(SignAgentAccountingEvent.TYPE_ID, SignAgentAccountingEvent::new);
        this.registerType(SendNotificationToContactAccountingEvent.TYPE_ID, SendNotificationToContactAccountingEvent::new);
    }
    
    public void registerType(UUID id, Supplier<? extends BergamotAccountingEvent> factory)
    {
        this.eventTypes.putIfAbsent(id, factory);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BergamotAccountingEvent> T create(UUID id)
    {
        Supplier<? extends BergamotAccountingEvent> supplier = this.eventTypes.get(id);
        return supplier == null ? null : (T) supplier.get();
    }
    
    public void encode(BergamotAccountingEvent event, ByteBuffer into)
    {
        Objects.requireNonNull(event);
        Objects.requireNonNull(event.getTypeId());
        into.putLong(event.getTypeId().getMostSignificantBits());
        into.putLong(event.getTypeId().getLeastSignificantBits());
        event.pack(into);
    }
    
    public <T extends BergamotAccountingEvent> T decode(ByteBuffer from)
    {
        UUID typeId = new UUID(from.getLong(), from.getLong());
        T event = this.create(typeId);
        if (event == null) throw new RuntimeException("Failed to decode type " + typeId);
        event.unpack(from);
        return event;
    }
    
    public String encodeToString(BergamotAccountingEvent event)
    {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        this.encode(event, buffer);
        buffer.flip();
        ByteBuffer encoded = Base64.getUrlEncoder().withoutPadding().encode(buffer);
        return new String(encoded.array(), encoded.arrayOffset(), encoded.remaining(), Util.UTF8);
    }
    
    public <T extends BergamotAccountingEvent> T decodeFromString(String event)
    {
        byte[] decoded = Base64.getUrlDecoder().decode(event);
        return this.decode(ByteBuffer.wrap(decoded));
    }
}
