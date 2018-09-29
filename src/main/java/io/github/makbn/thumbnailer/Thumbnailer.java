/*
 * regain/Thumbnailer - A file search engine providing plenty of formats (Plugin)
 * Copyright (C) 2011  Come_IN Computerclubs (University of Siegen)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
 */

package io.github.makbn.thumbnailer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.makbn.thumbnailer.thumbnailers.DWGThumbnailer;

import io.github.makbn.thumbnailer.thumbnailers.JODExcelConverterThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.JODHtmlConverterThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.JODPowerpointConverterThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.JODWordConverterThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.NativeImageThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.OpenOfficeThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.PDFBoxThumbnailer;
import jdk.nashorn.internal.runtime.logging.DebugLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Created by Mehdi Akbarian-Rastaghi on 9/25/18
 */
public class Thumbnailer {

    private static ThumbnailerManager thumbnailer;
    private static final String LOG4J_CONFIG_FILE = "conf/javathumbnailer.log4j.properties";
    private static int width = 220;
    private static int height = 150;
    private static ClassLoader loader = Thumbnailer.class.getClassLoader();
    protected static Logger mLog = Logger.getLogger(Thumbnailer.class);


    public static void main(String[] args) throws IOException, ThumbnailerException {
        initLogging();
        start();

        File dir = new File("/home/fanap/Desktop/docs");

        for(File f:dir.listFiles()){
            File out = createThumbnail(f);
            System.out.println("FILE created at : "+out.getAbsolutePath());
        }
/*
        File out = createThumbnail(new File("/home/fanap/Desktop/docs/aa.docx"));
        System.out.println("FILE created at : "+out.getAbsolutePath());*/

    }


    protected static void initLogging() throws IOException
    {
        System.setProperty("log4j.configuration", LOG4J_CONFIG_FILE);

        File logConfigFile = new File(loader.getResource(LOG4J_CONFIG_FILE).getFile());
        if (!logConfigFile.exists())
        {
            // Extract config properties from jar
            InputStream in = Thumbnailer.class.getResourceAsStream("/" + LOG4J_CONFIG_FILE);
            if (in == null)
            {
                System.err.println("Packaging error: can't find logging configuration inside jar. (Neither can I find the config file on the file system: " + logConfigFile.getAbsolutePath() + ")");
                System.exit(1);
            }

            OutputStream out = null;
            try {
                out = FileUtils.openOutputStream(logConfigFile);
                IOUtils.copy(in, out);
            } finally { try { if (in != null) in.close(); } finally { if (out != null) out.close(); } }
        }

        PropertyConfigurator.configureAndWatch(logConfigFile.getAbsolutePath(), 10 * 1000);
        mLog.info("Logging initialized");
    }

    public static void start() throws FileDoesNotExistException {

        thumbnailer = new ThumbnailerManager();
        loadExistingThumbnailers();

        setParameters();
    }


    public static void createThumbnail(File inputFile,File outputFile) throws IOException, ThumbnailerException {
        if(thumbnailer!=null){
            thumbnailer.generateThumbnail(inputFile,outputFile);
        }else {
            start();
            thumbnailer.generateThumbnail(inputFile,outputFile);
        }
    }

    public static  File createThumbnail(File inputFile) throws IOException, ThumbnailerException {
        if(thumbnailer!=null){
            return thumbnailer.createThumbnail(inputFile);
        }else {
            start();
            return thumbnailer.createThumbnail(inputFile);
        }
    }

    private static void setParameters() throws FileDoesNotExistException {
        thumbnailer.setThumbnailFolder("thumbs/");
        thumbnailer.setImageSize(width, height, 0);
    }




    protected static void loadExistingThumbnailers() {
        if (classExists("io.github.makbn.thumbnailer.thumbnailers.NativeImageThumbnailer"))
            thumbnailer.registerThumbnailer(new NativeImageThumbnailer());

        thumbnailer.registerThumbnailer(new OpenOfficeThumbnailer());
        thumbnailer.registerThumbnailer(new PDFBoxThumbnailer());

        try {
            thumbnailer.registerThumbnailer(new JODWordConverterThumbnailer());
            thumbnailer.registerThumbnailer(new JODExcelConverterThumbnailer());
            thumbnailer.registerThumbnailer(new JODPowerpointConverterThumbnailer());
            thumbnailer.registerThumbnailer(new JODHtmlConverterThumbnailer());
        } catch (IOException e) {

        }


        thumbnailer.registerThumbnailer(new DWGThumbnailer());
    }

    public static boolean classExists(String qualifiedClassname) {
        try {
            Class.forName(qualifiedClassname);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
