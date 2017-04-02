package com.intrbiz.bergamot.notification.engine.sms.io;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;
import com.intrbiz.configuration.Configurable;

public interface SMSTransport extends Configurable<NotificationEngineCfg>
{    
    SentSMS sendSMS(SMSMessage message) throws SMSTransportException;
}
