/*
 * Copyright (c) 2020.
 *
 * This file is part of Hypixel Skyblock Assistant.
 *
 * Hypixel Guild Synchronizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypixel Guild Synchronizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hypixel Guild Synchronizer.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.senither.hypixel.contracts.inventory;

import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.io.ByteSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public interface InventoryDecoder {

    default Tag decodeInventoryString(String inventory) throws IOException {
        final byte[] decodedInventory = Base64.getDecoder().decode(inventory);
        if (decodedInventory == null || decodedInventory.length == 0) {
            throw new IOException("Failed to decode inventory, inventory can't be empty or null!");
        }

        if (!isCompressed(decodedInventory)) {
            return NBTIO.readTag(ByteSource.wrap(decodedInventory).openStream());
        }
        return NBTIO.readTag(new GZIPInputStream(new ByteArrayInputStream(decodedInventory)));
    }

    default ByteArrayTag getInventoryDataFromAttributes(CompoundTag tag) {
        for (String key : tag.getValue().keySet()) {
            if (key.endsWith("_backpack_data")) {
                return tag.get(key);
            }
        }
        return null;
    }

    default boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
            && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
