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

package com.senither.hypixel.statistics.responses;

import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.StatisticsResponse;
import com.senither.hypixel.inventory.ItemRarity;
import com.senither.hypixel.utils.NumberUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PetsResponse extends StatisticsResponse {

    private final boolean hasData;
    private final List<Pet> pets;

    public PetsResponse(boolean apiEnable, boolean hasData) {
        super(apiEnable);

        this.hasData = hasData;
        this.pets = new ArrayList<>();
    }

    public boolean hasData() {
        return hasData;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public PetsResponse addPet(String type, String tier, long experience, boolean active) {
        pets.add(new Pet(type, tier, experience, active));

        return this;
    }

    @Nullable
    public Pet getActivePet() {
        for (Pet pet : getPets()) {
            if (pet.isActive()) {
                return pet;
            }
        }
        return null;
    }

    public class Pet {

        private final String type;
        private final ItemRarity tier;
        private final long experience;
        private final boolean active;

        Pet(String type, String tier, long experience, boolean active) {
            this.type = type;
            this.experience = experience;
            this.active = active;

            this.tier = ItemRarity.fromName(tier);
        }

        public String getName() {
            String[] parts = getType().toLowerCase().split(" ");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[0].substring(0, 1).toUpperCase() + parts[i].substring(1, parts[i].length());
            }
            return String.join(" ", parts);
        }

        public String getType() {
            return type;
        }

        public ItemRarity getTier() {
            return tier;
        }

        public long getExperience() {
            return experience;
        }

        public double getLevel() {
            Integer offset = Constants.PET_OFFSET.get(getTier());

            long experience = getExperience();
            for (int i = offset; i < offset + 99; i++) {
                Integer levelXp = Constants.PET_EXPERIENCE.asList().get(i);

                if (experience > levelXp) {
                    experience -= levelXp;
                    continue;
                }

                return i + 1 - offset;
            }
            return 100;
        }

        public boolean isActive() {
            return active;
        }

        public String toFormattedString() {
            return String.format("LvL %s %s %s",
                NumberUtil.formatNicely(getLevel()),
                getTier().getName(),
                getName()
            );
        }
    }
}
