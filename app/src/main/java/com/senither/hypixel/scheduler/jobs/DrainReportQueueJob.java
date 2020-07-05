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

package com.senither.hypixel.scheduler.jobs;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.scheduler.Job;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.reports.Report;
import com.senither.hypixel.reports.ReportService;
import com.senither.hypixel.reports.UnfinishedPlayerReport;
import com.senither.hypixel.time.Carbon;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DrainReportQueueJob extends Job {

    public DrainReportQueueJob(SkyblockAssistant app) {
        super(app, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            if (ReportService.getPlayerQueue().isEmpty()) {
                return;
            }

            Map.Entry<Report, HashSet<UnfinishedPlayerReport>> entry = ReportService.getPlayerQueue()
                .entrySet()
                .iterator()
                .next();

            Iterator<UnfinishedPlayerReport> iterator = entry.getValue().iterator();

            if (!iterator.hasNext()) {
                Report report = entry.getKey();

                app.getDatabaseManager().queryUpdate("UPDATE `reports` SET `data` = ?, `finished_at` = ? WHERE `id` = ?",
                    app.getHypixel().getGson().toJson(report),
                    Carbon.now(),
                    report.getId()
                );

                ReportService.getPlayerQueue().remove(entry.getKey());
                return;
            }

            while (iterator.hasNext()) {
                boolean wasLoadedFromCache = loadPlayerForReport(entry.getKey(), iterator.next());

                iterator.remove();

                if (!wasLoadedFromCache) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean loadPlayerForReport(Report report, UnfinishedPlayerReport unfinishedPlayerReport) throws Exception {
        unfinishedPlayerReport.setUsername(
            app.getHypixel().getUsernameFromUuid(unfinishedPlayerReport.getUuid())
        );

        SkyBlockProfileReply profileReply = null;
        try {
            profileReply = app.getHypixel().getSelectedSkyBlockProfileFromUsername(unfinishedPlayerReport.getUsername())
                .get(5, TimeUnit.SECONDS);

            String stringifiedPlayerUUID = unfinishedPlayerReport.getUuid().toString().replace("-", "");
            profileReply.getProfile().get("members").getAsJsonObject().keySet()
                .removeIf(memberUUID -> !memberUUID.equals(stringifiedPlayerUUID));

            report.createPlayerReport(unfinishedPlayerReport, profileReply);

            return profileReply.getProfile().has("isFromCache")
                && profileReply.getProfile().get("isFromCache").getAsBoolean();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FriendlyException) {
                // This should only be thrown if the user has no SkyBlock profiles, if
                // they don't have a profile we just ignore the user and skips them.
                return true;
            }
            throw e;
        }
    }
}
