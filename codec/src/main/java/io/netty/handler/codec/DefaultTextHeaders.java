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

package io.netty.handler.codec;

import io.netty.util.internal.PlatformDependent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

public class DefaultTextHeaders implements TextHeaders {

    private static final int BUCKET_SIZE = 17;

    private static int index(int hash) {
        return Math.abs(hash % BUCKET_SIZE);
    }

    @SuppressWarnings("unchecked")
    private final HeaderEntry[] entries = new HeaderEntry[BUCKET_SIZE];
    private final HeaderEntry head = new HeaderEntry(this);
    private final boolean ignoreCase;
    int size;

    public DefaultTextHeaders() {
        this(true);
    }

    public DefaultTextHeaders(boolean ignoreCase) {
        head.before = head.after = head;
        this.ignoreCase = ignoreCase;
    }

    protected int hashKey(CharSequence key) {
        return key.hashCode();
    }

    protected void validateKey(CharSequence key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
    }

    @SuppressWarnings("unchecked")
    protected CharSequence convertValue(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (value instanceof CharSequence) {
            return (CharSequence) value;
        }
        return value.toString();
    }

    protected boolean keyEquals(CharSequence a, CharSequence b) {
        return equals(a, b, ignoreCase);
    }

    protected boolean valueEquals(CharSequence a, CharSequence b, boolean ignoreCase) {
        return equals(a, b, ignoreCase);
    }

    private static boolean equals(CharSequence a, CharSequence b, boolean ignoreCase) {
        if (a == b) {
            return true;
        }

        if (a instanceof AsciiString) {
            AsciiString aa = (AsciiString) a;
            if (ignoreCase) {
                return aa.equalsIgnoreCase(b);
            } else {
                return aa.equals(b);
            }
        }

        if (b instanceof AsciiString) {
            AsciiString ab = (AsciiString) b;
            if (ignoreCase) {
                return ab.equalsIgnoreCase(a);
            } else {
                return ab.equals(a);
            }
        }

        if (ignoreCase) {
            return a.toString().equalsIgnoreCase(b.toString());
        } else {
            return a.equals(b);
        }
    }

    @Override
    public TextHeaders add(CharSequence key, Object value) {
        validateKey(key);
        CharSequence convertedVal = convertValue(value);
        int h = hashKey(key);
        int i = index(h);
        add0(h, i, key, convertedVal);
        return this;
    }

    @Override
    public TextHeaders add(CharSequence key, Iterable<?> values) {
        validateKey(key);
        if (values == null) {
            throw new NullPointerException("values");
        }

        int h = hashKey(key);
        int i = index(h);
        for (Object v: values) {
            if (v == null) {
                break;
            }
            CharSequence convertedVal = convertValue(v);
            add0(h, i, key, convertedVal);
        }
        return this;
    }

    @Override
    public TextHeaders add(CharSequence key, Object... values) {
        validateKey(key);
        if (values == null) {
            throw new NullPointerException("values");
        }

        int h = hashKey(key);
        int i = index(h);
        for (Object v: values) {
            if (v == null) {
                break;
            }
            CharSequence convertedVal = convertValue(v);
            add0(h, i, key, convertedVal);
        }
        return this;
    }

    private void add0(int h, int i, CharSequence key, CharSequence value) {
        // Update the hash table.
        HeaderEntry e = entries[i];
        HeaderEntry newEntry;
        entries[i] = newEntry = new HeaderEntry(this, h, key, value);
        newEntry.next = e;

        // Update the linked list.
        newEntry.addBefore(head);
    }

    @Override
    public TextHeaders add(TextHeaders headers) {
        if (headers == null) {
            throw new NullPointerException("headers");
        }

        add0(headers);
        return this;
    }

