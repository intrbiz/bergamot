package com.intrbiz.bergamot.check.http;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Information about an X509 certificate in an easy to access form
 */
public class CertInfo
{   
    private NameInfo subject;
    
    private NameInfo issuer;
    
    private int keySize;
    
    private String keyAlg;
    
    private String sigAlg;
    
    private Date notBefore;
    
    private Date notAfter;
    
    private Set<SubjectAltName> subjectAltNames = new LinkedHashSet<SubjectAltName>();
    
    @SuppressWarnings("restriction")
    public CertInfo(X509Certificate cert) throws IOException
    {
        // basic details
        this.sigAlg = cert.getSigAlgName();
        this.notBefore = cert.getNotBefore();
        this.notAfter = cert.getNotAfter();
        /*
         * Get the subject and issuer names, we need to deal with internal classes
         */
        this.subject = new NameInfo((sun.security.x509.X500Name) cert.getSubjectDN());
        this.issuer = new NameInfo((sun.security.x509.X500Name) cert.getIssuerDN());
        /*
         * Get some information about the key
         */
        PublicKey key = cert.getPublicKey();
        this.keyAlg = key.getAlgorithm();
        if (key instanceof sun.security.rsa.RSAPublicKeyImpl)
        {
            this.keySize = ((sun.security.rsa.RSAPublicKeyImpl) key).getModulus().bitLength();
        }
        /*
         * Extract the SANs
         */
        try
        {
            /*
             * Yes, this really does return a collection of two element array lists!
             * - The first element is the Integer type of the name (2 == DNS NAME)
             * - The second element is the String name 
             */
            Collection<List<?>> sans = cert.getSubjectAlternativeNames();
            if (sans != null)
            {
                for (List<?> san : sans)
                {
                    this.subjectAltNames.add(new SubjectAltName((Integer) san.get(0), (String) san.get(1)));
                }
            }
        }
        catch (CertificateParsingException e)
        {
            Logger.getLogger(CertInfo.class).error("Error extracting SAN information", e);
        }
    }

    public NameInfo getSubject()
    {
        return subject;
    }

    public NameInfo getIssuer()
    {
        return issuer;
    }

    public int getKeySize()
    {
        return keySize;
    }

    public String getKeyAlg()
    {
        return keyAlg;
    }

    public String getSigAlg()
    {
        return sigAlg;
    }

    public Date getNotBefore()
    {
        return notBefore;
    }

    public Date getNotAfter()
    {
        return notAfter;
    }

    public Set<SubjectAltName> getSubjectAltNames()
    {
        return Collections.unmodifiableSet(subjectAltNames);
    }
    
    public String toString()
    {
        return "cert-info {\n" +
                "subject: " + this.subject + "\n" +
                "issuer: " + this.issuer + "\n" +
                "not-before: " + this.notBefore + "\n" +
                "not-after: " + this.notAfter + "\n" +
                "key: " + this.keyAlg + " (" + this.keySize + ")\n" +
                "sig: " + this.sigAlg + "\n" +
                "subject-alt-names: " + this.subjectAltNames + "\n" +
               "}";
    }
}
