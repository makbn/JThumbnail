package io.github.makbn.jthumbnail.core.thumbnailers;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.util.ResizeImage;

@Component
public class DWGThumbnailer extends AbstractThumbnailer {
    public DWGThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }
    @SuppressWarnings("all")
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        //GENERATE FROM EXISTING BITMAP IN DWG
        byte[] outputByte = new byte[4096];


        try (FileInputStream fis = new FileInputStream(input)) {
            long ignored = fis.skip(0x0D);
            ignored = fis.read(outputByte, 0, 4);
            int posSentinel = (((outputByte[3]) & 0xFF) * 256 * 256 * 256) + (((outputByte[2]) & 0xFF) * 256 * 256) + (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
            ignored = fis.skip(posSentinel - 0x0D - 4 + 30L);
            outputByte[1] = 0;
            ignored = fis.read(outputByte, 0, 1);
            int typePreview = ((outputByte[0]) & 0xFF);
            if (typePreview == 2) {
                ignored = fis.read(outputByte, 0, 4);
                int posBMP = (((outputByte[3]) & 0xFF) * 256 * 256 * 256) + (((outputByte[2]) & 0xFF) * 256 * 256) + (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
                ignored = fis.read(outputByte, 0, 4);
                int lenBMP = (((outputByte[3]) & 0xFF) * 256 * 256 * 256) + (((outputByte[2]) & 0xFF) * 256 * 256) + (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
                ignored = fis.skip(posBMP - (posSentinel + 30) - 1 - 4 - 4 + 14L);
                ignored = fis.read(outputByte, 0, 2);
                int biBitCount = (((outputByte[1]) & 0xFF) * 256) + ((outputByte[0]) & 0xFF);
                ignored = fis.skip(-16);
                int bisSize = 0;
                int bfSize = 0;
                if (biBitCount < 9)
                    bfSize = 54 + 4 * ((int) (Math.pow(2, biBitCount))) + lenBMP;
                else bfSize = 54 + lenBMP;
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

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.write(outputByte, 0, 14);

                while ((lenBMP > 0) && ((bisSize = fis.read(outputByte, 0, (Math.min(lenBMP, 4096)))) != -1)) {
                    bos.write(outputByte, 0, bisSize);
                    lenBMP -= bisSize;
                }

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

                BufferedImage originalImage = ImageIO.read(bis);

                ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
                resizer.setInputImage(originalImage);
                resizer.writeOutput(output);
            }
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{"image/x-dwg"};
    }

}
