package com.intrbiz.bergamot.ui.util;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.format.ThreadSafeSimpleDateFormat;
import com.intrbiz.json.JSBoolean;
import com.intrbiz.json.JSObject;
import com.intrbiz.json.JSString;
import com.intrbiz.json.reader.JSONReader;

import io.netty.util.CharsetUtil;


public class RecaptchaUtil
{
    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    
    private static final long MAX_TIME_DIFF = TimeUnit.MINUTES.toMillis(5);
    
    private static final ThreadSafeSimpleDateFormat TIMESTAMP_FORMAT = new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    
    private static final Logger logger = Logger.getLogger(RecaptchaUtil.class);
    
    public static boolean verify(String requestHostname, String secretKey, String response, String remoteIp)
    {
        Objects.requireNonNull(secretKey);
        Objects.requireNonNull(response);
        // the form data
        Form form = Form.form()
        .add("secret", secretKey)
        .add("response", response);
        // optionally add remote ip
        if (! Util.isEmpty(remoteIp))
            form.add("remoteip", remoteIp);
        // make the request
        try
        {
            HttpResponse res = Request.Post(RECAPTCHA_VERIFY_URL).bodyForm(form.build()).execute().returnResponse();
            if (res.getStatusLine().getStatusCode() == 200)
            {
                JSObject vro = (JSObject) JSONReader.parse(EntityUtils.toString(res.getEntity(), CharsetUtil.UTF_8));
                logger.debug("Got reCAPTCHA verify response: " + vro);
                if (((JSBoolean) vro.getMember("success")).isBoolean())
                {
                    logger.info("Got successful reCAPTCHA verify response");
                    String hostname = ((JSString) vro.getMember("hostname")).getString();
                    Date timestamp = TIMESTAMP_FORMAT.parse(((JSString) vro.getMember("challenge_ts")).getString().replaceAll("Z$", "+0000"));
                    long timeDiff = System.currentTimeMillis() - timestamp.getTime();
                    logger.info("Verfing reCAPTCHA hostname: " + hostname + " == " + requestHostname + " and time diff: " + timeDiff);
                    return requestHostname.equals(hostname) && timeDiff > 0 && timeDiff < MAX_TIME_DIFF;
                }
            }
            else
            {
                logger.error("Got a non-ok response when verifying reCAPTCHA");
            }
        }
        catch (Exception e)
        {
            logger.error("Error verifying reCAPTCHA", e);
        }
        return false;
    }
}
