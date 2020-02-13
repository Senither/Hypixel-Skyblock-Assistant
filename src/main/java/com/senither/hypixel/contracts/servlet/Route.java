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

package com.senither.hypixel.contracts.servlet;

import spark.ModelAndView;
import spark.TemplateViewRoute;

import java.util.HashMap;

public abstract class Route implements TemplateViewRoute {

    protected final ModelAndView view(String name) {
        return new ModelAndView(new HashMap<String, Object>(), prepareRouteName(name));
    }

    protected final ModelAndView view(String name, HashMap<String, Object> model) {
        if (model == null) {
            return view(name);
        }
        return new ModelAndView(model, prepareRouteName(name));
    }

    private String prepareRouteName(String name) {
        if (name.startsWith("/")) {
            return prepareRouteName(name.substring(1, name.length()));
        }

        if (!name.endsWith(".vm")) {
            name += ".vm";
        }

        if (!name.startsWith("views/")) {
            name = "views/" + name;
        }

        return name;
    }
}
