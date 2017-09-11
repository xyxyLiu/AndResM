/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reginald.andresm.arsc;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/** Represents a chunk whose payload is a list of sub-chunks. */
public abstract class ChunkWithChunks extends Chunk {

    private final List<Chunk> chunks = new LinkedList<>();

    protected ChunkWithChunks(ByteBuffer buffer, @Nullable Chunk parent) {
        super(buffer, parent);
    }

    @Override
    protected void init(ByteBuffer buffer) {
        super.init(buffer);
        chunks.clear();
        int start = this.offset + getHeaderSize();
        int offset = start;
        int end = this.offset + getOriginalChunkSize();
        int position = buffer.position();
        buffer.position(start);

        while (offset < end) {
            Chunk chunk = Chunk.newInstance(buffer, this);
            chunks.add(chunk);
            offset += chunk.getOriginalChunkSize();
        }

        buffer.position(position);
    }

    /**
     * Retrieves the @{code chunks} contained in this chunk.
     * @return list of chunk contained in this chunk.
     */
    public final List<Chunk> getChunks() {
        return chunks;
    }

    public void addChunk(int index, Chunk chunk) {
        if (index > chunks.size() || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + chunks.size());
        }

        chunks.add(index, chunk);
    }

    public void removeChunk(int index) {
        if (index > chunks.size() || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + chunks.size());
        }

        chunks.remove(index);
    }

    @Override
    protected void writePayload(DataOutput output, ByteBuffer header, boolean shrink)
            throws IOException {
        for (Chunk chunk : getChunks()) {
            byte[] chunkBytes = chunk.toByteArray(shrink);
            output.write(chunkBytes);
            writePad(output, chunkBytes.length);
        }
    }
}
