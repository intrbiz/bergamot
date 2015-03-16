package com.intrbiz.bergamot.crypto.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

@SuppressWarnings("deprecation")
public class RSAUtil
{   
    public enum KeyType {
        CA,
        INTERMEDIATE,
        SERVER,
        CLIENT
    }
    
    public static KeyPair generateRSAKeyPair()
    {
        return generateRSAKeyPair(2048);
    }
    
    public static KeyPair generateRSAKeyPair(int size)
    {
        try
        {
            KeyPairGenerator jenny = KeyPairGenerator.getInstance("RSA");
            jenny.initialize(size, new SecureRandom());
            // generate the pair
            KeyPair pair = jenny.generateKeyPair();
            return pair;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static String buildDN(String country, String state, String locality, String org, String orgUnit, String commonName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("C=").append(country).append(", ");
        sb.append("ST=").append(state).append(", ");
        sb.append("L=").append(locality).append(", ");
        sb.append("O=").append(org).append(", ");
        if (orgUnit != null) sb.append("OU=").append(orgUnit).append(", ");
        sb.append("CN=").append(commonName);
        return sb.toString();
    }
    
    public static CertificatePair generateCertificate(String DN, SerialNum serial, int days, int keySize, KeyType type, CertificatePair issuer) throws Exception
    {
        return generateCertificate(DN, serial, days, keySize, type, null, issuer);
    }
    
    public static CertificatePair generateCertificate(String DN, SerialNum serial, int days, int keySize, KeyType type, PublicKey key, CertificatePair issuer) throws Exception
    {
        // validate
        if ((KeyType.INTERMEDIATE == type || KeyType.CLIENT == type || KeyType.SERVER == type) && issuer == null) throw new IllegalArgumentException("Issue must be given to sign requested key type");
        if (issuer == null && key != null) throw new IllegalArgumentException("When signing a given public key, an issuer must be given");
        // generate the key pair
        KeyPair pair = null;
        if (key == null)
        {
            pair = generateRSAKeyPair(keySize);
            key = pair.getPublic();
        }
        // expiry time
        Calendar now = Calendar.getInstance();
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_YEAR, days);
        // generate the certificate
        X509Name subject = new X509Name(DN);
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(serial.toBigInt());
        certGen.setIssuerDN(issuer == null ? subject : ((X509Principal) issuer.getCertificate().getSubjectDN()));
        certGen.setNotBefore(now.getTime());
        certGen.setNotAfter(expiry.getTime());
        certGen.setSubjectDN(subject);
        certGen.setPublicKey(key);
        certGen.setSignatureAlgorithm("SHA256WithRSA");
        // set extensions
        certGen.addExtension(X509Extension.subjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(key));
        if (issuer == null)
        {
            certGen.addExtension(X509Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(key));
        }
        else
        {
            certGen.addExtension(X509Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(issuer.getCertificate()));
        }
        if (KeyType.CA == type || KeyType.INTERMEDIATE == type)
        {
            certGen.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(true));
            certGen.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.cRLSign | KeyUsage.dataEncipherment | KeyUsage.digitalSignature | KeyUsage.keyCertSign));
            certGen.addExtension(X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.anyExtendedKeyUsage));
        }
        else if (KeyType.SERVER == type)
        {
            certGen.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(false));
            certGen.addExtension(X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        }
        else if (KeyType.CLIENT == type)
        {
            certGen.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(false));
            certGen.addExtension(X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
        }
        // go go go
        X509Certificate theCert = certGen.generate(issuer == null ? pair.getPrivate() : issuer.getKey());
        // check
        theCert.verify(issuer == null ? key : issuer.getCertificate().getPublicKey());
        // encode
        return new CertificatePair(theCert, pair == null ? null : pair.getPrivate());
    }
}
