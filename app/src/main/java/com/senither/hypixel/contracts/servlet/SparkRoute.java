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

import com.google.gson.JsonObject;
import spark.Response;
import spark.Route;

public abstract class SparkRoute implements Route {

    protected JsonObject buildResponse(Response response, int code, String message) {
        return buildResponse(response, code, message, null);
    }

    protected JsonObject buildResponse(Response response, int code, String message, JsonObject data) {
        response.status(code);

        JsonObject root = new JsonObject();

        root.addProperty("status", code);
        root.addProperty(code >= 200 && code < 400 ? "message" : "reason", message);

        if (data != null) {
            root.add("data", data);
        }

        return root;
    }

    protected JsonObject buildDataResponse(Response response, int code, JsonObject object) {
        response.status(code);

        JsonObject root = new JsonObject();

        root.addProperty("status", code);
        root.add("data", object);

        return root;
    }
}
