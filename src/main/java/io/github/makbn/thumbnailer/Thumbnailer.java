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

import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import io.github.makbn.thumbnailer.thumbnailers.*;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Mehdi Akbarian-Rastaghi on 9/25/18
 */
public class Thumbnailer {

    private static final String defaultOutputDir = AppSettings.TEMP_DIR;

    private static ThumbnailerManager thumbnailer;
    private static final MimeTypeDetector typeDetector = new MimeTypeDetector();

    protected static Logger mLog =  LogManager.getLogger(Thumbnailer.class.getName());

    private static ThumbnailState state;

    private enum ThumbnailState {ideal, running}
    private static ConcurrentHashMap<ThumbnailCandidate,ThumbnailListener> files;


    public static void start() throws FileDoesNotExistException, ThumbnailerException {
        if(!AppSettings.isInit())
            throw new ThumbnailerException("call AppSettings.init(args) first!");
        if(files == null)
            files = new ConcurrentHashMap<>();

        if(thumbnailer!= null)
            return;
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

    public static  File createThumbnail(File inputFile,String ext) throws IOException, ThumbnailerException {
        if(thumbnailer!=null){
            return thumbnailer.createThumbnail(inputFile, ext);
        }else {
            start();
            return thumbnailer.createThumbnail(inputFile, ext);
        }
    }


    public static void  createThumbnail(ThumbnailCandidate candidate, ThumbnailListener listener){
        synchronized (files){

            files.put(candidate,listener);
            mLog.warn("file added to queue!");
            if(state == ThumbnailState.ideal) {
                runTasks();
            }else {
                mLog.warn("task is running!");
            }
        }

    }

    private static void runTasks() {
        synchronized (state) {
            if(state == ThumbnailState.running) {
                return;
            }
            mLog.warn("task started!");
            state = ThumbnailState.running;
            Runnable taskRunner = () -> {
                while (!files.isEmpty()) {
                    files.entrySet().removeIf(e -> {
                        try {
                            e.getKey().setThumbExt(typeDetector.getOutputExt(e.getKey().getFile()));
                            File out = createThumbnail(e.getKey().getFile(), e.getKey().getThumbExt());
                            e.getValue().onThumbnailReady(out.getName(), out);
                            return true;
                        } catch (IOException | ThumbnailerException exp) {
                            mLog.warn(exp);
                            e.getValue().onThumbnailFailed(e.getKey().getHash(),exp.getMessage(),100);
                            return true;
                        }
                    });
                }
                mLog.warn("task ended!");
                state = ThumbnailState.ideal;
            };
            Thread taskThread = new Thread(taskRunner);
            taskThread.setName("thumbnail-task-thread-"+taskThread.getId());
            taskThread.start();
        }
    }


    private static void setParameters() throws FileDoesNotExistException {
        thumbnailer.setThumbnailFolder(defaultOutputDir);
        thumbnailer.setImageSize(AppSettings.THUMB_WIDTH, AppSettings.THUMB_WIDTH, 0);
        state = ThumbnailState.ideal;
    }


    protected static void loadExistingThumbnailers() {

        thumbnailer.registerThumbnailer(new NativeImageThumbnailer());
        thumbnailer.registerThumbnailer(new OpenOfficeThumbnailer());
        thumbnailer.registerThumbnailer(new PDFBoxThumbnailer());
        thumbnailer.registerThumbnailer(new JODWordConverterThumbnailer());
        thumbnailer.registerThumbnailer(new JODExcelConverterThumbnailer());
        thumbnailer.registerThumbnailer(new JODPowerpointConverterThumbnailer());
        thumbnailer.registerThumbnailer(new JODHtmlConverterThumbnailer());
        thumbnailer.registerThumbnailer(new MPEGThumbnailer());
        thumbnailer.registerThumbnailer(new MP3Thumbnailer());
        thumbnailer.registerThumbnailer(new DWGThumbnailer());
        thumbnailer.registerThumbnailer(new ImageThumbnailer());

        mLog.warn("Thumbnailers loaded!");
    }
}