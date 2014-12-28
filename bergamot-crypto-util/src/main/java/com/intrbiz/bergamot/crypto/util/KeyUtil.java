package com.intrbiz.bergamot.crypto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyUtil
{
    public static IvParameterSpec newAESIV()
    {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    
    public static SecretKey newAESKey() throws NoSuchAlgorithmException
    {
        KeyGenerator sKeyGen = KeyGenerator.getInstance("AES");
        sKeyGen.init(128);
        SecretKey sKey = sKeyGen.generateKey();
        return sKey;
    }
    
    public static SecretKey openAESKey(byte[] encoded)
    {
        return new SecretKeySpec(encoded, "AES");
    }
    
    public static IvParameterSpec openAESIV(byte[] iv)
    {
        return new IvParameterSpec(iv);
    }
    
    public static KeyPair newRSAKeyPair() throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }
    
    public static byte[] encryptSecretKey(SecretKey sKey, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE, key);
        byte[] esk = rsa.doFinal(sKey.getEncoded());
        return esk;
    }
    
    public static SecretKey decryptSecretKey(byte[] encrypted, PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher rsad = Cipher.getInstance("RSA");
        rsad.init(Cipher.DECRYPT_MODE, key);
        byte[] dsk = rsad.doFinal(encrypted);
        return openAESKey(dsk);
    }
    
    public static byte[] encryptIV(IvParameterSpec iv, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE, key);
        byte[] esk = rsa.doFinal(iv.getIV());
        return esk;
    }
    
    public static IvParameterSpec decryptIV(byte[] encrypted, PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher rsad = Cipher.getInstance("RSA");
        rsad.init(Cipher.DECRYPT_MODE, key);
        byte[] dsk = rsad.doFinal(encrypted);
        return openAESIV(dsk);
    }
    
    public static void savePublicKey(File file, PublicKey key) throws FileNotFoundException, IOException
    {
        try (FileOutputStream out = new FileOutputStream(file))
        {
            out.write(key.getEncoded());
        }
    }
    
    public static PublicKey loadPublicKey(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] data = loadFile(file);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(data));
    }
    
    public static void savePrivateKey(File file, KeyPair key) throws FileNotFoundException, IOException
    {
        // the data
        byte[] pub = key.getPublic().getEncoded();
        byte[] prv = key.getPrivate().getEncoded();
        // pack the data
        ByteBuffer buf = ByteBuffer.wrap(new byte[8 + pub.length + prv.length]);
        buf.putInt(pub.length);
        buf.put(pub);
        buf.putInt(prv.length);
        buf.put(prv);
        // write
        try (FileOutputStream out = new FileOutputStream(file))
        {
            out.write(buf.array());
        }
    }
    
    public static KeyPair loadPrivateKey(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] data = loadFile(file);
        // unpack
        ByteBuffer buf = ByteBuffer.wrap(data);
        int pubLen = buf.getInt();
        if (pubLen < 16 || pubLen > 8192) throw new IOException("Bad key");
        byte[] pub = new byte[pubLen];
        buf.get(pub);
        int prvLen = buf.getInt();
        if (prvLen < 16 || prvLen > 8192) throw new IOException("Bad key");
        byte[] prv = new byte[prvLen];
        buf.get(prv);
        // load
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey prvKey = kf.generatePrivate(new PKCS8EncodedKeySpec(prv));
        PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(pub));
        return new KeyPair(pubKey, prvKey);
    }
    
    private static byte[] loadFile(File file) throws FileNotFoundException, IOException
    {
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file))
        {
            in.read(data);
        }
        return data;
    }
}
