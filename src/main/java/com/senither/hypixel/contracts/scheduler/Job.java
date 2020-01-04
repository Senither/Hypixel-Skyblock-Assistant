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

package com.senither.hypixel.contracts.scheduler;

import com.senither.hypixel.SkyblockAssistant;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class Job extends TimerTask {

    protected final SkyblockAssistant app;

    private final long delay;
    private final long period;
    private final TimeUnit unit;

    public Job(SkyblockAssistant app) {
        this(app, 0);
    }

    public Job(SkyblockAssistant app, long delay) {
        this(app, delay, 1);
    }

    public Job(SkyblockAssistant app, long delay, long period) {
        this(app, delay, period, TimeUnit.MINUTES);
    }

    public Job(SkyblockAssistant app, long delay, long period, TimeUnit unit) {
        this.app = app;

        this.delay = delay;
        this.period = period;
        this.unit = unit;
    }

    public long getDelay() {
        return delay;
    }

    public long getPeriod() {
        return period;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
