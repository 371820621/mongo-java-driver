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

package org.mongodb.command;

import org.mongodb.Document;
import org.mongodb.operation.FindAndModify;

final class CommandDocumentTemplates {

    private CommandDocumentTemplates() {
    }

    static Document getFindAndModify(final FindAndModify findAndModify, final String collectionName) {
        final Document cmd = new Document("findandmodify", collectionName);
        if (findAndModify.getFilter() != null) {
            cmd.put("query", findAndModify.getFilter());
        }
        if (findAndModify.getSelector() != null) {
            cmd.put("fields", findAndModify.getSelector());
        }
        if (findAndModify.getSortCriteria() != null) {
            cmd.put("sort", findAndModify.getSortCriteria());
        }
        if (!findAndModify.isRemove()) {
            if (findAndModify.isReturnNew()) {
                cmd.put("new", true);
            }
            if (findAndModify.isUpsert()) {
                cmd.put("upsert", true);
            }
        }

        return cmd;
    }
}
