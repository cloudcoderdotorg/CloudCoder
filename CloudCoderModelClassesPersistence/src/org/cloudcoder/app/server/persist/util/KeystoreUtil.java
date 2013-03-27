package org.cloudcoder.app.server.persist.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Date;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

public class KeystoreUtil
{
    public static void createKeyStore(String commonName,
        String storePassword,
        String keystoreFile)
    throws Exception
    {
        KeyStore ks=createKeyStore(3600, commonName, storePassword);
        FileOutputStream fos=new FileOutputStream(new File(keystoreFile));
        ks.store(fos, storePassword.toCharArray());
        fos.flush();
        fos.close();
    }
    
    public static KeyStore createKeyStore(int validity,
        String commonName,
        String keyPassword) 
    throws Exception
    {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        String alias="cloudcoder";
        String dname="CN="+commonName+", OU=None, L=None, ST=None, C=None";
        String keyAlgName="RSA";
        String sigAlgName="SHA1WithRSA";
        int keySize=2048;
        doGenKeyPair(ks, 
                validity, 
                alias, 
                dname, 
                keyAlgName, 
                keySize, 
                sigAlgName, 
                keyPassword);
        return ks;
    }
    
    public static void main(String[] args) throws Exception
    {
        String alias="cloudcoder";
        String storePassword="";
        String keystoreFile="keystore.jks";
        int validity=3600;
        String keyPassword="";
        String commonName="Knox College";
        String dname="CN="+commonName+", OU=None, L=None, ST=None, C=None";

        KeyStore ks = KeyStore.getInstance("JKS");

        ks.load(null, null);

        doGenKeyPair(ks, validity, alias, dname, "RSA", 2048, "SHA1WithRSA", keyPassword);

        FileOutputStream fos=new FileOutputStream(new File(keystoreFile));
        ks.store(fos, "".toCharArray());
        fos.flush();
        fos.close();

        if (true) {
            return;
        }

        CertificateFactory cf = CertificateFactory.getInstance( "X.509" );  
        //NOTE: THIS IS java.security.cert.Certificate NOT java.security.Certificate  
        Certificate cert = null;

        //GET THE FILE CONTAINING YOUR CERTIFICATE  
        FileInputStream fis = new FileInputStream( "MyCert.cer" );  
        BufferedInputStream bis = new BufferedInputStream(fis);  
        //I USE x.509 BECAUSE THAT'S WHAT keytool CREATES  
        /* 
         * LOAD THE STORE 
         * The first time you're doing this (i.e. the keystore does not 
         * yet exist - you're creating it), you HAVE to load the keystore 
         * from a null source with null password. Before any methods can 
         * be called on your keystore you HAVE to load it first. Loading 
         * it from a null source and null password simply creates an empty 
         * keystore. At a later time, when you want to verify the keystore 
         * or get certificates (or whatever) you can load it from the 
         * file with your password. 
         */  
        /* 
         * I ONLY HAVE ONE CERT, I JUST USED "while" BECAUSE I'M JUST 
         * DOING TESTING AND WAS TAKING WHATEVER CODE I FOUND IN 
         * THE API DOCUMENTATION. I COULD HAVE DONE AN "if", BUT I 
         * WANTED TO SHOW HOW YOU WOULD HANDLE IT IF YOU GOT A CERT 
         * FROM VERISIGN THAT CONTAINED MULTIPLE CERTS 
         */  
        //GET THE CERTS CONTAINED IN THIS ROOT CERT FILE  
        while ( bis.available() > 0 )  
        {  
            cert = cf.generateCertificate( bis );  
            ks.setCertificateEntry( "SGCert", cert );  
        }  
        //ADD TO THE KEYSTORE AND GIVE IT AN ALIAS NAME  
        ks.setCertificateEntry( "SGCert", cert );  
        //SAVE THE KEYSTORE TO A FILE  
        /* 
         * After this is saved, I believe you can just do setCertificateEntry 
         * to add entries and then not call store. I believe it will update 
         * the existing store you load it from and not just in memory. 
         */  
        ks.store( new FileOutputStream( "NewClientKeyStore" ), "MyPass".toCharArray() );
    }
    
    private static final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("sun.security.util.Resources");

    /**
     * This code is based on code from OpenJDK-6, available here:
     * 
     * http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/sun/security/tools/KeyTool.java
     * 
     * @param keyStore
     * @param validity
     * @param alias
     * @param dname
     * @param keyAlgName
     * @param keysize
     * @param sigAlgName
     * @param keyPass
     * @param storePass
     * @throws Exception
     */
    static void doGenKeyPair(KeyStore keyStore, 
        int validity, 
        String alias, 
        String dname, 
        String keyAlgName, 
        int keysize, 
        String sigAlgName,
        String keyPass)
   throws KeyStoreException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, CertificateException
   { 
        if (keysize == -1) {
            if ("EC".equalsIgnoreCase(keyAlgName)) {
                keysize = 256;
            } else {
                keysize = 1024;
            }
        }
        if (keyStore.containsAlias(alias)) {
            MessageFormat form = new MessageFormat(rb.getString("Key pair not generated, alias <alias> already exists"));
            Object[] source = {alias};
            throw new IllegalArgumentException(form.format(source));
        }
        if (sigAlgName == null) {
            if ("DSA".equalsIgnoreCase(keyAlgName)) {
                sigAlgName = "SHA1WithDSA";
            } else if ("RSA".equalsIgnoreCase(keyAlgName)) {
                sigAlgName = "SHA1WithRSA";
            } else if ("EC".equalsIgnoreCase(keyAlgName)) {
                sigAlgName = "SHA1withECDSA";
            } else {
                throw new IllegalArgumentException(rb.getString("Cannot derive signature algorithm"));
            }
        }
        //CertAndKeyGen keypair = new CertAndKeyGen(keyAlgName, sigAlgName, providerName);
        CertAndKeyGen keypair = new CertAndKeyGen(keyAlgName, sigAlgName);
        // If DN is provided, parse it. Otherwise, prompt the user for it.
        X500Name x500Name= new X500Name(dname);
        keypair.generate(keysize);
        PrivateKey privKey = keypair.getPrivateKey();
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), validity*24L*60L*60L);
        if (true) {
            MessageFormat form = new MessageFormat(rb.getString
                    ("Generating keysize bit keyAlgName key pair and self-signed certificate " +
                            "(sigAlgName) with a validity of validality days\n\tfor: x500Name"));
            Object[] source = {new Integer(keysize),
                    privKey.getAlgorithm(),
                    chain[0].getSigAlgName(),
                    new Long(validity),
                    x500Name};
            System.err.println(form.format(source));
        }
        keyStore.setKeyEntry(alias, privKey, keyPass.toCharArray(), chain);
    }
}
