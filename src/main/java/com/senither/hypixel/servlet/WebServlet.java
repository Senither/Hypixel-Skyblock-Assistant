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

package com.senither.hypixel.servlet;

import com.senither.hypixel.contracts.servlet.Route;
import com.senither.hypixel.servlet.handlers.NotFoundRouteHandler;
import com.senither.hypixel.servlet.handlers.SparkExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

public class WebServlet {

    public static final Logger log = LoggerFactory.getLogger(WebServlet.class);

    private final int port;
    private boolean initialized;

    public WebServlet(int port) {
        this.port = port;
        this.initialized = false;
    }

    private void initialize() {
        log.info("Igniting Spark API on port: " + port);

        Spark.port(port);

        Spark.notFound(new NotFoundRouteHandler());
        Spark.exception(Exception.class, new SparkExceptionHandler());

        initialized = true;
    }

    public synchronized void registerGet(final String path, final Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("GET {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.get(path, route, new VelocityTemplateEngine());
    }
}
