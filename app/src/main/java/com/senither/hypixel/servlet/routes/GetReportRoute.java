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

package com.senither.hypixel.servlet.routes;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.servlet.SparkRoute;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import spark.Request;
import spark.Response;

public class GetReportRoute extends SparkRoute {

    private final SkyblockAssistant app;

    public GetReportRoute(SkyblockAssistant app) {
        this.app = app;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String reportId = request.params("id");

        Collection result = app.getDatabaseManager().query("SELECT * FROM `reports` WHERE `id` = ?", reportId);
        if (result.isEmpty()) {
            return buildResponse(response, 404, "Report was not found!");
        }

        DataRow report = result.first();
        if (report.getString("data") == null) {
            return buildResponse(response, 206, "Report is still being generated!");
        }

        JsonObject data = app.getHypixel().getGson().fromJson(
            report.getString("data"), JsonObject.class
        );

        if (data.has("guildEntry")) {
            data.getAsJsonObject("guildEntry").add("data", app.getHypixel().getGson().fromJson(
                data.getAsJsonObject("guildEntry").get("data").getAsString(), JsonObject.class
            ));
        }

        return buildDataResponse(response, 200, data);
    }
}
