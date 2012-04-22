package edu.cc.oba;

import java.io.File;
import java.util.Properties;
import java.util.Vector;
 
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
 
public class ConnectWithPass {
 
    public static void conn_do(String[] args) throws Exception {
         
        if (args.length < 3) {
            throw new Exception("not enough arguments");
        }
         
        String serverUrl = args[0];
        String userName = args[1];
        String password = args[2];
         
        JSch jsch = new JSch();
         
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("compression.s2c", "zlib,none");
        config.put("compression.c2s", "zlib,none");
         
        Session session = jsch.getSession(userName, serverUrl);
        session.setConfig(config);
        session.setPort(22);
        session.setPassword(password);
        session.connect();
         
       
        session.disconnect();
    }
 
}