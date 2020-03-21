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

package com.senither.hypixel.metrics;

import java.util.EnumMap;

public class Metrics {

    private static final EnumMap<MetricType, Long> metric = new EnumMap<>(MetricType.class);

    static {
        for (MetricType type : MetricType.values()) {
            metric.put(type, 0L);
        }
    }

    public synchronized static void increment(MetricType type) {
        metric.put(type, metric.get(type) + 1);
    }

    public synchronized static void decrement(MetricType type) {
        metric.put(type, metric.get(type) - 1);
    }

    public synchronized static long getValue(MetricType type) {
        return metric.get(type);
    }
}
