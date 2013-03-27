package org.cloudcoder.app.server.persist;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.daemon.Util;

public class ListCloudCoderProperties
{
    static final String CLOUDCODER_PROPERTIES = "cloudcoder.properties";
    private static List<String> keys=Arrays.asList("gwt.sdk",
            "cloudcoder.db.user",
            "cloudcoder.db.passwd",
            "cloudcoder.db.databaseName",
            "cloudcoder.db.host",
            "cloudcoder.db.portStr",
            "cloudcoder.login.service",
            "cloudcoder.submitsvc.oop.host",
            "cloudcoder.submitsvc.oop.numThreads",
            "cloudcoder.submitsvc.oop.port",
            "cloudcoder.submitsvc.ssl.cn",
            "cloudcoder.submitsvc.ssl.keystore",
            "cloudcoder.submitsvc.ssl.keystore.password",
            "cloudcoder.webserver.port",
            "cloudcoder.webserver.contextpath",
            "cloudcoder.webserver.localhostonly");
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    throws Exception
    {
        Scanner keyboard=new Scanner(System.in);
        String builderProperties=ConfigurationUtil.ask(keyboard, "Would you like to print the properties for the builder as well?", ConfigurationUtil.YES);
        String builderJarfile=null;
        if (builderProperties.equals(ConfigurationUtil.YES)) {
            builderJarfile=ConfigurationUtil.ask(keyboard, "What is the name (or path) of the jarfile containing the code for your CloudCoder builder?", "cloudcoderBuilder.jar");
        }
        
        String filename=CLOUDCODER_PROPERTIES;
        Properties ccProps=Util.loadPropertiesFromResource(ListCloudCoderProperties.class.getClassLoader(), filename);
        // go through properties in a certain order
        
        System.out.println("\n\ncloudcoderApp.jar configuration properties ('the Webapp')");
        printCloudCoderProperties(ccProps);
        
        if (builderJarfile!=null) {
            ZipFile zipFile=new ZipFile(builderJarfile);
            ZipEntry entry=zipFile.getEntry(CLOUDCODER_PROPERTIES);
            Properties builderProps=new Properties();
            builderProps.load(zipFile.getInputStream(entry));
            System.out.println("\n\n" +builderJarfile+" configuration properties ('the Builder')");
            printCloudCoderProperties(builderProps);
        }
        
    }

    /**
     * @param ccProps
     */
    private static void printCloudCoderProperties(Properties ccProps) {
        for (String key : keys) {
            if (ccProps.containsKey(key)) {
                System.out.println(key+"="+ccProps.getProperty(key));
            }
        }
        for (Entry e : ccProps.entrySet()) {
            //OK, fine, this requires a few linear scans
            if (!keys.contains(e.getKey())) {
                System.out.println(e.getKey()+"="+e.getValue());
            }
        }
    }

}
