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

package com.senither.hypixel.commands;

import com.senither.hypixel.contracts.commands.Command;

import java.util.List;

public class CommandContainer {

    private final Command command;
    private final String category;

    CommandContainer(Command command) {
        this.command = command;

        String[] parts = command.getClass().getTypeName().split("\\.");
        String categoryPackage = parts[parts.length - 2];

        category = categoryPackage.substring(0, 1).toUpperCase() + categoryPackage.substring(1, categoryPackage.length()).toLowerCase();
    }

    public Command getCommand() {
        return command;
    }

    public CategoryIcons getCategoryIcon() {
        return CategoryIcons.fromName(getCategory());
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return command.getName();
    }

    public List<String> getDescription() {
        return command.getDescription();
    }

    public List<String> getTriggers() {
        return command.getTriggers();
    }

    public boolean isVisible() {
        return command.isVisible();
    }
}
