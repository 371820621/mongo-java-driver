/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.codecs;

import org.bson.BSONReader;
import org.bson.BSONType;
import org.bson.BSONWriter;
import org.mongodb.Codec;
import org.mongodb.Decoder;

import java.util.Collection;

//Can't implement Codec<Iterable<?>> like we want to because getEncoderClass is unimplementable
@SuppressWarnings("rawtypes")
public class IterableCodec implements Codec<Iterable> {
    private final CollectionFactory collectionFactory;
    private final Decoder<?> decoder;
    private final EncoderRegistry encoderRegistry;

    public IterableCodec(final Codecs codecs, final EncoderRegistry encoderRegistry) {
        this(new ArrayListFactory(), codecs, encoderRegistry);
    }

    public IterableCodec(final CollectionFactory collectionFactory, final Decoder<?> decoder,
                         final EncoderRegistry encoderRegistry) {
        this.collectionFactory = collectionFactory;
        this.decoder = decoder;
        this.encoderRegistry = encoderRegistry;
    }

    @Override
    public void encode(final BSONWriter bsonWriter, final Iterable iterable) {
        bsonWriter.writeStartArray();
        for (final Object value : iterable) {
            CodecUtils.encode(encoderRegistry, bsonWriter, value);
        }
        bsonWriter.writeEndArray();
    }

    // The decode has to do an unchecked cast to turn the decoded object into the correct type
    @SuppressWarnings("unchecked")
    @Override
    public <E> Iterable<E> decode(final BSONReader reader) {
        reader.readStartArray();
        final Collection<E> collection = collectionFactory.createCollection();
        while (reader.readBSONType() != BSONType.END_OF_DOCUMENT) {
            // Need to test under which circumstances a ClassCastException might be thrown
            collection.add((E) decoder.decode(reader));
        }
        reader.readEndArray();
        return collection;
    }

    @Override
    public Class<Iterable> getEncoderClass() {
        return Iterable.class;
    }
}
