/*
 * Copyright (c) 2015 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package no.asgari.civilization.server.application;

import com.google.common.cache.LoadingCache;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.model.GameType;

/**
 * Application singleton
 */
@SuppressWarnings("unchecked")
@Log4j
//This class is a @Singleton
public final class CivSingleton {

    private static CivSingleton civSingleton = new CivSingleton();
    //Key is playerId and value is username
    private LoadingCache<String, String> usernameCache;

    private LoadingCache<GameType, ItemReader> itemsCache;

    private CivSingleton() {
    }

    public static CivSingleton instance() {
        return civSingleton;
    }

    public void setPlayerCache(LoadingCache<String, String> usernameCache) {
        this.usernameCache = usernameCache;
    }

    public void setItemsCache(LoadingCache<GameType, ItemReader> itemsCache) {
        this.itemsCache = itemsCache;
    }

    /**
     * Key is playerId and value is username
     */
    public LoadingCache<String, String> playerCache() {
        return usernameCache;
    }

    /**
     * Key is a combination of GameType and SheetName
     *
     * @return
     */
    public LoadingCache<GameType, ItemReader> itemsCache() {
        return itemsCache;
    }
}