    private void add0(TextHeaders headers) {
        if (headers.isEmpty()) {
            return;
        }

        if (headers instanceof DefaultTextHeaders) {
            @SuppressWarnings("unchecked")
            DefaultTextHeaders m = (DefaultTextHeaders) headers;
            HeaderEntry e = m.head.after;
            while (e != m.head) {
                CharSequence key = e.key;
                validateKey(key);
                add(key, convertValue(e.value));
                e = e.after;
            }
        } else {
            for (Entry<CharSequence, CharSequence> e: headers.unconvertedEntries()) {
                add(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public boolean remove(CharSequence key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        int h = hashKey(key);
        int i = index(h);
        return remove0(h, i, key);
    }

    private boolean remove0(int h, int i, CharSequence key) {
        HeaderEntry e = entries[i];
        if (e == null) {
            return false;
        }

        boolean removed = false;
        for (;;) {
            if (e.hash == h && keyEquals(e.key, key)) {
                e.remove();
                HeaderEntry next = e.next;
                if (next != null) {
                    entries[i] = next;
                    e = next;
                } else {
                    entries[i] = null;
                    return true;
                }
                removed = true;
            } else {
                break;
            }
        }

        for (;;) {
            HeaderEntry next = e.next;
            if (next == null) {
                break;
            }
            if (next.hash == h && keyEquals(next.key, key)) {
                e.next = next.next;
                next.remove();
                removed = true;
            } else {
                e = next;
            }
        }

        return removed;
    }

    @Override
    public TextHeaders set(CharSequence key, Object value) {
        validateKey(key);
        CharSequence convertedVal = convertValue(value);
        int h = hashKey(key);
        int i = index(h);
        remove0(h, i, key);
        add0(h, i, key, convertedVal);
        return this;
    }

    @Override
    public TextHeaders set(CharSequence key, Iterable<?> values) {
        validateKey(key);
        if (values == null) {
            throw new NullPointerException("values");
        }

        int h = hashKey(key);
        int i = index(h);

        remove0(h, i, key);
        for (Object v: values) {
            if (v == null) {
                break;
            }
            CharSequence convertedVal = convertValue(v);
            add0(h, i, key, convertedVal);
        }

        return this;
    }

    @Override
    public TextHeaders set(CharSequence key, Object... values) {
        validateKey(key);
        if (values == null) {
            throw new NullPointerException("values");
        }

        int h = hashKey(key);
        int i = index(h);

        remove0(h, i, key);
        for (Object v: values) {
            if (v == null) {
                break;
            }
            CharSequence convertedVal = convertValue(v);
            add0(h, i, key, convertedVal);
        }

        return this;
    }

    @Override
    public TextHeaders set(TextHeaders headers) {
        if (headers == null) {
            throw new NullPointerException("headers");
        }

        clear();
        add0(headers);
        return this;
    }

    @Override
    public TextHeaders clear() {
        Arrays.fill(entries, null);
        head.before = head.after = head;
        size = 0;
        return this;
    }

    @Override
    public CharSequence getUnconverted(CharSequence key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        int h = hashKey(key);
        int i = index(h);
        HeaderEntry e = entries[i];
        CharSequence value = null;
        // loop until the first header was found
        while (e != null) {
            if (e.hash == h && keyEquals(e.key, key)) {
                value = e.value;
            }

            e = e.next;
        }
        if (value != null) {
            return value;
        }
        return null;
    }

    @Override
    public String get(CharSequence key) {
        CharSequence v = getUnconverted(key);
        if (v == null) {
            return null;
        }
        return v.toString();
    }

    @Override
    public List<CharSequence> getAllUnconverted(CharSequence key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        int cnt = 0;
        int size = size();
        CharSequence[] values = new CharSequence[size];

        int h = hashKey(key);
        int i = index(h);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && keyEquals(e.key, key)) {
                values[cnt ++] = e.getValue();
            }
            e = e.next;
        }

        assert size == cnt;
        return Arrays.asList(values);
    }

    @Override
    public List<String> getAll(CharSequence key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        int cnt = 0;
        int size = size();
        String[] values = new String[size];

        int h = hashKey(key);
        int i = index(h);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && keyEquals(e.key, key)) {
                values[cnt ++] = e.getValue().toString();
            }
            e = e.next;
        }

        assert size == cnt;
        return Arrays.asList(values);
    }

    @Override
    public List<Map.Entry<String, String>> entries() {
        int cnt = 0;
        int size = size();
        @SuppressWarnings("unchecked")
        Map.Entry<String, String>[] all = new Map.Entry[size];

        HeaderEntry e = head.after;
        while (e != head) {
            all[cnt ++] = new StringHeaderEntry(e);
            e = e.after;
        }

        assert size == cnt;
        return Arrays.asList(all);
    }

    @Override
    public List<Map.Entry<CharSequence, CharSequence>> unconvertedEntries() {
        int cnt = 0;
        int size = size();
        @SuppressWarnings("unchecked")
        Map.Entry<CharSequence, CharSequence>[] all = new Map.Entry[size];

        HeaderEntry e = head.after;
        while (e != head) {
            all[cnt ++] = e;
            e = e.after;
        }

        assert size == cnt;
        return Arrays.asList(all);
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return new StringHeaderIterator();
    }

