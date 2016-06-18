package com.intrbiz.bergamot.agent.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.BergamotAgent;
import com.intrbiz.bergamot.agent.KeyStoreUtil;
import com.intrbiz.bergamot.agent.config.BergamotAgentCfg;
import com.intrbiz.bergamot.agent.config.CfgParameter;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationComplete;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationFailed;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationRequest;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationRequired;

public class AgentRegistrationHandler extends AbstractAgentHandler
{
    private Logger logger = Logger.getLogger(AgentRegistrationHandler.class);
    
    private UUID hostId;
    
    private String hostName;
    
    private KeyPair currentKeyPair;
    
    private long registrationStartedAt;

    public AgentRegistrationHandler()
    {
        super();
    }

    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] { AgentRegistrationRequired.class, AgentRegistrationComplete.class, AgentRegistrationFailed.class };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        if (request instanceof AgentRegistrationRequired)
        {
            synchronized (this) 
            {
                if (this.currentKeyPair == null)
                {
                    try
                    {
                        logger.info("Starting agent registration process");
                        registrationStartedAt = System.currentTimeMillis();
                        // first compute our host name
                        this.hostId = this.getHostId();
                        this.hostName = this.getHostName();
                        // now generate an RSA key pair
                        KeyPairGenerator jenny = KeyPairGenerator.getInstance("RSA");
                        jenny.initialize(2048, new SecureRandom());
                        this.currentKeyPair = jenny.generateKeyPair();
                        // ask for us to be registered
                        AgentRegistrationRequest regReq = new AgentRegistrationRequest();
                        regReq.setAgentId(this.hostId);
                        regReq.setCommonName(hostName);
                        regReq.setPublicKey(KeyStoreUtil.savePublicKey(this.currentKeyPair.getPublic()));
                        // fill out the registration request
                        return regReq;
                    }
                    catch (NoSuchAlgorithmException e)
                    {
                        throw new RuntimeException("Failed to generate RSA key", e);
                    }
                }
                else
                {
                    logger.info("Agent registration attempt already in progress, started: " + (System.currentTimeMillis() - registrationStartedAt) + " ms ago");
                }
            }
        }
        else if (request instanceof AgentRegistrationComplete)
        {
            AgentRegistrationComplete complete = ((AgentRegistrationComplete) request);
            logger.info("Successfully got signed agent certificate:\n" + complete.getCertificate());
            // build the new configuration file
            BergamotAgentCfg currentConfig = this.getAgent().getConfiguration();
            // set the id
            currentConfig.getParameters().clear();
            currentConfig.addParameter(new CfgParameter("agent-id", null, null, complete.getAgentId().toString()));
            // set the name
            currentConfig.setName(complete.getCommonName());
            // update the certificates
            currentConfig.setCertificate(complete.getCertificate());
            currentConfig.setKey(KeyStoreUtil.saveKey(this.currentKeyPair.getPrivate()));
            currentConfig.addParameter(new CfgParameter("public-key", null, null, KeyStoreUtil.savePublicKey(this.currentKeyPair.getPublic())));
            // save the config
            try
            {
                BergamotAgent.saveConfig(currentConfig);
            }
            catch (Exception e)
            {
                logger.error("Failed to save Bergamot Agent configuration after registration");
            }
            // restart the agent to reconnect
            this.getAgent().restart(currentConfig);
        }
        else if (request instanceof AgentRegistrationFailed)
        {
            logger.error("Failed to get signed agent certificate: " + ((AgentRegistrationFailed) request).getErrorCode() + " " + ((AgentRegistrationFailed) request).getMessage());
            this.currentKeyPair = null;
        }
        else
        {
            logger.warn("Ignoring unexpected registration message: " + request);
        }
        return null;
    }
    
    private String getHostName()
    {
        // first off allow for the host name to be overridden using a bergamot parameter
        String hostName = System.getProperty("bergamot.host.name");
        if (hostName == null || hostName.length() <= 0)
        {
            // next try the gerald host parameter
            hostName = System.getProperty("gerald.host.name");
            if (hostName == null || hostName.length() <= 0)
            {
                try
                {
                    // look at localhost address
                    hostName = InetAddress.getLocalHost().getHostName();
                }
                catch (UnknownHostException e)
                {
                    throw new RuntimeException("Unable to get host name, please set the proprty: gerald.host.name.");
                }
            }
        }
        return hostName;
    }
    
    private UUID getHostId()
    {        
        String hostId = System.getProperty("bergamot.host.id");
        if (hostId == null || hostId.length() <= 0)
        {
            hostId = this.readHostIdFile("bergamot.host.id");
        }
        if (hostId == null || hostId.length() <= 0)
        {
            hostId = System.getProperty("gerald.host.id");
        }
        if (hostId == null || hostId.length() <= 0)
        {
            hostId = this.readHostIdFile("gerald.host.id");
        }
        if (hostId == null || hostId.length() <= 0)
        {
            hostId = UUID.randomUUID().toString();
        }
        return UUID.fromString(hostId);
    }
    
    private String readHostIdFile(String fileName)
    {
        try
        {
            File file = new File("/etc/" + fileName);
            if (file.exists())
            {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try
                {
                    String id = reader.readLine();
                    if (id != null) return UUID.fromString(id.trim()).toString();
                }
                finally
                {
                    reader.close();
                }
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }
}
