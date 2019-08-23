package com.Network.thermal_image_processing_pipeline;

import android.os.Environment;

import com.example.thermal_image_processing_pipeline.MainActivity;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

import static com.example.thermal_image_processing_pipeline.MainActivity.pipeline;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class SSHConnection {

    private static final String CAMERA_DIRECTORY = "/var/volatile/cache/recorder/";

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
        String commands = "cd " + CAMERA_DIRECTORY + " && ";                    // Move to default directory.
        commands += "rm " + CAMERA_DIRECTORY + "*.pgm" + " ; ";                 // Make sure there are no old PGM files.
        commands += "catch_raw_image -k " + MainActivity.NBR_SHUTTER_IMAGES;    // Takes a number of raw 12-bit (k argument) images with the shutter closed.

        channelssh.setCommand(commands);
        channelssh.connect();
        channelssh.disconnect();

        Channel channel = session.openChannel("sftp");              // Prepare to fetch the shutter images by sftp.
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.cd(CAMERA_DIRECTORY);

        File sdcard = Environment.getExternalStorageDirectory();

        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*.pgm");

        while(list.size() < MainActivity.NBR_SHUTTER_IMAGES) {      // Ensure that all the shutter images are ready.
            Thread.sleep(500);
            list = sftpChannel.ls("*.pgm");
        }

        int pos = 1;
        for(ChannelSftp.LsEntry entry : list) {                     // Get the shutter images.
            sftpChannel.get(CAMERA_DIRECTORY + entry.getFilename(), sdcard + "/Download/shutter" + pos + ".pgm");
            pos++;
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

        for (int i = 1; i <= MainActivity.NBR_SHUTTER_IMAGES; i++){
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
