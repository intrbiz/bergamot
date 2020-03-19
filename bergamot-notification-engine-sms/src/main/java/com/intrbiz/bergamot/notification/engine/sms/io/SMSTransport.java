package com.intrbiz.bergamot.notification.engine.sms.io;

import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;

public interface SMSTransport
{   
    void configure(NotificationEngineContext engineContext) throws Exception;
    
    SentSMS sendSMS(SMSMessage message) throws SMSTransportException;
}
