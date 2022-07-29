package io.github.makbn.thumbnailer.thumbnailers;


import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.util.IOUtil;
import io.github.makbn.thumbnailer.util.ResizeImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class DWGThumbnailer extends AbstractThumbnailer {

    private static final Logger mLog = LogManager.getLogger("DWGThumbnailer");

    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        //GENERATE FROM EXISTING BITMAP IN DWG
        byte[] outputByte = new byte[4096];

        FileInputStream fis = null;
        long ignored = 0;
        try {
            fis = new FileInputStream(input);
            ignored = fis.skip(0x0D);
            ignored = fis.read(outputByte, 0, 4);
            int PosSentinel = (((outputByte[3]) & 0xFF) * 256 * 256 * 256) + (((outputByte[2]) & 0xFF) * 256 * 256) + (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
            ignored = fis.skip(PosSentinel - 0x0D - 4 + 30);
            outputByte[1] = 0;
            ignored = fis.read(outputByte, 0, 1);
            int TypePreview = ((outputByte[0]) & 0xFF);
            if (TypePreview == 2) {
                ignored = fis.read(outputByte, 0, 4);
                int PosBMP = (((outputByte[3]) & 0xFF) * 256 * 256 * 256) + (((outputByte[2]) & 0xFF) * 256 * 256) + (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
                ignored = fis.read(outputByte, 0, 4);
                int LenBMP = (((outputByte[3]) & 0xFF) * 256 * 256 * 256) + (((outputByte[2]) & 0xFF) * 256 * 256) + (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
                ignored = fis.skip(PosBMP - (PosSentinel + 30) - 1 - 4 - 4 + 14);
                ignored = fis.read(outputByte, 0, 2);
                int biBitCount = (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
                fis.skip(-16);
                int bisSize = 0;
                int bfSize = 0;
                if (biBitCount < 9)
                    bfSize = 54 + 4 * ((int) (Math.pow(2, biBitCount))) + LenBMP;
                else bfSize = 54 + LenBMP;
                //WORD "BM"
                outputByte[0] = 0x42;
                outputByte[1] = 0x4D;
                //DWORD bfSize
                outputByte[2] = (byte) (bfSize & 0xff);
                outputByte[3] = (byte) (bfSize >> 8 & 0xff);
                outputByte[4] = (byte) (bfSize >> 16 & 0xff);
                outputByte[5] = (byte) (bfSize >>> 24);
                //WORD bfReserved1
                outputByte[6] = 0x00;
                outputByte[7] = 0x00;
                //WORD bfReserved2
                outputByte[8] = 0x00;
                outputByte[9] = 0x00;
                //DWORD bfOffBits
                outputByte[10] = 0x36;
                outputByte[11] = 0x04;
                outputByte[12] = 0x00;
                outputByte[13] = 0x00;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(outputByte, 0, 14);
                while ((LenBMP > 0) && ((bisSize = fis.read(outputByte, 0, (Math.min(LenBMP, 4096)))) != -1)) {
                    baos.write(outputByte, 0, bisSize);
                    LenBMP -= bisSize;
                }

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                BufferedImage originalImage = ImageIO.read(bais);

                ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
                resizer.setInputImage(originalImage);
                resizer.writeOutput(output);
            }
        } catch (ThumbnailerException e) {
            throw new ThumbnailerException(e);
        } finally {
            IOUtil.quietlyClose(fis);
        }
    }

    public String[] getAcceptedMIMETypes() {
        return new String[]{"image/x-dwg"};
    }

}
