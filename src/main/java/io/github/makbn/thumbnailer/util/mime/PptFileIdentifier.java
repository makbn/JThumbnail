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
 *
 * Concept from Nuxeo:
 * nuxeo-platform-mimetype-core/src/main/java/org/nuxeo/ecm/platform/mimetype/detectors/PptMimetypeSniffer.java (v5.5)
 * Licenced LGPL 2.1
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 */

package io.github.makbn.thumbnailer.util.mime;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;

import java.io.File;
import java.io.FileInputStream;


public class PptFileIdentifier extends OfficeFileIdentifier {
    public PptFileIdentifier() {
        super();
        ext.add("ppt");
        ext.add("pps");
    }

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {

        if (isOfficeFile(mimeType) && !PPT_MIME_TYPE.equals(mimeType)) {
            try {
                FileInputStream stream = new FileInputStream(file);
                HSLFSlideShow presentation = new HSLFSlideShow(stream);

                if (presentation.getSlides().size() != 0) {
                    return PPT_MIME_TYPE;
                }
            } catch (Throwable e) {
                //ignored
            }
        }

        return mimeType;
    }

    @Override
    public String getThumbnailExtension() {
        return "png";
    }

}
