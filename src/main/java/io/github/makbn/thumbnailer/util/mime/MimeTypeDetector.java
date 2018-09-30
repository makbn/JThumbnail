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

package io.github.makbn.thumbnailer.util.mime;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Wrapper class for MIME Identification of Files.
 * <p>
 * Depends:
 * <li>Aperture (for MIME-Detection)
 */
public class MimeTypeDetector {


    private List<MimeTypeIdentifier> extraIdentifiers;

    private static Logger mLog = Logger.getLogger(MimeTypeDetector.class);
    private static Tika tika;

    /**
     * Create a MimeType Detector and init it.
     */
    public MimeTypeDetector() {
        extraIdentifiers = new ArrayList<MimeTypeIdentifier>();
        tika = new Tika();
        addMimeTypeIdentifier(new Office2007FileIdentifier());
        addMimeTypeIdentifier(new PptFileIdentifier());
        addMimeTypeIdentifier(new XlsFileIdentifier());
        addMimeTypeIdentifier(new DocFileIdentifier());

    }

    /**
     * Add a new MimeTypeIdentifier to this Detector.
     * MimeTypeIdentifier may override the decision of the detector.
     * The order the identifiers are added will also be the order they will be executed
     * (i.e., the last identifiers may override all others.)
     *
     * @param identifier a new MimeTypeIdentifier
     */
    public void addMimeTypeIdentifier(MimeTypeIdentifier identifier) {
        extraIdentifiers.add(identifier);
    }

    /**
     * Detect MIME-Type for this file.
     *
     * @param file File to analyse
     * @return String of MIME-Type, or null if no detection was possible (or unknown MIME Type)
     */
    public String getMimeType(File file) throws IOException {

        String mimeType =tika.detect(file);

		/* I don't see any effect of this
		if (mimeType != null && mimeType.equalsIgnoreCase("application/zip")) {
			mLog.info("Is a zip-file. Try second round-detection ...");
			// some new files like MS Office documents are zip files
			// so rewrite the URL for the correct mimetype detection
			mimeType = mimeTypeIdentifier.identify(bytes, null, new URIImpl("zip:mime:" + file_url));
		}
		*/

        if (mimeType != null && mimeType.length() == 0)
            mimeType = null;

        // Identifiers may re-write MIME.
        for (MimeTypeIdentifier identifier : extraIdentifiers)
            mimeType = identifier.identify(mimeType, null, file);

        mLog.info("Detected MIME-Type of " + file.getName() + " is " + mimeType);
        return mimeType;
    }

    /**
     * Return the standard extension of a specific MIME-Type.
     * What are these files "normally" called?
     *
     * @param mimeType MIME-Type, e.g. "text/plain"
     * @return Extension, e.g. "txt"
     */
    public String getStandardExtensionForMimeType(String mimeType) {
        List<String> extensions = getExtensionsCached(mimeType);

        if (extensions == null)
            return null;

        try {
            return extensions.get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    Map<String, List<String>> extensionsCache = new HashMap<String, List<String>>();

    @SuppressWarnings("unchecked")
    protected List<String> getExtensionsCached(String mimeType) {
        List<String> extensions = extensionsCache.get(mimeType);
        if (extensions != null)
            return extensions;
/*

        MimeTypes all = MimeTypes.getDefaultMimeTypes();

        try {
            extensions = (List<String>) all.forName(mimeType).getExtensions();
        } catch (MimeTypeException e) {
            e.printStackTrace();
        }

        for (MimeTypeIdentifier identifier : extraIdentifiers) {
            if (extensions != null)
                return extensions;

            extensions = identifier.getExtensionsFor(mimeType);
        }
*/


        extensions = new ArrayList<>();
        switch (mimeType){
            case "application/vnd.openxmlformats-officedocument.wordprocessingml":
                extensions.add("docx");
                extensions.add("dotx");
                break;
            case "application/vnd.openxmlformats-officedocument.presentationml":
                extensions.add("pptx");
                extensions.add("sldx");
                extensions.add("ppsx");
                extensions.add("potx");
                break;
            case "application/vnd.openxmlformats-officedocument.spreadsheetml":
                extensions.add("xlsx");
                extensions.add("xltx");
                break;
            case " application/vnd.ms-powerpoint":
                extensions.add("ppt");
                extensions.add("ppam");
                extensions.add("sldm");
                extensions.add("pptm");
                extensions.add("ppsm");
                extensions.add("potm");
                break;
            case "application/msword":
                extensions.add("doc");
                extensions.add("docm");
                extensions.add("dotm");
                break;
            case "application/pdf":
                extensions.add("pdf");
                break;
            default:
                mLog.warn("no ext found!");
                break;

        }
        extensionsCache.put(mimeType, extensions);
        return extensions;
    }

    /**
     * Test if an given extension can contain a File of MIME-Type
     *
     * @param extension Filename extension (e.g. "txt")
     * @param mimeType  MIME-Type		   (e.g. "text/plain")
     * @return True if compatible.
     */
    public boolean doesExtensionMatchMimeType(String extension, String mimeType) {
        List<String> extensions = null;
        extensions = getExtensionsCached(mimeType);
        if (extensions == null)
            return false;

        return extensions.contains(extension);
    }
}
