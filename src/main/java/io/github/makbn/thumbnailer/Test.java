package io.github.makbn.thumbnailer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mehdi Akbarian-Rastaghi on 9/30/18
 */

public class Test {

    public static void main(String[] args) {

        try {
            Thumbnailer.start();
            File in = new File("/files/pdf/test1.pdf");
            if(in.exists()) {
                File out = Thumbnailer.createThumbnail(in);
                System.out.println("FILE created at : " + out.getAbsolutePath());
            }
        } catch (FileDoesNotExistException e) {
            e.printStackTrace();
        } catch (ThumbnailerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