    @Override
    public Iterator<Entry<CharSequence, CharSequence>> unconvertedIterator() {
        return new HeaderIterator();
    }

    @Override
    public boolean contains(CharSequence key) {
        return getUnconverted(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return head == head.after;
    }

    @Override
    public boolean contains(CharSequence name, Object value) {
        return contains(name, value, false);
    }

    @Override
    public boolean contains(CharSequence name, Object value, boolean ignoreCase) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        int h = hashKey(name);
        int i = index(h);
        CharSequence convertedVal = convertValue(value);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && keyEquals(e.key, name)) {
                if (valueEquals(e.value, convertedVal, ignoreCase)) {
                    return true;
                }
            }
            e = e.next;
        }
        return false;
    }

    @Override
    public Set<CharSequence> unconvertedNames() {
        Set<CharSequence> keys = new LinkedHashSet<CharSequence>(size());
        HeaderEntry e = head.after;
        while (e != head) {
            keys.add(e.getKey());
            e = e.after;
        }
        return keys;
    }

    @Override
    public Set<String> names() {
        Set<String> keys = new LinkedHashSet<String>(size());
        HeaderEntry e = head.after;
        while (e != head) {
            keys.add(e.getKey().toString());
            e = e.after;
        }
        return keys;
    }

    @Override
    public TextHeaders forEachEntry(TextHeaderProcessor processor) {
        HeaderEntry e = head.after;
        try {
            while (e != head) {
                if (!processor.process(e.getKey(), e.getValue())) {
                    break;
                }
                e = e.after;
            }
        } catch (Exception ex) {
            PlatformDependent.throwException(ex);
        }
        return this;
    }

    private static final class HeaderEntry implements Map.Entry<CharSequence, CharSequence> {
        private final DefaultTextHeaders parent;
        final int hash;
        final CharSequence key;
        CharSequence value;
        HeaderEntry next;
        HeaderEntry before, after;

        HeaderEntry(DefaultTextHeaders parent, int hash, CharSequence key, CharSequence value) {
            this.parent = parent;
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        HeaderEntry(DefaultTextHeaders parent) {
            this.parent = parent;
            hash = -1;
            key = null;
            value = null;
        }

        void remove() {
            before.after = after;
            after.before = before;
            parent.size --;
        }

        void addBefore(HeaderEntry e) {
            after  = e;
            before = e.before;
            before.after = this;
            after.before = this;
            parent.size ++;
        }

        @Override
        public CharSequence getKey() {
            return key;
        }

        @Override
        public CharSequence getValue() {
            return value;
        }

        @Override
        public CharSequence setValue(CharSequence value) {
            if (value == null) {
                throw new NullPointerException("value");
            }
            value = parent.convertValue(value);
            CharSequence oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public String toString() {
            return key.toString() + '=' + value.toString();
        }
    }


    private static final class StringHeaderEntry implements Entry<String, String> {
        private final Entry<CharSequence, CharSequence> entry;
        private String key;
        private String value;

        StringHeaderEntry(Entry<CharSequence, CharSequence> entry) {
            this.entry = entry;
        }

        @Override
        public String getKey() {
            if (key == null) {
                key = entry.getKey().toString();
            }
            return key;
        }

        @Override
        public String getValue() {
            if (value == null) {
                value = entry.getValue().toString();
            }
            return value;
        }

        @Override
        public String setValue(String value) {
            return entry.setValue(value).toString();
        }

        @Override
        public String toString() {
            return entry.toString();
        }
    }

    private final class HeaderIterator implements Iterator<Map.Entry<CharSequence, CharSequence>> {

        private HeaderEntry current = head;

        @Override
        public boolean hasNext() {
            return current.after != head;
        }

        @Override
        public Entry<CharSequence, CharSequence> next() {
            current = current.after;

            if (current == head) {
                throw new NoSuchElementException();
            }

            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class StringHeaderIterator implements Iterator<Map.Entry<String, String>> {

        private HeaderEntry current = head;

        @Override
        public boolean hasNext() {
            return current.after != head;
        }

        @Override
        public Entry<String, String> next() {
            current = current.after;

            if (current == head) {
                throw new NoSuchElementException();
            }

            return new StringHeaderEntry(current);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
