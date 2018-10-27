package io.github.makbn.thumbnailer;

import org.apache.commons.cli.*;

import java.util.ResourceBundle;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-21
 */
public class AppSettings {
    private static ResourceBundle rb = ResourceBundle.getBundle("application");

    public static  int DRIVE_OPENOFFICE_PORT;
    public static String DRIVE_OPENOFFICE_SERVER_PATH;

    public static String AEROSPIKE_IP;
    public static int AEROSPIKE_PORT;
    public static String AEROSPIKE_USERNAME;
    public static String AEROSPIKE_PASSWORD;
    public static String AEROSPIKE_NAMESPACE;
    public static String AEROSPIKE_SET;
    public static String TEMP_DIR;
    public static String DRIVE_DIR;

    public static int THUMB_WIDTH;
    public static int THUMB_HEIGHT;



    public static void init(String[] args){
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        try {
            CommandLine cmd = parser.parse(options, args);

            DRIVE_OPENOFFICE_PORT = Integer.parseInt(getValue(cmd, "openoffice_port"));
            DRIVE_OPENOFFICE_SERVER_PATH = getValue(cmd,"openoffice_dir");
            AEROSPIKE_USERNAME = getValue(cmd, "aerospike_username");
            AEROSPIKE_PASSWORD = getValue(cmd, "aerospike_password");
            AEROSPIKE_IP = getValue(cmd, "aerospike_ip");
            AEROSPIKE_PORT = Integer.parseInt(getValue(cmd, "aerospike_port"));
            AEROSPIKE_NAMESPACE = getValue(cmd, "aerospike_namespace");
            AEROSPIKE_SET = getValue(cmd, "aerospike_set");
            TEMP_DIR = getValue(cmd, "temp_dir");
            DRIVE_DIR = getValue(cmd, "drive_dir");
            THUMB_HEIGHT = Integer.parseInt(getValue(cmd, "thumb_height"));
            THUMB_WIDTH = Integer.parseInt(getValue(cmd, "thumb_width"));



        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption("aerospike_username",true,"username of aerospike.");
        options.addOption("aerospike_password",true,"password of aerospike.");
        options.addOption("aerospike_ip",true,"address of aerospike.");
        options.addOption("aerospike_port",true,"port of aerospike.");
        options.addOption("aerospike_namespace",true,"namespace of aerospike.");
        options.addOption("temp_dir",true,"temp directory for saving thumb files.");
        options.addOption("drive_dir",true,"drive directory to save thumb files.");
        options.addOption("thumb_height",true," thumbnail height size in px.");
        options.addOption("thumb_width", true, "thumbnail width size in px.");
        return options;
    }

    private static String getValue(CommandLine cmd, String key){
        if(cmd.hasOption(key)){
            return cmd.getOptionValue(key);
        }else {
            return rb.getString(key);
        }
    }
}
