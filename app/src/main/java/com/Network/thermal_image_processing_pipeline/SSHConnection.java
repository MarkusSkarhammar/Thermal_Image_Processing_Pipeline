package com.Network.thermal_image_processing_pipeline;

import android.os.Environment;

import com.example.thermal_image_processing_pipeline.MainActivity;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.Vector;

import static com.example.thermal_image_processing_pipeline.MainActivity.pipeline;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class SSHConnection {

    private static final String defaultDirectory = "/var/volatile/cache/recorder/";

    public static void getShutter(String username, String password, String hostname) {

        ChannelSftp sftpChannel = null;
        Channel channel = null;
        Session session = null;
        ChannelExec channelssh = null;

        int pos = 1;

        deleteOldShutterImages();

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, hostname, 22);
            session.setPassword(password);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();

            // SSH Channel
            channelssh = (ChannelExec) session.openChannel("exec");
            channelssh.setInputStream(null);

            // Execute command

            channelssh.setCommand("killall rawtool ; cd " + defaultDirectory + " ; catch_raw_image -k 8 ; rawtool -s -p 1234 &");
            channelssh.connect();
            channelssh.disconnect();

            channel = session.openChannel("sftp");
            channel.connect();

            sftpChannel = (ChannelSftp) channel;
            sftpChannel.cd(defaultDirectory);

            File sdcard = Environment.getExternalStorageDirectory();

            Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*.pgm");

            while(list.size() < 7) {
                Thread.sleep(500);
                list = sftpChannel.ls("*.pgm");
            }

            for(ChannelSftp.LsEntry entry : list) {
                sftpChannel.get(defaultDirectory + entry.getFilename(), sdcard + "/Download/shutter" + (pos++) + ".pgm");
            }

            while(list.size() > 0) {
                Thread.sleep(500);
                list = sftpChannel.ls("*.pgm");
                sftpChannel.rm("*.pgm");
            }

            sftpChannel.disconnect();
            channel.disconnect();
            session.disconnect();


            pipeline.setupShutterValueFromStorage(MainActivity.activity);

            if(TCPClient.ERROR_SSH_SHUTTER_TRANSFER)
                TCPClient.ERROR_SSH_SHUTTER_TRANSFER = false;

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            System.out.println("Position is: " + pos + ". " + e.toString());
            TCPClient.ERROR_SSH_SHUTTER_TRANSFER = true;

            if(sftpChannel.isConnected()){
                sftpChannel.disconnect();
            }

            channelssh.setCommand("rm " + defaultDirectory + "*.pgm");
            try{
                channelssh.connect();
                channelssh.disconnect();
            }catch (JSchException f) {
                f.printStackTrace();
            }

            if(channel.isConnected()){
                channel.disconnect();
            }
            if(session.isConnected())
                session.disconnect();
        }
    }

    public static void getGain(String username, String password, String hostname) throws Exception {

        deleteOldGain();

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, 22);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        File sdcard = Environment.getExternalStorageDirectory();

        sftpChannel.get("/lib/persistent/usr/share/sensor_analysis/supplied.lgc", sdcard + "/Download/supplied.lgc");
        sftpChannel.disconnect();

        session.disconnect();

        pipeline.getGain(MainActivity.activity, str_w, str_h);
    }

    public static void startCamera(String username, String password, String hostname) throws Exception{
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, 22);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        channelssh.setInputStream(null);

        channelssh.setCommand("killall rawtool ; irfpgactrl --data_sel=6 --noise_flt_on=0 --dpc_on=0 ; /usr/bin/rawtool -s -p 1234 & ; rm " + defaultDirectory + "*.pgm");
        channelssh.connect();
        channelssh.disconnect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = null;
        sftpChannel = (ChannelSftp) channel;
        sftpChannel.cd(defaultDirectory);

        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*.pgm");

        while(list.size() > 0) {
            Thread.sleep(500);
            list = sftpChannel.ls("*.pgm");
            sftpChannel.rm("*.pgm");
        }

        sftpChannel.disconnect();
        channel.disconnect();
        session.disconnect();

    }

    private static void deleteOldShutterImages() {

        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getPath() + "/Download/" );
        File[] listOfFiles = folder.listFiles();

        for(File f : listOfFiles){
            if(f.getName().contains("shutter")){
                f.delete();
            }
        }
    }

    private static void deleteOldGain() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file;

        file = new File(sdcard, "/Download/supplied.lgc");
        if (file.exists()) {
            file.delete();
        }
    }
}
