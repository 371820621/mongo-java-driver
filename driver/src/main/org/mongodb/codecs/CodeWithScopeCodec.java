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
import org.bson.BSONWriter;
import org.bson.types.CodeWithScope;
import org.mongodb.Codec;
import org.mongodb.Document;

public class CodeWithScopeCodec implements Codec<CodeWithScope> {
    private final EncoderRegistry encoderRegistry;
    private final SimpleDocumentCodec simpleDocumentCodec;

    public CodeWithScopeCodec(final Codecs codecs, final EncoderRegistry encoderRegistry) {
        this.encoderRegistry = encoderRegistry;
        simpleDocumentCodec = new SimpleDocumentCodec(codecs);
    }

    @Override
    public CodeWithScope decode(final BSONReader bsonReader) {
        final String code = bsonReader.readJavaScriptWithScope();
        final Document scope = simpleDocumentCodec.decode(bsonReader);
        return new CodeWithScope(code, scope);
    }

    @Override
    public void encode(final BSONWriter bsonWriter, final CodeWithScope codeWithScope) {
        bsonWriter.writeJavaScriptWithScope(codeWithScope.getCode());
        CodecUtils.encode(encoderRegistry, bsonWriter, codeWithScope.getScope());
    }

    @Override
    public Class<CodeWithScope> getEncoderClass() {
        return CodeWithScope.class;
    }
}
