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

package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.GameDTO;
import no.asgari.civilization.server.dto.GameLogDTO;
import no.asgari.civilization.server.dto.MessageDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.misc.Java8Util;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Log4j
public class GameAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Player, String> playerCollection;
    private final GameLogAction gameLogAction;
    private final JacksonDBCollection<Chat, String> chatCollection;

    public GameAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.chatCollection = JacksonDBCollection.wrap(db.getCollection(Chat.COL_NAME), Chat.class, String.class);
        this.gameLogAction = new GameLogAction(db);
    }

    public String createNewGame(CreateNewGameDTO dto, String playerId) {
        PBF pbf = new PBF();
        pbf.setName(dto.getName());
        pbf.setType(dto.getType());
        pbf.setNumOfPlayers(dto.getNumOfPlayers());
        ItemReader itemReader;
        try {
            itemReader = CivSingleton.instance().itemsCache().get(dto.getType());
        } catch (ExecutionException e) {
            log.error("Couldnt get itemReader from cache " + e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        pbf.getItems().addAll(itemReader.shuffledCivs);
        pbf.getItems().addAll(itemReader.shuffledCultureI);
        pbf.getItems().addAll(itemReader.shuffledCultureII);
        pbf.getItems().addAll(itemReader.shuffledCultureIII);
        pbf.getItems().addAll(itemReader.shuffledGPs);
        pbf.getItems().addAll(itemReader.shuffledHuts);
        pbf.getItems().addAll(itemReader.shuffledVillages);
        pbf.getItems().addAll(itemReader.shuffledTiles);
        pbf.getItems().addAll(itemReader.shuffledCityStates);
        pbf.getItems().addAll(itemReader.ancientWonders);
        pbf.getItems().addAll(itemReader.medievalWonders);
        pbf.getItems().addAll(itemReader.modernWonders);
        pbf.getItems().addAll(itemReader.mountedList);
        pbf.getItems().addAll(itemReader.aircraftList);
        pbf.getItems().addAll(itemReader.artilleryList);
        pbf.getItems().addAll(itemReader.infantryList);
        pbf.getTechs().addAll(itemReader.allTechs);

        WriteResult<PBF, String> pbfInsert = pbfCollection.insert(pbf);
        pbf.setId(pbfInsert.getSavedId());
        log.info("PBF game created with id " + pbfInsert.getSavedId());
        joinGame(pbf.getId(), playerId, Optional.of(dto.getColor()));
        return pbf.getId();
    }

    public List<PbfDTO> getAllActiveGames() {
        @Cleanup DBCursor<PBF> dbCursor = pbfCollection.find();
        return Java8Util.streamFromIterable(dbCursor)
                .map(GameAction::createPbfDTO)
                .sorted(new Comparator<PbfDTO>() {
                    @Override
                    public int compare(PbfDTO o1, PbfDTO o2) {
                        int v = Boolean.valueOf(o1.isActive()).compareTo(o2.isActive());
                        if(v != 0) return v;
                        return Long.valueOf(o1.getCreated()).compareTo(o2.getCreated());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Creating PbfDTO so to not include every players Ghand and information
     *
     * @param pbf - the PBF
     * @return PbfDto
     */
    private static PbfDTO createPbfDTO(PBF pbf) {
        PbfDTO dto = new PbfDTO();
        dto.setType(pbf.getType());
        dto.setId(pbf.getId());
        dto.setName(pbf.getName());
        dto.setActive(pbf.isActive());
        long created = pbf.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        dto.setCreated(created);
        dto.setNumOfPlayers(pbf.getNumOfPlayers());
        dto.setPlayers(pbf.getPlayers().stream()
                .map(p -> createPlayerDTO(p, pbf.getId()))
                .collect(Collectors.toList()));
        dto.setNameOfUsersTurn(pbf.getNameOfUsersTurn());
        return dto;
    }

    private static PlayerDTO createPlayerDTO(Playerhand player, String pbfId) {
        PlayerDTO dto = new PlayerDTO();
        dto.setUsername(player.getUsername());
        dto.setPlayerId(player.getPlayerId());
        dto.setPbfId(pbfId);
        return dto;
    }

    private static Playerhand createPlayerHand(Player player, String color) {
        Playerhand playerhand = new Playerhand();
        playerhand.setUsername(player.getUsername());
        playerhand.setPlayerId(player.getId());
        playerhand.setYourTurn(false);
        playerhand.setColor(color);
        return playerhand;
    }

    /**
     * Joins a game. If it is full it will throw exception
     */
    public void joinGame(String pbfId, String playerId, Optional<String> colorOpt) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        if (pbf.getNumOfPlayers() == pbf.getPlayers().size()) {
            log.warn("Cannot join the game. Its full");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageDTO("Cannot join the game. Its full!"))
                    .build();
            throw new WebApplicationException(badReq);
        }

        Player player = playerCollection.findOneById(playerId);

        boolean playerAlreadyJoined = pbf.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(player.getId()));
        if (playerAlreadyJoined) {
            log.warn("Cannot join the game. Player has already joined it");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageDTO("Cannot join the game. You have already joined!"))
                    .build();
            throw new WebApplicationException(badReq);
        }

        player.getGameIds().add(pbfId);
        playerCollection.updateById(player.getId(), player);

        String color = colorOpt.orElse(chooseColorForPlayer(pbf));
        Playerhand playerhand = createPlayerHand(player, color);
        if (!pbf.getPlayers().contains(playerhand)) {
            createInfoLog(pbf.getId(), playerhand.getUsername() + " joined the game and is playing color " + playerhand.getColor());
            pbf.getPlayers().add(playerhand);
        }
        pbf = startIfAllPlayers(pbf);
        pbfCollection.updateById(pbf.getId(), pbf);
    }

    private String chooseColorForPlayer(PBF pbf) {
        if (pbf.getPlayers().isEmpty()) {
            return Playerhand.green();
        }

        Set<String> allColors = Sets.newHashSet(Playerhand.green(), Playerhand.purple(), Playerhand.blue(), Playerhand.yellow(), Playerhand.red());

        Set<String> colors = pbf.getPlayers().stream()
                .map(Playerhand::getColor)
                .collect(Collectors.toSet());

        Sets.SetView<String> difference = Sets.difference(allColors, colors);
        return difference.iterator().next();
    }

    /**
     * Returns the username of the starting turn player
     *
     * @param pbfId
     * @return
     */
    public String getTurnPlayer(String pbfId) {
        PBF pbf = findPBFById(pbfId);
        return pbf.getPlayers().stream()
                .filter(Playerhand::isYourTurn)
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindPlayer)
                .getUsername();

    }

    public List<PlayerDTO> getAllPlayers(String pbfId) {
        Preconditions.checkNotNull(pbfId);
        PBF pbf = pbfCollection.findOneById(pbfId);
        return pbf.getPlayers().stream()
                .map(p -> createPlayerDTO(p, pbf.getId()))
                .collect(Collectors.toList());
    }

    private PBF startIfAllPlayers(PBF pbf) {
        final int numOfPlayersNeeded = pbf.getNumOfPlayers();
        if (numOfPlayersNeeded == pbf.getPlayers().size()) {
            Playerhand randomPlayer = getRandomPlayer(pbf.getPlayers());
            log.debug("Setting starting player " + randomPlayer);
            randomPlayer.setYourTurn(true);
            createInfoLog(pbf.getId(), "Starting player is " + randomPlayer.getUsername());
        }
        return pbf;
    }

    private Playerhand getRandomPlayer(List<Playerhand> players) {
        Collections.shuffle(players);
        return players.get(0);
    }

    public GameDTO getGame(PBF pbf, Player player) {
        Preconditions.checkNotNull(pbf);
        //Set common stuff
        GameDTO dto = new GameDTO();
        long created = pbf.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        dto.setCreated(created);
        dto.setId(pbf.getId());
        dto.setType(pbf.getType());
        dto.setName(pbf.getName());
        dto.setWhosTurnIsIt(pbf.getNameOfUsersTurn());

        //Set logs
        List<GameLog> allPublicLogs = gameLogAction.getGameLogs(pbf.getId());
        List<GameLogDTO> publicGamelogDTOs = allPublicLogs.stream()
                .filter(log -> !Strings.isNullOrEmpty(log.getPublicLog()))
                .map(log -> new GameLogDTO(log.getId(), log.getPublicLog(), log.getCreatedInMillis(), log.getDraw()))
                .collect(Collectors.toList());
        dto.setPublicLogs(publicGamelogDTOs);

        //Set private player info if correct player is loggedIn.
        if (player != null && !Strings.isNullOrEmpty(player.getUsername()) && !Strings.isNullOrEmpty(player.getId())) {
            //Get all the private player stuff
            Optional<Playerhand> playerhand = pbf.getPlayers()
                    .stream()
                    .filter(p -> p.getPlayerId().equals(player.getId()))
                    .findFirst();

            if (playerhand.isPresent()) {
                List<GameLog> allPrivateLogs = gameLogAction.getGameLogsBelongingToPlayer(pbf.getId(), playerhand.get().getUsername());
                List<GameLogDTO> privateGamelogDTOs = allPrivateLogs.stream()
                        .filter(log -> !Strings.isNullOrEmpty(log.getPrivateLog()))
                        .map(log -> new GameLogDTO(log.getId(), log.getPrivateLog(), log.getCreatedInMillis(), log.getDraw()))
                        .collect(Collectors.toList());

                dto.setPlayer(playerhand.get());
                dto.setPrivateLogs(privateGamelogDTOs);
            }
        }
        return dto;
    }

    public boolean withdrawFromGame(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        if (pbf.getNumOfPlayers() == pbf.getPlayers().size()) {
            return false;
        }

        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.warn("User with id " + playerId + " is not player of this game, and cannot withdraw");
            Response badReq = Response.status(Response.Status.FORBIDDEN)
                    .entity(new MessageDTO("User is not player of this game, and cannot withdraw"))
                    .build();
            throw new WebApplicationException(badReq);
        }

        Iterator<Playerhand> iterator = pbf.getPlayers().iterator();
        while (iterator.hasNext()) {
            Playerhand playerhand = iterator.next();
            if (playerhand.getPlayerId().equals(playerId)) {
                iterator.remove();
                gameLogAction.createCommonPublicLog("Withdrew from game", pbfId, playerId);
                pbfCollection.updateById(pbf.getId(), pbf);
                return true;
            }
        }

        return false;
    }

    public Chat chat(String pbfId, String message, String username) {
        Chat chat = new Chat();
        chat.setPbfId(pbfId);
        chat.setMessage(message);
        chat.setUsername(username);
        String id = chatCollection.insert(chat).getSavedId();
        chat.setId(id);
        return chat;
    }

    public List<Chat> getChat(String pbfId) {
        Preconditions.checkNotNull(pbfId);
        List<Chat> chats = chatCollection.find(DBQuery.is("pbfId", pbfId)).sort(DBSort.desc("created")).toArray();
        return chats;
    }
}
