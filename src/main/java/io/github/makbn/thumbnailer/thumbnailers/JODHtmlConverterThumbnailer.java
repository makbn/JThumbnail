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

import io.github.makbn.thumbnailer.config.AppSettings;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dummy class for converting Html documents into Openoffice-Textfiles.
 * <p>
 * (The preview won't be better than what OpenOffice can achieve. See also
 * issue <a href="https://github.com/benjamin4ruby/java-thumbnailer/issues/8">...</a>)
 *
 * @see JODConverterThumbnailer
 */
@Component
public class JODHtmlConverterThumbnailer extends JODConverterThumbnailer {

    @Autowired
    public JODHtmlConverterThumbnailer(AppSettings settings, OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager manager) {
        super(settings, openOfficeThumbnailer, manager);
    }

    protected String getStandardOpenOfficeExtension() {
        return "odt";
    }

    protected String getStandardZipExtension() {
        return "html";
    }

    protected String getStandardOfficeExtension() {
        return "html";
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "text/html"
        };
    }

}
