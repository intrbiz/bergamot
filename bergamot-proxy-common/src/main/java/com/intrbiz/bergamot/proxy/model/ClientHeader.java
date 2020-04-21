package com.intrbiz.bergamot.proxy.model;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.proxy.KeyResolver;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

public class ClientHeader
{
    private static final Logger logger = Logger.getLogger(ClientHeader.class);
    
    private static final long AUTHENTICATION_GRACE_SECONDS = 30;
    
    public static final class BergamotHeaderNames
    {
        public static final String HEADER_PREFIX = "x-bergamot-";
        
        public static final int HEADER_PREFIX_LEN = HEADER_PREFIX.length();
        
        public static final String ID = HEADER_PREFIX + "id";
        
        public static final String TIMESTAMP = HEADER_PREFIX + "timestamp";
        
        public static final String KEY_ID = HEADER_PREFIX + "key-id";
        
        public static final String PROXY_FOR = HEADER_PREFIX + "proxy-for";
        
        public static final String ENGINES = HEADER_PREFIX + "engines";
        
        public static final String WORKER_POOL = HEADER_PREFIX + "-worker-pool";
    }
    
    public static final class BergamotHeaderValues
    {
        public static final String PROXY_FOR_WORKER = "worker";
        
        public static final String PROXY_FOR_NOTIFIER = "notifier";
        
        
    }
    
    public static final class HttpHeaders
    {
        public static final String X_FORWARDED_FOR = "x-forwarded-for";
    }
    
    private final UUID id;
    
    private final String address;
    
    private final String userAgent;
    
    private final Map<String, String> attributes;
    
    private final long authenticationTimestamp;
    
    private final UUID keyId;
    
    private String signature;
    
    private AuthenticationKey key;

    public ClientHeader(UUID id, String address, String userAgent, Map<String, String> attributes, long authenticationTimestamp, UUID keyId, String signature)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.address = Objects.requireNonNull(address);
        this.userAgent = Objects.requireNonNull(userAgent);
        this.attributes = Objects.requireNonNull(attributes);
        this.authenticationTimestamp = authenticationTimestamp;
        this.keyId = Objects.requireNonNull(keyId);
        this.signature = Objects.requireNonNull(signature);
    }

    public UUID getId()
    {
        return this.id;
    }
    
    public String getAddress()
    {
        return this.address;
    }

    public String getUserAgent()
    {
        return this.userAgent;
    }

    public Set<String> getAttributeNames()
    {
        return this.attributes.keySet();
    }
    
    public String getAttribute(String name)
    {
        return this.attributes.get(name);
    }
    
    public String getProxyFor()
    {
        return this.getAttribute(BergamotHeaderNames.PROXY_FOR);
    }
    
    public String getWorkerPool()
    {
        return this.getAttribute(BergamotHeaderNames.WORKER_POOL);
    }
    
    public Set<String> getEngines()
    {
        String engines = this.getAttribute(BergamotHeaderNames.ENGINES);
        return engines == null ? 
                Collections.emptySet() : 
                Arrays.stream(engines.split(",")).map(String::trim).collect(Collectors.toSet());
    }
    
    public long getAuthenticationTimestamp()
    {
        return this.authenticationTimestamp;
    }

    public UUID getKeyId()
    {
        return this.keyId;
    }
    
    public String getSignature()
    {
        return this.signature;
    }
    
    public AuthenticationKey getKey()
    {
        return this.key;
    }
    
    public Set<UUID> getSiteIds()
    {
        return this.key == null || this.key.getSiteId() == null ? 
                Collections.emptySet() : 
                Collections.singleton(this.key.getSiteId());
    }

    public CompletionStage<Boolean> authenticate(KeyResolver keyResolver)
    {
        if (Math.abs((System.currentTimeMillis() / 1000) - this.authenticationTimestamp) < AUTHENTICATION_GRACE_SECONDS)
        {
            return keyResolver.resolveKey(this.keyId).thenApply((resolvedKey) -> {
                if (resolvedKey != null)
                {
                    this.key = resolvedKey;
                    try
                    {
                        return resolvedKey.checkBase64(this.authenticationTimestamp, this.id, this.attributes, this.signature);
                    }
                    catch (InvalidKeyException | NoSuchAlgorithmException e)
                    {
                        logger.warn("Error checking signature", e);
                    }
                }
                else
                {
                    logger.warn("Failed to resolve authentication key: " + this.keyId);
                }
                return false;
            });
        }
        else
        {
            logger.warn("Authentication timestamp is not within the grace period");
        }
        return CompletableFuture.completedFuture(false);
    }
    
    public static ClientHeader fromRequest(HttpRequest req, SocketAddress remoteAddress) throws IllegalArgumentException
    {
        UUID  id = UUID.fromString(requireNonEmpty(req.headers().get(BergamotHeaderNames.ID), BergamotHeaderNames.ID));
        long authenticationTimestamp = Long.parseLong(requireNonEmpty(req.headers().get(BergamotHeaderNames.TIMESTAMP), BergamotHeaderNames.TIMESTAMP));
        UUID keyId = UUID.fromString(requireNonEmpty(req.headers().get(BergamotHeaderNames.KEY_ID), BergamotHeaderNames.KEY_ID));
        String userAgent = Util.coalesce(req.headers().get(HttpHeaderNames.USER_AGENT), "Unknown");
        String sig = requireNonEmpty(req.headers().get(HttpHeaderNames.AUTHORIZATION), HttpHeaderNames.AUTHORIZATION.toString());
        Map<String, String> attrs = new HashMap<>();
        for (String name : req.headers().names())
        {
            if (name.startsWith(BergamotHeaderNames.HEADER_PREFIX) && (!(BergamotHeaderNames.ID.equals(name) || BergamotHeaderNames.TIMESTAMP.equals(name) || BergamotHeaderNames.KEY_ID.equals(name))))
            {
                attrs.put(name, req.headers().get(name));
            }
        }
        String address = ((InetSocketAddress) remoteAddress).getAddress().getHostAddress();
        String forwardedFor = req.headers().get(HttpHeaders.X_FORWARDED_FOR);
        if (! Util.isEmpty(forwardedFor))
        {
            int idx = forwardedFor.indexOf(',');
            address = idx > 0 ? forwardedFor.substring(0, idx) : address;
        }
        return new ClientHeader(id, address, userAgent, attrs, authenticationTimestamp, keyId, sig);
    }
    
    private static String requireNonEmpty(String value, String name) throws IllegalArgumentException
    {
        if (Util.isEmpty(value))
            throw new IllegalArgumentException("The header '" + name + "' must be provided");
        return value;
    }
}
