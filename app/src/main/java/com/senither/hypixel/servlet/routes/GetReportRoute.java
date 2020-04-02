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
import com.senither.hypixel.reports.Report;
import com.senither.hypixel.reports.ReportService;
import com.senither.hypixel.reports.UnfinishedPlayerReport;
import spark.Request;
import spark.Response;

import java.util.HashSet;
import java.util.Map;

public class GetReportRoute extends SparkRoute {

    public GetReportRoute(SkyblockAssistant app) {
        super(app);
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
            int finished = 0, unfinished = 0;
            for (Map.Entry<Report, HashSet<UnfinishedPlayerReport>> entry : ReportService.getPlayerQueue().entrySet()) {
                if (entry.getKey().getId().toString().equals(reportId)) {
                    unfinished = entry.getValue().size();
                    finished = entry.getKey().getPlayerReports().size();
                }
            }

            JsonObject state = new JsonObject();
            state.addProperty("completed", finished);
            state.addProperty("pending", unfinished);

            return buildResponse(response, 206, "Report is still being generated!", state);
        }

        JsonObject data = app.getHypixel().getGson().fromJson(
            report.getString("data"), JsonObject.class
        );

        if (data.has("guildEntry")) {
            data.getAsJsonObject("guildEntry").add("data", app.getHypixel().getGson().fromJson(
                data.getAsJsonObject("guildEntry").get("data").getAsString(), JsonObject.class
            ));
        }

        data.addProperty("created_at", report.getString("created_at"));
        data.addProperty("finished_at", report.getString("finished_at"));

        return buildDataResponse(response, 200, data);
    }
}
