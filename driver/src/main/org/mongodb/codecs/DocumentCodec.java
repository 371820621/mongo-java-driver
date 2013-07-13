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
import org.mongodb.Document;
import org.mongodb.Encoder;
import org.mongodb.codecs.validators.QueryFieldNameValidator;
import org.mongodb.codecs.validators.Validator;

import java.util.Map;

// TODO: decode into DBRef?
public class DocumentCodec implements Codec<Document> {
    private final PrimitiveCodecs primitiveCodecs;
    private final Validator<String> fieldNameValidator;
    private final Codecs codecs;
    private final EncoderRegistry encoderRegistry;

    public DocumentCodec() {
        this(PrimitiveCodecs.createDefault());
    }

    public DocumentCodec(final PrimitiveCodecs primitiveCodecs) {
        this(primitiveCodecs, new QueryFieldNameValidator());
    }

    protected DocumentCodec(final PrimitiveCodecs primitiveCodecs, final Validator<String> fieldNameValidator) {
        this(primitiveCodecs, fieldNameValidator, new EncoderRegistry());
    }

    protected DocumentCodec(final PrimitiveCodecs primitiveCodecs, final Validator<String> fieldNameValidator,
                            final EncoderRegistry encoderRegistry) {
        if (primitiveCodecs == null) {
            throw new IllegalArgumentException("primitiveCodecs is null");
        }
        this.encoderRegistry = encoderRegistry;
        this.fieldNameValidator = fieldNameValidator;
        this.primitiveCodecs = primitiveCodecs;
        codecs = new Codecs(primitiveCodecs, fieldNameValidator, encoderRegistry);
    }

    @Override
    public void encode(final BSONWriter bsonWriter, final Document document) {
        bsonWriter.writeStartDocument();

        beforeFields(bsonWriter, document);

        for (final Map.Entry<String, Object> entry : document.entrySet()) {
            fieldNameValidator.validate(entry.getKey());

            if (skipField(entry.getKey())) {
                continue;
            }
            bsonWriter.writeName(entry.getKey());
            writeValue(bsonWriter, entry.getValue());
        }
        bsonWriter.writeEndDocument();
    }

//    protected void writeValue(final BSONWriter bsonWriter, final Object value) {
//        final Encoder<Object> encoder = (Encoder<Object>) encoderRegistry.get(value.getClass());
//        encoder.encode(bsonWriter, value);
//    }

    protected void beforeFields(final BSONWriter bsonWriter, final Document document) {
    }

    protected boolean skipField(final String key) {
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void writeValue(final BSONWriter bsonWriter, final Object value) {
        codecs.encode(bsonWriter, value);
    }

    @Override
    public Document decode(final BSONReader reader) {
        final Document document = new Document();

        reader.readStartDocument();
        while (reader.readBSONType() != BSONType.END_OF_DOCUMENT) {
            final String fieldName = reader.readName();
            document.put(fieldName, readValue(reader, fieldName));
        }

        reader.readEndDocument();

        return document;
    }

    protected Object readValue(final BSONReader reader, final String fieldName) {
        final BSONType bsonType = reader.getCurrentBSONType();
        if (bsonType.equals(BSONType.DOCUMENT)) {
            return getDecoderForField(fieldName).decode(reader);
        } else {
            return codecs.decode(reader);
        }
    }

    @Override
    public Class<Document> getEncoderClass() {
        return Document.class;
    }

    protected PrimitiveCodecs getPrimitiveCodecs() {
        return primitiveCodecs;
    }

    protected Decoder<Document> getDecoderForField(final String fieldName) {
        return this;
    }
}
