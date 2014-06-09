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
import io.netty.handler.codec.DefaultTextHeaders;
import io.netty.handler.codec.TextHeaderProcessor;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


public class DefaultSpdyHeaders extends DefaultTextHeaders implements SpdyHeaders {
    @Override
    protected CharSequence convertName(CharSequence name) {
        name = super.convertName(name);
        if (name instanceof AsciiString) {
            name = ((AsciiString) name).toLowerCase();
        } else {
            name = name.toString().toLowerCase();
        }
        SpdyCodecUtil.validateHeaderName(name);
        return name;
    }

    @Override
    protected CharSequence convertValue(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        CharSequence seq;
        if (value instanceof CharSequence) {
            seq = (CharSequence) value;
        } else {
            seq = value.toString();
        }

        SpdyCodecUtil.validateHeaderValue(seq);
        return seq;
    }

    @Override
    public boolean removeHost() {
        return remove(HttpNames.HOST);
    }

    @Override
    public String getHost() {
        return get(HttpNames.HOST);
    }

    @Override
    public SpdyHeaders setHost(String host) {
        set(HttpNames.HOST, host);
        return this;
    }

    @Override
    public boolean removeMethod(int spdyVersion) {
        return remove(HttpNames.METHOD);
    }

    @Override
    public HttpMethod getMethod(int spdyVersion) {
        try {
            return HttpMethod.valueOf(get(HttpNames.METHOD));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public SpdyHeaders setMethod(int spdyVersion, HttpMethod method) {
        set(HttpNames.METHOD, method.name());
        return this;
    }

    @Override
    public boolean removeScheme(int spdyVersion) {
        return remove(HttpNames.SCHEME);
    }

    @Override
    public String getScheme(int spdyVersion) {
        return get(HttpNames.SCHEME);
    }

    @Override
    public SpdyHeaders setScheme(int spdyVersion, String scheme) {
        set(HttpNames.SCHEME, scheme);
        return this;
    }

    @Override
    public boolean removeStatus(int spdyVersion) {
        return remove(HttpNames.STATUS);
    }

    @Override
    public HttpResponseStatus getStatus(int spdyVersion) {
        try {
            String status = get(HttpNames.STATUS);
            int space = status.indexOf(' ');
            if (space == -1) {
                return HttpResponseStatus.valueOf(Integer.parseInt(status));
            } else {
                int code = Integer.parseInt(status.substring(0, space));
                String reasonPhrase = status.substring(space + 1);
                HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(code);
                if (responseStatus.reasonPhrase().equals(reasonPhrase)) {
                    return responseStatus;
                } else {
                    return new HttpResponseStatus(code, reasonPhrase);
                }
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public SpdyHeaders setStatus(int spdyVersion, HttpResponseStatus status) {
        set(HttpNames.STATUS, status.toString());
        return this;
    }

    @Override
    public boolean removePath(int spdyVersion) {
        return remove(HttpNames.PATH);
    }

    @Override
    public String getPath(int spdyVersion) {
        return get(HttpNames.PATH);
    }

    @Override
    public SpdyHeaders setPath(int spdyVersion, String path) {
        set(HttpNames.PATH, path);
        return this;
    }

    @Override
    public boolean removeVersion(int spdyVersion) {
        return remove(HttpNames.VERSION);
    }

    @Override
    public HttpVersion getVersion(int spdyVersion) {
        try {
            return HttpVersion.valueOf(get(HttpNames.VERSION));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public SpdyHeaders setVersion(int spdyVersion, HttpVersion httpVersion) {
        set(HttpNames.VERSION, httpVersion.text());
        return this;
    }

    @Override
    public SpdyHeaders add(CharSequence name, Object value) {
        super.add(name, value);
        return this;
    }

    @Override
    public SpdyHeaders add(CharSequence name, Iterable<?> values) {
        super.add(name, values);
        return this;
    }

    @Override
    public SpdyHeaders add(CharSequence name, Object... values) {
        super.add(name, values);
        return this;
    }

    @Override
    public SpdyHeaders add(TextHeaders headers) {
        super.add(headers);
        return this;
    }

    @Override
    public SpdyHeaders set(CharSequence name, Object value) {
        super.set(name, value);
        return this;
    }

    @Override
    public SpdyHeaders set(CharSequence name, Object... values) {
        super.set(name, values);
        return this;
    }

    @Override
    public SpdyHeaders set(CharSequence name, Iterable<?> values) {
        super.set(name, values);
        return this;
    }

    @Override
    public SpdyHeaders set(TextHeaders headers) {
        super.set(headers);
        return this;
    }

    @Override
    public SpdyHeaders clear() {
        super.clear();
        return this;
    }

    @Override
    public SpdyHeaders forEachEntry(TextHeaderProcessor processor) {
        super.forEachEntry(processor);
        return this;
    }
}
