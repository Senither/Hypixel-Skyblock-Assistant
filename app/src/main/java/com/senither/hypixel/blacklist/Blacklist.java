/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.senither.hypixel.blacklist;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("SuspiciousMethodCalls")
public class Blacklist {

    private static final Logger log = LoggerFactory.getLogger(Blacklist.class);

    private final SkyblockAssistant app;
    private final List<BlacklistEntity> blacklist;
    private final Ratelimit ratelimit;

    /**
     * Creates a new blacklist instance.
     *
     * @param app The main Skyblock Assistant instance.
     */
    public Blacklist(SkyblockAssistant app) {
        this.app = app;

        this.blacklist = new BlacklistList();
        this.ratelimit = new Ratelimit(this);
    }

    /**
     * Gets the ratelimiter instance for the current blacklist.
     *
     * @return The ratelimiter instance for the current blacklist.
     */
    public Ratelimit getRatelimit() {
        return ratelimit;
    }

    /**
     * Checks if the given user is on the blacklist.
     *
     * @param user The user that should be checked.
     * @return <code>True</code> if the user is on the blacklist, <code>False</code> otherwise.
     */
    public boolean isBlacklisted(@Nonnull User user) {
        BlacklistEntity entity = getEntity(user.getIdLong());
        return entity != null && entity.isBlacklisted();
    }

    /**
     * Adds the given user to the blacklist with the given reason, the blacklist
     * record will last until it is {@link #remove(long) removed}.
     *
     * @param user   The user that should be added to the blacklist.
     * @param reason The reason for the user being added to the blacklist.
     */
    public void addUser(@Nonnull User user, @Nullable String reason) {
        addIdToBlacklist(user.getIdLong(), reason);
    }

    /**
     * Removes the blacklist record with the given ID.
     *
     * @param id The ID to remove from teh blacklist.
     */
    public void remove(long id) {
        if (!blacklist.contains(id)) {
            return;
        }

        Iterator<BlacklistEntity> iterator = blacklist.iterator();
        while (iterator.hasNext()) {
            BlacklistEntity next = iterator.next();

            if (next.getId() == id) {
                iterator.remove();
                break;
            }
        }

        try {
            app.getDatabaseManager().queryUpdate("DELETE FROM `blacklists` WHERE `id` = ?", id);
        } catch (SQLException e) {
            log.error("Failed to sync blacklist with the database: " + e.getMessage(), e);
        }
    }


    /**
     * Gets the blacklist entity for the given ID.
     *
     * @param id The ID to get the blacklist entity for.
     * @return Possible-null, the blacklist entity matching the given ID.
     */
    @Nullable
    public BlacklistEntity getEntity(long id) {
        for (BlacklistEntity entity : blacklist) {
            if (entity.getId() == id) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Adds the ID to the blacklist with the given reason.
     *
     * @param id     The ID that should be added to the blacklist.
     * @param reason The reason that the ID was added to the blacklist.
     */
    public void addIdToBlacklist(final long id, final @Nullable String reason) {
        addIdToBlacklist(id, reason, null);
    }

    /**
     * Adds the ID to the blacklist with the given reason, and expire time.
     *
     * @param id        The ID that should be added to the blacklist.
     * @param reason    The reason that the ID was added to the blacklist.
     * @param expiresIn The carbon time instance for when the entity should expire.
     */
    public void addIdToBlacklist(final long id, final @Nullable String reason, @Nullable Carbon expiresIn) {
        BlacklistEntity entity = getEntity(id);
        if (entity != null) {
            blacklist.remove(entity);
        }

        blacklist.add(new BlacklistEntity(id, reason, expiresIn));

        try {
            app.getDatabaseManager().queryUpdate("DELETE FROM `blacklists` WHERE `id` = ?", id);
            app.getDatabaseManager().queryInsert("INSERT INTO `blacklists` SET `id` = ?, `expires_in` = ?, `reason` = ?",
                id, expiresIn == null ? Carbon.now().addYears(10) : expiresIn, reason
            );
        } catch (SQLException e) {
            log.error("Failed to sync blacklist with the database: " + e.getMessage(), e);
        }
    }

    /**
     * Get the all the entities currently on the blacklist.
     *
     * @return The entities currently on the blacklist.
     */
    public List<BlacklistEntity> getBlacklistEntities() {
        return blacklist;
    }

    /**
     * Syncs the blacklist with the database.
     */
    public synchronized void syncBlacklistWithDatabase() {
        blacklist.clear();
        try {
            Collection collection = app.getDatabaseManager().query(
                "SELECT * FROM `blacklists` WHERE `expires_in` > ?",
                Carbon.now()
            );

            collection.forEach(row -> {
                String id = row.getString("id", null);
                if (id == null) {
                    return;
                }

                try {
                    long longId = Long.parseLong(id);

                    blacklist.add(new BlacklistEntity(
                        longId,
                        row.getString("reason"),
                        row.getTimestamp("expires_in")
                    ));
                } catch (NumberFormatException ignored) {
                    // This is ignored
                }
            });
        } catch (SQLException e) {
            log.error("Failed to sync blacklist with the database: " + e.getMessage(), e);
        }
    }

    private class BlacklistList extends ArrayList<BlacklistEntity> {

        @Override
        public boolean contains(Object o) {
            if (o instanceof Long) {
                long id = (long) o;
                for (BlacklistEntity entity : this) {
                    if (entity.getId() == id) {
                        return true;
                    }
                }
                return false;
            }

            return super.contains(o);
        }
    }
}
