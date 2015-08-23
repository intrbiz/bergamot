package com.intrbiz.bergamot.crypto.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public final class TLSConstants
{
    public static final class PROTOCOLS
    {
        public static final String SSLv3 = "SSLv3";
        
        public static final String TLSv1 = "TLSv1";
        
        public static final String TLSv1_1 = "TLSv1.1";
        
        public static final String TLSv1_2 = "TLSv1.2";
        
        public static final String[] SAFE_PROTOCOLS = { TLSv1, TLSv1_1, TLSv1_2 };
        
        public static final String[] ALL_PROTOCOLS  = { SSLv3, TLSv1, TLSv1_1, TLSv1_2 };
    }
    
    public static final class CIPHERS
    {
        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = new CipherInfo("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = new CipherInfo("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "ECDHE_RSA");

        public static final CipherInfo TLS_RSA_WITH_AES_256_CBC_SHA256 = new CipherInfo("TLS_RSA_WITH_AES_256_CBC_SHA256", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 = new CipherInfo("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 = new CipherInfo("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", "ECDH_RSA");

        public static final CipherInfo TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 = new CipherInfo("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", "DHE_RSA");

        public static final CipherInfo TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 = new CipherInfo("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", "DHE_DSS");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "ECDHE_RSA");

        public static final CipherInfo TLS_RSA_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_RSA_WITH_AES_256_CBC_SHA", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", "ECDH_RSA");

        public static final CipherInfo TLS_DHE_RSA_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "DHE_RSA");

        public static final CipherInfo TLS_DHE_DSS_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", "DHE_DSS");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "ECDHE_RSA");

        public static final CipherInfo TLS_RSA_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_RSA_WITH_AES_128_CBC_SHA256", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", "ECDH_RSA");

        public static final CipherInfo TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "DHE_RSA");

        public static final CipherInfo TLS_DHE_DSS_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", "DHE_DSS");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "ECDHE_RSA");

        public static final CipherInfo TLS_RSA_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_RSA_WITH_AES_128_CBC_SHA", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "ECDH_RSA");

        public static final CipherInfo TLS_DHE_RSA_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "DHE_RSA");

        public static final CipherInfo TLS_DHE_DSS_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "DHE_DSS");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = new CipherInfo("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_RC4_128_SHA = new CipherInfo("TLS_ECDHE_RSA_WITH_RC4_128_SHA", "ECDHE_RSA");

        public static final CipherInfo SSL_RSA_WITH_RC4_128_SHA = new CipherInfo("SSL_RSA_WITH_RC4_128_SHA", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_RC4_128_SHA = new CipherInfo("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_RC4_128_SHA = new CipherInfo("TLS_ECDH_RSA_WITH_RC4_128_SHA", "ECDH_RSA");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "ECDHE_RSA");

        public static final CipherInfo TLS_RSA_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_RSA_WITH_AES_256_GCM_SHA384", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", "ECDH_RSA");

        public static final CipherInfo TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", "DHE_RSA");

        public static final CipherInfo TLS_DHE_DSS_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", "DHE_DSS");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "ECDHE_RSA");

        public static final CipherInfo TLS_RSA_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_RSA_WITH_AES_128_GCM_SHA256", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", "ECDH_RSA");

        public static final CipherInfo TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "DHE_RSA");

        public static final CipherInfo TLS_DHE_DSS_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", "DHE_DSS");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = new CipherInfo("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = new CipherInfo("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "ECDHE_RSA");

        public static final CipherInfo SSL_RSA_WITH_3DES_EDE_CBC_SHA = new CipherInfo("SSL_RSA_WITH_3DES_EDE_CBC_SHA", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = new CipherInfo("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = new CipherInfo("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "ECDH_RSA");

        public static final CipherInfo SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA = new CipherInfo("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "DHE_RSA");

        public static final CipherInfo SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA = new CipherInfo("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "DHE_DSS");

        public static final CipherInfo SSL_RSA_WITH_RC4_128_MD5 = new CipherInfo("SSL_RSA_WITH_RC4_128_MD5", "RSA");

        public static final CipherInfo TLS_EMPTY_RENEGOTIATION_INFO_SCSV = new CipherInfo("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", null);

        public static final CipherInfo TLS_DH_anon_WITH_AES_256_GCM_SHA384 = new CipherInfo("TLS_DH_anon_WITH_AES_256_GCM_SHA384", "DH_anon");

        public static final CipherInfo TLS_DH_anon_WITH_AES_128_GCM_SHA256 = new CipherInfo("TLS_DH_anon_WITH_AES_128_GCM_SHA256", "DH_anon");

        public static final CipherInfo TLS_DH_anon_WITH_AES_256_CBC_SHA256 = new CipherInfo("TLS_DH_anon_WITH_AES_256_CBC_SHA256", "DH_anon");

        public static final CipherInfo TLS_ECDH_anon_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", "ECDH_anon");

        public static final CipherInfo TLS_DH_anon_WITH_AES_256_CBC_SHA = new CipherInfo("TLS_DH_anon_WITH_AES_256_CBC_SHA", "DH_anon");

        public static final CipherInfo TLS_DH_anon_WITH_AES_128_CBC_SHA256 = new CipherInfo("TLS_DH_anon_WITH_AES_128_CBC_SHA256", "DH_anon");

        public static final CipherInfo TLS_ECDH_anon_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", "ECDH_anon");

        public static final CipherInfo TLS_DH_anon_WITH_AES_128_CBC_SHA = new CipherInfo("TLS_DH_anon_WITH_AES_128_CBC_SHA", "DH_anon");

        public static final CipherInfo TLS_ECDH_anon_WITH_RC4_128_SHA = new CipherInfo("TLS_ECDH_anon_WITH_RC4_128_SHA", "ECDH_anon");

        public static final CipherInfo SSL_DH_anon_WITH_RC4_128_MD5 = new CipherInfo("SSL_DH_anon_WITH_RC4_128_MD5", "DH_anon");

        public static final CipherInfo TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = new CipherInfo("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", "ECDH_anon");

        public static final CipherInfo SSL_DH_anon_WITH_3DES_EDE_CBC_SHA = new CipherInfo("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", "DH_anon");

        public static final CipherInfo TLS_RSA_WITH_NULL_SHA256 = new CipherInfo("TLS_RSA_WITH_NULL_SHA256", "RSA");

        public static final CipherInfo TLS_ECDHE_ECDSA_WITH_NULL_SHA = new CipherInfo("TLS_ECDHE_ECDSA_WITH_NULL_SHA", "ECDHE_ECDSA");

        public static final CipherInfo TLS_ECDHE_RSA_WITH_NULL_SHA = new CipherInfo("TLS_ECDHE_RSA_WITH_NULL_SHA", "ECDHE_RSA");

        public static final CipherInfo SSL_RSA_WITH_NULL_SHA = new CipherInfo("SSL_RSA_WITH_NULL_SHA", "RSA");

        public static final CipherInfo TLS_ECDH_ECDSA_WITH_NULL_SHA = new CipherInfo("TLS_ECDH_ECDSA_WITH_NULL_SHA", "ECDH_ECDSA");

        public static final CipherInfo TLS_ECDH_RSA_WITH_NULL_SHA = new CipherInfo("TLS_ECDH_RSA_WITH_NULL_SHA", "ECDH_RSA");

        public static final CipherInfo TLS_ECDH_anon_WITH_NULL_SHA = new CipherInfo("TLS_ECDH_anon_WITH_NULL_SHA", "ECDH_anon");

        public static final CipherInfo SSL_RSA_WITH_NULL_MD5 = new CipherInfo("SSL_RSA_WITH_NULL_MD5", "RSA");

        public static final CipherInfo SSL_RSA_WITH_DES_CBC_SHA = new CipherInfo("SSL_RSA_WITH_DES_CBC_SHA", "RSA");

        public static final CipherInfo SSL_DHE_RSA_WITH_DES_CBC_SHA = new CipherInfo("SSL_DHE_RSA_WITH_DES_CBC_SHA", "DHE_RSA");

        public static final CipherInfo SSL_DHE_DSS_WITH_DES_CBC_SHA = new CipherInfo("SSL_DHE_DSS_WITH_DES_CBC_SHA", "DHE_DSS");

        public static final CipherInfo SSL_DH_anon_WITH_DES_CBC_SHA = new CipherInfo("SSL_DH_anon_WITH_DES_CBC_SHA", "DH_anon");

        public static final CipherInfo SSL_RSA_EXPORT_WITH_RC4_40_MD5 = new CipherInfo("SSL_RSA_EXPORT_WITH_RC4_40_MD5", "RSA_EXPORT");

        public static final CipherInfo SSL_DH_anon_EXPORT_WITH_RC4_40_MD5 = new CipherInfo("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", "DH_anon_EXPORT");

        public static final CipherInfo SSL_RSA_EXPORT_WITH_DES40_CBC_SHA = new CipherInfo("SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "RSA_EXPORT");

        public static final CipherInfo SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = new CipherInfo("SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "DHE_RSA_EXPORT");

        public static final CipherInfo SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = new CipherInfo("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "DHE_DSS_EXPORT");

        public static final CipherInfo SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA = new CipherInfo("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", "DH_anon_EXPORT");

        public static final CipherInfo[] ALL_CIPHERS = { TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384, TLS_RSA_WITH_AES_256_CBC_SHA256, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384, TLS_DHE_RSA_WITH_AES_256_CBC_SHA256, TLS_DHE_DSS_WITH_AES_256_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_DSS_WITH_AES_256_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256, TLS_RSA_WITH_AES_128_CBC_SHA256, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256, TLS_DHE_RSA_WITH_AES_128_CBC_SHA256, TLS_DHE_DSS_WITH_AES_128_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, TLS_ECDHE_RSA_WITH_RC4_128_SHA, SSL_RSA_WITH_RC4_128_SHA, TLS_ECDH_ECDSA_WITH_RC4_128_SHA, TLS_ECDH_RSA_WITH_RC4_128_SHA, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_DSS_WITH_AES_256_GCM_SHA384, TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256, TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, TLS_DHE_DSS_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_RC4_128_MD5, TLS_EMPTY_RENEGOTIATION_INFO_SCSV, TLS_DH_anon_WITH_AES_256_GCM_SHA384, TLS_DH_anon_WITH_AES_128_GCM_SHA256, TLS_DH_anon_WITH_AES_256_CBC_SHA256, TLS_ECDH_anon_WITH_AES_256_CBC_SHA, TLS_DH_anon_WITH_AES_256_CBC_SHA, TLS_DH_anon_WITH_AES_128_CBC_SHA256, TLS_ECDH_anon_WITH_AES_128_CBC_SHA, TLS_DH_anon_WITH_AES_128_CBC_SHA, TLS_ECDH_anon_WITH_RC4_128_SHA, SSL_DH_anon_WITH_RC4_128_MD5, TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA, SSL_DH_anon_WITH_3DES_EDE_CBC_SHA, TLS_RSA_WITH_NULL_SHA256, TLS_ECDHE_ECDSA_WITH_NULL_SHA, TLS_ECDHE_RSA_WITH_NULL_SHA, SSL_RSA_WITH_NULL_SHA, TLS_ECDH_ECDSA_WITH_NULL_SHA, TLS_ECDH_RSA_WITH_NULL_SHA, TLS_ECDH_anon_WITH_NULL_SHA, SSL_RSA_WITH_NULL_MD5, SSL_RSA_WITH_DES_CBC_SHA, SSL_DHE_RSA_WITH_DES_CBC_SHA, SSL_DHE_DSS_WITH_DES_CBC_SHA, SSL_DH_anon_WITH_DES_CBC_SHA, SSL_RSA_EXPORT_WITH_RC4_40_MD5, SSL_DH_anon_EXPORT_WITH_RC4_40_MD5, SSL_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA, SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA };
        
        public static final String[]     ALL_CIPHER_NAMES = getCipherNames(ALL_CIPHERS);

        public static final CipherInfo[] ENABLED_CIPHERS = { TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384, TLS_RSA_WITH_AES_256_CBC_SHA256, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384, TLS_DHE_RSA_WITH_AES_256_CBC_SHA256, TLS_DHE_DSS_WITH_AES_256_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_DSS_WITH_AES_256_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256, TLS_RSA_WITH_AES_128_CBC_SHA256, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256, TLS_DHE_RSA_WITH_AES_128_CBC_SHA256, TLS_DHE_DSS_WITH_AES_128_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, TLS_ECDHE_RSA_WITH_RC4_128_SHA, SSL_RSA_WITH_RC4_128_SHA, TLS_ECDH_ECDSA_WITH_RC4_128_SHA, TLS_ECDH_RSA_WITH_RC4_128_SHA, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_DSS_WITH_AES_256_GCM_SHA384, TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256, TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, TLS_DHE_DSS_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_RC4_128_MD5, TLS_EMPTY_RENEGOTIATION_INFO_SCSV };
        
        public static final String[]     ENABLED_CIPHER_NAMES = getCipherNames(ENABLED_CIPHERS);

        public static final CipherInfo[] SAFE_CIPHERS = { TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384, TLS_RSA_WITH_AES_256_CBC_SHA256, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384, TLS_DHE_RSA_WITH_AES_256_CBC_SHA256, TLS_DHE_DSS_WITH_AES_256_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDH_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_DSS_WITH_AES_256_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256, TLS_RSA_WITH_AES_128_CBC_SHA256, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256, TLS_DHE_RSA_WITH_AES_128_CBC_SHA256, TLS_DHE_DSS_WITH_AES_128_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_DSS_WITH_AES_256_GCM_SHA384, TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256, TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, TLS_DHE_DSS_WITH_AES_128_GCM_SHA256, TLS_EMPTY_RENEGOTIATION_INFO_SCSV };
        
        public static final String[]     SAFE_CIPHER_NAMES = getCipherNames(SAFE_CIPHERS);

        public static final CipherInfo[] ALL_SUPPORTED_CIPHERS = getSupportedCiphers(ALL_CIPHERS);
        
        public static final String[]     ALL_SUPPORTED_CIPHER_NAMES = getCipherNames(ALL_SUPPORTED_CIPHERS);
        
        public static final CipherInfo[] ENABLED_SUPPORTED_CIPHERS = getSupportedCiphers(ENABLED_CIPHERS);
        
        public static final String[]     ENABLED_SUPPORTED_CIPHER_NAMES = getCipherNames(ENABLED_SUPPORTED_CIPHERS);
        
        public static final CipherInfo[] SAFE_SUPPORTED_CIPHERS = getSupportedCiphers(SAFE_CIPHERS);
        
        public static final String[]     SAFE_SUPPORTED_CIPHER_NAMES = getCipherNames(SAFE_SUPPORTED_CIPHERS);
    }
    
    /**
     * Filter the cipher list by what is supported in this current JVM 
     */
    public static final CipherInfo[] getSupportedCiphers(CipherInfo[] ciphers)
    {
        try
        {
            List<CipherInfo> supported = new ArrayList<CipherInfo>(ciphers.length);
            // set of ciphers this JVM supports
            HashSet<String> jvmSupported = new HashSet<String>();
            for (String jvmCipher: SSLContext.getDefault().getSupportedSSLParameters().getCipherSuites())
            {
                jvmSupported.add(jvmCipher);
            }
            // filter
            for (CipherInfo cipher : ciphers)
            {
                if (jvmSupported.contains(cipher.getName()))
                {
                    supported.add(cipher);
                }
            }
            return supported.toArray(new CipherInfo[supported.size()]);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static final String[] getCipherNames(CipherInfo[] ciphers)
    {
        String[] names = new String[ciphers.length];
        for (int i = 0; i < ciphers.length; i++)
        {
            names[i] = ciphers[i].getName();
        }
        return names;
    }
    
    public static CipherInfo getCipherInfo(String cipher)
    {
        for (int i = 0; i < CIPHERS.ALL_CIPHERS.length; i++)
        {
            if (cipher.equals(CIPHERS.ALL_CIPHERS[i].getName()))
                return CIPHERS.ALL_CIPHERS[i];
        }
        return null;
    }

    
    public static final class CipherInfo
    {
        private final String name;
        
        private final String auth;
        
        public CipherInfo(String name, String auth)
        {
            this.name = name;
            this.auth = auth;
        }
        
        public String getName()
        {
            return this.name;
        }
        
        public String getAuth()
        {
            return this.auth;
        }
        
        public String toString()
        {
            return this.name;
        }
    }
    
    public static void main(String[] args) throws Exception
    {   
        SSLEngine engine = SSLContext.getDefault().createSSLEngine();
        boolean ns;
        // constants class
        System.out.println("public static final class CIPHERS");
        System.out.println("{");
        // all cipher constants
        for (String cipher : engine.getSupportedCipherSuites())
        {
            if (! cipher.contains("KRB5"))
            {
                String auth = getAuth(cipher);        
                System.out.println("    public static final CipherInfo " + cipher + " = new CipherInfo(\"" + cipher + "\", " + (auth == null ? "null" : "\"" + auth + "\"") + ");");
                System.out.println();
            }
        }
        // all ciphers array
        System.out.print("    public static final CipherInfo[] ALL_CIPHERS = { ");
        ns = false;
        for (String cipher : engine.getSupportedCipherSuites())
        {
            if (! cipher.contains("KRB5"))
            {
                if (ns) System.out.print(", ");
                System.out.print(cipher);
                ns = true;
            }
        }
        System.out.println(" };");
        System.out.println();
        // names only
        System.out.print("    public static final String[]     ALL_CIPHER_NAMES = getCipherNames(ALL_CIPHERS);");
        System.out.println();
        // enabled ciphers array
        System.out.print("    public static final CipherInfo[] ENABLED_CIPHERS = { ");
        ns = false;
        for (String cipher : engine.getEnabledCipherSuites())
        {
            if (! cipher.contains("KRB5"))
            {
                if (ns) System.out.print(", ");
                System.out.print(cipher);
                ns = true;
            }
        }
        System.out.println(" };");
        System.out.println();
        // names only
        System.out.print("    public static final String[]     ENABLED_CIPHER_NAMES = getCipherNames(ENABLED_CIPHERS);");
        System.out.println();
        // safe ciphers array
        System.out.print("    public static final CipherInfo[] SAFE_CIPHERS = { ");
        ns = false;
        for (String cipher : engine.getEnabledCipherSuites())
        {
            // ignore RC4, MD5 HMAC and 3DES
            if (! (cipher.contains("RC4") || cipher.contains("MD5") || cipher.contains("3DES") || cipher.contains("KRB5")))
            {
                if (ns) System.out.print(", ");
                System.out.print(cipher);
                ns = true;
            }
        }
        // names only
        System.out.print("    public static final String[]     ENABLED_CIPHER_NAMES = getCipherNames(ENABLED_CIPHERS);");
        System.out.println();
        // locally supported runtime constants
        System.out.println("    public static final String[]     SAFE_CIPHER_NAMES = getCipherNames(SAFE_CIPHERS);");
        System.out.println();
        System.out.println("    public static final CipherInfo[] ALL_SUPPORTED_CIPHERS = getSupportedCiphers(ALL_CIPHERS);");
        System.out.println();
        System.out.println("    public static final String[]     ALL_SUPPORTED_CIPHER_NAMES = getCipherNames(ALL_SUPPORTED_CIPHERS);");
        System.out.println();
        System.out.println("    public static final CipherInfo[] ENABLED_SUPPORTED_CIPHERS = getSupportedCiphers(ENABLED_CIPHERS);");
        System.out.println();
        System.out.println("    public static final String[]     ENABLED_SUPPORTED_CIPHER_NAMES = getCipherNames(ENABLED_SUPPORTED_CIPHERS);");
        System.out.println();
        System.out.println("    public static final CipherInfo[] SAFE_SUPPORTED_CIPHERS = getSupportedCiphers(SAFE_CIPHERS);");
        System.out.println();
        System.out.println("    public static final String[]     SAFE_SUPPORTED_CIPHER_NAMES = getCipherNames(SAFE_SUPPORTED_CIPHERS);");
        System.out.println();
        System.out.println(" };");
        System.out.println();
        // finish
        System.out.println("}");
    }
    
    public static final String getAuth(String cipher)
    {
        if (! cipher.contains("WITH")) return null;
        int s = cipher.indexOf("_");
        int e = cipher.indexOf("_WITH");
        return cipher.substring(s + 1, e);
    }
}