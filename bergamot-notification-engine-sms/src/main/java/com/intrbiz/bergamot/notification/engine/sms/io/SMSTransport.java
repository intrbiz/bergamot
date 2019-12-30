package com.intrbiz.bergamot.notification.engine.sms.io;

import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;
import com.intrbiz.configuration.Configuration;

public interface SMSTransport
{   
    void configure(Configuration cfg) throws Exception;
    
    SentSMS sendSMS(SMSMessage message) throws SMSTransportException;
}
