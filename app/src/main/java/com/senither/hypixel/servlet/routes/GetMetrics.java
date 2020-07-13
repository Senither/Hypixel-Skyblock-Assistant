package com.senither.hypixel.servlet.routes;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.servlet.SparkRoute;
import com.senither.hypixel.metrics.PrometheusMetricsServlet;
import spark.Request;
import spark.Response;

public class GetMetrics extends SparkRoute {

    private static final PrometheusMetricsServlet metricsServlet = new PrometheusMetricsServlet();

    public GetMetrics(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return metricsServlet.servletGet(request.raw(), response.raw());
    }
}
