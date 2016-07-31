package com.intrbiz.bergamot.crypto.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

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
        // not before
        Calendar now = Calendar.getInstance();
        // not after
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_YEAR, days);
        // subject DN
        X500Name subjectDN = new X500Name(DN);
        // issuer DN
        X500Name issuerDN = issuer == null ? subjectDN : new X500Name(issuer.getCertificate().getSubjectX500Principal().getName());
        
        // build the certificate
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerDN, serial.toBigInt(), now.getTime(), expiry.getTime(), subjectDN, key);
        // set extensions
        JcaX509ExtensionUtils utils = new JcaX509ExtensionUtils();
        // subject public key
        builder.addExtension(Extension.subjectKeyIdentifier, false, utils.createSubjectKeyIdentifier(key));
        // issuer public key
        if (issuer == null)
        {
            builder.addExtension(Extension.authorityKeyIdentifier, false, utils.createAuthorityKeyIdentifier(key));
        }
        else
        {
            builder.addExtension(Extension.authorityKeyIdentifier, false, utils.createAuthorityKeyIdentifier(issuer.getCertificate()));
        }
        // constraints
        if (KeyType.CA == type || KeyType.INTERMEDIATE == type)
        {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.cRLSign | KeyUsage.dataEncipherment | KeyUsage.digitalSignature | KeyUsage.keyCertSign));
            builder.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.anyExtendedKeyUsage));
        }
        else if (KeyType.SERVER == type)
        {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            builder.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        }
        else if (KeyType.CLIENT == type)
        {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            builder.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
        }
        // the signer
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuer == null ? pair.getPrivate() : issuer.getKey()); 
        // go go go
        X509CertificateHolder theCertHolder = builder.build(signer);
        // extract the actual fucking certificate
        X509Certificate theCert = new JcaX509CertificateConverter().getCertificate(theCertHolder);
        // check
        theCert.verify(issuer == null ? key : issuer.getCertificate().getPublicKey());
        // encode
        return new CertificatePair(theCert, pair == null ? null : pair.getPrivate());
    }
}
