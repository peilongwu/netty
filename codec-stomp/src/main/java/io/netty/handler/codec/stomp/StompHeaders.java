/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.stomp;

import io.netty.handler.codec.AsciiString;

/**
 * Provides the constants for the standard STOMP header names and values.
 */
public final class StompHeaders {

    public static final AsciiString ACCEPT_VERSION = new AsciiString("accept-version");
    public static final AsciiString HOST = new AsciiString("host");
    public static final AsciiString LOGIN = new AsciiString("login");
    public static final AsciiString PASSCODE = new AsciiString("passcode");
    public static final AsciiString HEART_BEAT = new AsciiString("heart-beat");
    public static final AsciiString VERSION = new AsciiString("version");
    public static final AsciiString SESSION = new AsciiString("session");
    public static final AsciiString SERVER = new AsciiString("server");
    public static final AsciiString DESTINATION = new AsciiString("destination");
    public static final AsciiString ID = new AsciiString("id");
    public static final AsciiString ACK = new AsciiString("ack");
    public static final AsciiString TRANSACTION = new AsciiString("transaction");
    public static final AsciiString RECEIPT = new AsciiString("receipt");
    public static final AsciiString MESSAGE_ID = new AsciiString("message-id");
    public static final AsciiString SUBSCRIPTION = new AsciiString("subscription");
    public static final AsciiString RECEIPT_ID = new AsciiString("receipt-id");
    public static final AsciiString MESSAGE = new AsciiString("message");
    public static final AsciiString CONTENT_LENGTH = new AsciiString("content-length");
    public static final AsciiString CONTENT_TYPE = new AsciiString("content-type");

    private StompHeaders() { }
}
