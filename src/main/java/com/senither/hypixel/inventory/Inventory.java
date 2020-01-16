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

package com.senither.hypixel.inventory;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.senither.hypixel.contracts.inventory.InventoryDecoder;
import com.senither.hypixel.contracts.inventory.Searchable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Inventory implements Searchable, InventoryDecoder {

    private final List<Item> items = new ArrayList<>();

    public Inventory(String encodedInventory) throws IOException {
        Tag decodedInventoryTag = decodeInventoryString(encodedInventory);
        if (decodedInventoryTag == null || !(decodedInventoryTag instanceof CompoundTag)) {
            throw new IOException("Decoded inventory is not a LinkedHashMap, unable to parse inventory data!");
        }

        Tag inventoryContentsTag = ((CompoundTag) decodedInventoryTag).get("i");
        if (!(inventoryContentsTag instanceof ListTag)) {
            throw new IOException("Inventory contents tag is not a list, unable to get inventory contents!");
        }

        for (Tag tag : (ListTag) inventoryContentsTag) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }

            CompoundTag itemTag = (CompoundTag) tag;
            if (itemTag.isEmpty()) {
                continue;
            }

            items.add(new Item(this, itemTag));
        }
    }

    void addBackpackContent(ByteArrayTag backpackData) {
        try {
            Inventory inventory = new Inventory(Base64.getEncoder().encodeToString(backpackData.getValue()));

            items.addAll(inventory.getItems());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasItem(String name) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public List<Item> getItems() {
        return items;
    }
}
