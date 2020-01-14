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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.senither.hypixel.contracts.inventory.Inventory;
import com.senither.hypixel.contracts.inventory.Searchable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PlayerInventory extends Inventory implements Searchable {

    private final List<String> itemNames = new ArrayList<>();

    public PlayerInventory(String encodedInventory) throws IOException {
        Tag tag = decodeInventoryString(encodedInventory);
        if (tag == null || !(tag.getValue() instanceof LinkedHashMap)) {
            throw new IOException("Decoded inventory is not a LinkedHashMap, unable to parse inventory data!");
        }

        ListTag items = (ListTag) ((LinkedHashMap<Object, Object>) tag.getValue()).get("i");

        for (Tag itemStack : items.getValue()) {
            LinkedHashMap itemMap = ((LinkedHashMap<Object, Object>) itemStack.getValue());
            if (itemMap == null) {
                continue;
            }

            CompoundTag itemTag = (CompoundTag) itemMap.get("tag");
            if (itemTag == null) {
                continue;
            }

            CompoundTag displayTag = (CompoundTag) itemTag.getValue().get("display");

            itemNames.add(displayTag.get("Name").getValue().toString().replaceAll("^([!|#|%]?[§]+[a-f|0-9])", ""));
        }
    }

    @Override
    public boolean hasItem(String name) {
        return itemNames.contains(name);
    }

    public List<String> getItemNames() {
        return itemNames;
    }
}
