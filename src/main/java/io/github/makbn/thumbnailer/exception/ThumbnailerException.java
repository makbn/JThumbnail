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

package io.github.makbn.thumbnailer.exception;

import java.io.Serial;

/**
 * Thrown if Thumbnailing process failed.
 */
public class ThumbnailerException extends Exception {

    @Serial
    private static final long serialVersionUID = -7988812285439060247L;

    public ThumbnailerException() {
        super();
    }

    public ThumbnailerException(String message) {
        super(message);
    }

    public ThumbnailerException(Throwable cause) {
        super(cause);
    }

    public ThumbnailerException(String message, Throwable cause) {
        super(message, cause);
    }

}
