/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.idp.stepup.event.api;

/** Exception class indicating event setter was used for already locked in parameter. */
@SuppressWarnings("serial")
public class EventUnmodiafiableException extends Exception {

    /**
     * Constructor.
     */
    public EventUnmodiafiableException() {
    }

    /**
     * Constructor.
     * 
     * @param message
     *            exception msg
     */
    public EventUnmodiafiableException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            exception cause
     */
    public EventUnmodiafiableException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            exception msg
     * @param cause
     *            exception cause.
     */
    public EventUnmodiafiableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            exception msg
     * @param cause
     *            exception cause
     * @param enableSuppression
     *            suppression on/off
     * @param writableStackTrace
     *            stacktrace writable on/off
     */
    public EventUnmodiafiableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
