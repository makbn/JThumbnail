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

package io.github.makbn.thumbnailer.thumbnailers;

import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;

import java.io.File;
import java.io.IOException;

/**
 * Dummy class for converting Spreadsheet documents into Openoffice-Textfiles.
 *
 * @see JODConverterThumbnailer
 */
public class ExcelConverterThumbnailer extends AbstractThumbnailer {
    private final OpenOfficeThumbnailer ooo_thumbnailer;

    public ExcelConverterThumbnailer() {
        ooo_thumbnailer = new OpenOfficeThumbnailer();
    }

    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {

        //Create a workbook instance
        try {
            Workbook workbook = new Workbook();
            //Load a sample Excel document
            workbook.loadFromFile(input.getAbsolutePath());
            //Fit all worksheets on one page (optional)
            workbook.getConverterSetting().setSheetFitToPage(true);

            File outputTmp = File.createTempFile("jthumbnailer", "." + "pdf");

            //Save the workbook to PDF
            workbook.saveToFile(outputTmp.getAbsolutePath(), FileFormat.PDF);
            ooo_thumbnailer.generateThumbnail(outputTmp, output);
            outputTmp.deleteOnExit();
        } catch (Exception err) {
            throw new ThumbnailerException(err);
        }

    }

    protected String getStandardOpenOfficeExtension() {
        return "ods";
    }

    protected String getStandardZipExtension() {
        return "xlsx";
    }

    protected String getStandardOfficeExtension() {
        return "xls";
    }

    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                /*		"application/vnd.ms-office", // xls?
                        "application/zip" // xlsx? */
        };
    }

    public void setImageSize(int thumbWidth, int thumbHeight, int imageResizeOptions) {
        super.setImageSize(thumbWidth, thumbHeight, imageResizeOptions);
        ooo_thumbnailer.setImageSize(thumbWidth, thumbHeight, imageResizeOptions);
    }


}
