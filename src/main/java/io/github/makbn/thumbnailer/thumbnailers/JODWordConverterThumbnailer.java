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

/**
 * Dummy class for converting Text documents into Openoffice-Textfiles.
 * <p>
 * Tika could be used to detect ms-word-files, but quite a heavy library. Maybe it would be useful as a preperator as well?
 *
 * @see JODConverterThumbnailer
 */
public class JODWordConverterThumbnailer extends JODConverterThumbnailer {

    protected String getStandardOpenOfficeExtension() {
        return ".odt";
    }

    protected String getStandardZipExtension() {
        return "docx";
    }

    protected String getStandardOfficeExtension() {
        return "doc";
    }

    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "text/plain",
                "text/rtf",
/*				"application/msword", */
                "application/vnd.ms-word",
                "application/vnd.openxmlformats-officedocument.wordprocessingml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/wordperfect",
                /*		"application/vnd.ms-office", // doc?
                        "application/zip" // docx? */
        };
    }

}
