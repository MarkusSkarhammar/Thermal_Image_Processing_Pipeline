package com.Network.thermal_image_processing_pipeline;

import android.os.Environment;

import com.example.thermal_image_processing_pipeline.MainActivity;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Vector;

import static com.example.thermal_image_processing_pipeline.MainActivity.pipeline;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class SSHConnection {

    private static final String defaultDirectory = "/root/";

    public static void getShutter(String username, String password, String hostname) throws Exception {

        deleteOldShutterImages();

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

        // Execute command

        /*
        channelssh.setCommand("cd " + defaultDirectory);        // Move to default directory.
        channelssh.connect();
        channelssh.disconnect();

        Thread.sleep(1000);

        */

        channelssh.setCommand("rm " + defaultDirectory + "*.pgm");
        channelssh.connect();
        channelssh.disconnect();

        Thread.sleep(1000);

        channelssh.setCommand("catch_raw_image -k 12");
        channelssh.connect();
        channelssh.disconnect();

        /*
        String commands = "cd " + defaultDirectory + "; rm " + defaultDirectory + "*.pgm;" + "catch_raw_image -s 12";
        channelssh.setCommand(commands);
        channelssh.disconnect();*/

        Thread.sleep(8000);

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        File sdcard = Environment.getExternalStorageDirectory();

        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*.pgm");

        while(list.size() < 11) {
            Thread.sleep(500);
            list = sftpChannel.ls("*.pgm");
        }

        int pos = 1;
        for(ChannelSftp.LsEntry entry : list) {
            sftpChannel.get(defaultDirectory + entry.getFilename(), sdcard + "/Download/shutter" + (pos++) + ".pgm");
        }

        sftpChannel.exit();
        channel.disconnect();
        session.disconnect();

        pipeline.setupShutterValueFromStorage(MainActivity.activity);
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

    private static void deleteOldShutterImages() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file;

        for (int i = 1; i <= 12; i++){
            file = new File(sdcard, "/Download/shutter" + i + ".pgm");
            if (file.exists()) {
                file.delete();
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
