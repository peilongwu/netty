/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.spdy;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.TextHeaderProcessor;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Provides the constants for the standard SPDY HTTP header names and commonly
 * used utility methods that access a {@link SpdyHeadersFrame}.
 */
public interface SpdyHeaders extends TextHeaders {

    /**
     * SPDY HTTP header names
     */
    final class HttpNames {
        /**
         * {@code ":host"}
         */
        public static final AsciiString HOST = new AsciiString(":host");
        /**
         * {@code ":method"}
         */
        public static final AsciiString METHOD = new AsciiString(":method");
        /**
         * {@code ":path"}
         */
        public static final AsciiString PATH = new AsciiString(":path");
        /**
         * {@code ":scheme"}
         */
        public static final AsciiString SCHEME = new AsciiString(":scheme");
        /**
         * {@code ":status"}
         */
        public static final AsciiString STATUS = new AsciiString(":status");
        /**
         * {@code ":version"}
         */
        public static final AsciiString VERSION = new AsciiString(":version");

        private HttpNames() { }
    }

    /**
     * Removes the SPDY host header.
     */
    boolean removeHost();

    /**
     * Returns the SPDY host header.
     */
    String getHost();

    /**
     * Set the SPDY host header.
     */
    SpdyHeaders setHost(String host);

    /**
     * Removes the HTTP method header.
     */
    boolean removeMethod(int spdyVersion);

    /**
     * Returns the {@link HttpMethod} represented by the HTTP method header.
     */
    HttpMethod getMethod(int spdyVersion);

    /**
     * Sets the HTTP method header.
     */
    SpdyHeaders setMethod(int spdyVersion, HttpMethod method);

    /**
     * Removes the URL scheme header.
     */
    boolean removeScheme(int spdyVersion);

    /**
     * Returns the value of the URL scheme header.
     */
    String getScheme(int spdyVersion);

    /**
     * Sets the URL scheme header.
     */
    SpdyHeaders setScheme(int spdyVersion, String scheme);

    /**
     * Removes the HTTP response status header.
     */
    boolean removeStatus(int spdyVersion);

    /**
     * Returns the {@link HttpResponseStatus} represented by the HTTP response status header.
     */
    HttpResponseStatus getStatus(int spdyVersion);

    /**
     * Sets the HTTP response status header.
     */
    SpdyHeaders setStatus(int spdyVersion, HttpResponseStatus status);

    /**
     * Removes the URL path header.
     */
    boolean removePath(int spdyVersion);

    /**
     * Returns the value of the URL path header.
     */
    String getPath(int spdyVersion);

    /**
     * Sets the URL path header.
     */
    SpdyHeaders setPath(int spdyVersion, String path);

    /**
     * Removes the HTTP version header.
     */
    boolean removeVersion(int spdyVersion);

    /**
     * Returns the {@link HttpVersion} represented by the HTTP version header.
     */
    HttpVersion getVersion(int spdyVersion);

    /**
     * Sets the HTTP version header.
     */
    SpdyHeaders setVersion(int spdyVersion, HttpVersion httpVersion);

    @Override
    SpdyHeaders add(CharSequence name, Object value);

    @Override
    SpdyHeaders add(CharSequence name, Iterable<?> values);

    @Override
    SpdyHeaders add(CharSequence name, Object... values);

    @Override
    SpdyHeaders add(TextHeaders headers);

    @Override
    SpdyHeaders set(CharSequence name, Object value);

    @Override
    SpdyHeaders set(CharSequence name, Iterable<?> values);

    @Override
    SpdyHeaders set(CharSequence name, Object... values);

    @Override
    SpdyHeaders set(TextHeaders headers);

    @Override
    SpdyHeaders clear();

    @Override
    SpdyHeaders forEachEntry(TextHeaderProcessor processor);
}
