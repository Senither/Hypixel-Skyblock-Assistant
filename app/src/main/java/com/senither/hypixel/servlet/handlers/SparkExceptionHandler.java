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

package com.senither.hypixel.servlet.handlers;

import com.google.gson.JsonObject;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.servlet.WebServlet;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

public class SparkExceptionHandler implements ExceptionHandler<Exception> {

    @Override
    public void handle(Exception exception, Request request, Response response) {
        response.status(500);

        if (exception instanceof FriendlyException || (exception instanceof ExecutionException && exception.getCause() instanceof FriendlyException)) {
            JsonObject root = new JsonObject();

            root.addProperty("status", 400);
            root.addProperty("reason", exception instanceof ExecutionException
                ? exception.getCause().getMessage() : exception.getMessage()
            );

            response.header("Access-Control-Allow-Origin", "*");
            response.type("application/json");

            response.body(root.toString());
            return;
        }

        WebServlet.log.error(request.requestMethod() + " " + request.pathInfo(), exception);

        try (StringWriter writer = new StringWriter()) {
            try (PrintWriter printer = new PrintWriter(writer)) {
                exception.printStackTrace(printer);

                response.body(writer.toString());
                response.type("text/plain");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
