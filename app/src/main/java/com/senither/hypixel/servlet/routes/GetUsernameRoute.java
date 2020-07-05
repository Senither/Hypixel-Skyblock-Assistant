package com.senither.hypixel.servlet.routes;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.servlet.SparkRoute;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class GetUsernameRoute extends SparkRoute {

    public GetUsernameRoute(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        HashSet<String> stringifiedUuids = getUuidsFromRequest(request);
        if (stringifiedUuids.isEmpty()) {
            return buildResponse(response, 400, "Missing uuid or uuids query parameters for the uuids that should be resolved.");
        }

        StringBuilder stringifiedParams = new StringBuilder();
        for (String ignored : stringifiedUuids) {
            stringifiedParams.append("?, ");
        }

        Collection usernamesCollection = app.getDatabaseManager().query(String.format(
            "SELECT `uuid`, `username` FROM `uuids` WHERE `uuid` IN (%s)",
            stringifiedParams.toString().substring(0, stringifiedParams.length() - 2)
        ), stringifiedUuids.toArray());

        JsonObject jsonObject = new JsonObject();
        for (String uuidString : stringifiedUuids) {
            try {
                UUID uuid = UUID.fromString(uuidString);

                List<DataRow> currentUuid = usernamesCollection.where("uuid", uuid.toString());
                if (currentUuid.isEmpty()) {
                    jsonObject.add(uuid.toString(), null);
                    continue;
                }
                jsonObject.addProperty(uuid.toString(), currentUuid.get(0).getString("username"));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return buildDataResponse(response, 200, jsonObject);
    }

    public HashSet<String> getUuidsFromRequest(Request request) {
        HashSet<String> uuids = new HashSet<>();

        if (request.queryParams("uuid") != null) {
            uuids.add(request.queryParams("uuid"));
        }

        if (request.queryParams("uuids") != null) {
            uuids.addAll(Arrays.asList(request.queryParams("uuids").split(",")));
        }

        return uuids;
    }
}
