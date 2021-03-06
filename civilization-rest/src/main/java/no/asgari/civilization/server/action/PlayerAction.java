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
import com.mongodb.DB;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.Civ;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Tradable;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j
public class PlayerAction extends BaseAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;

    public PlayerAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    /**
     * Returns a set of all the game ids of player
     */
    public Set<String> getGames(Player player) {
        Preconditions.checkNotNull(player);
        log.debug("Getting all games for player " + player.getUsername());
        return player.getGameIds();
    }

    /**
     * Choose a tech for player and store back in the pbf collection
     *
     * @param pbfId    - The pbf id
     * @param techName - The tech
     * @param playerId - The id of player
     */
    public GameLog chooseTech(String pbfId, String techName, String playerId) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(techName);

        PBF pbf = pbfCollection.findOneById(pbfId);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        Optional<Tech> tech = pbf.getTechs().stream()
                .filter(techToFind -> techToFind.getName().equals(techName))
                .findFirst();
        //if not static then this::cannotFindItem
        Tech chosenTech = tech.orElseThrow(PlayerAction::cannotFindItem);
        chosenTech.setHidden(true);
        chosenTech.setOwnerId(playerId);

        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (playerhand.getTechsChosen().contains(chosenTech)) {
            log.warn("Player with id " + playerId + " tried to add same tech as they had");
            return null;
        }
        playerhand.getTechsChosen().add(chosenTech);

        pbfCollection.updateById(pbf.getId(), pbf);
        log.debug("Player " + playerId + " chose tech " + chosenTech.getName());

        return super.createLog(chosenTech, pbfId, GameLog.LogType.TECH);
    }

    public boolean removeTech(String pbfId, String techName, String playerId) {
        Preconditions.checkNotNull(techName);
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        Tech techToRemove = playerhand.getTechsChosen().stream()
                .filter(tech -> tech.getName().equals(techName))
                .findFirst().orElseThrow(PlayerAction::cannotFindItem);
        boolean removed = playerhand.getTechsChosen().remove(techToRemove);
        if (!removed) {
            log.error("Could not remove tech " + techName + " from player with id " + playerId + " in pbf " + pbf.getName());
            return false;
        }

        pbfCollection.updateById(pbf.getId(), pbf);

        //No point in creating game log, the techs are for your own information
        log.debug("Removed tech " + techName + " from player with id " + playerId + " in pbf " + pbf.getName());
        return true;
    }

    public boolean endTurn(String pbfId, String username) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(username);

        PBF pbf = pbfCollection.findOneById(pbfId);

        //Loop through the list and find next starting player
        for (int i = 0; i < pbf.getPlayers().size(); i++) {
            Playerhand playerhand = pbf.getPlayers().get(i);
            if (playerhand.getUsername().equals(username)) {
                playerhand.setYourTurn(false);

                //Choose next player in line to be starting player
                if (pbf.getPlayers().size() == (i + 1)) {
                    //We are at the end, pick the first player
                    pbf.getPlayers().get(0).setYourTurn(true);
                } else {
                    pbf.getPlayers().get(i + 1).setYourTurn(true);
                }

                try {
                    pbfCollection.updateById(pbf.getId(), pbf);
                    return true;
                } catch (Exception ex) {
                    log.error("Couldn't update pbf " + ex.getMessage(), ex);
                    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .build());
                }
            }
        }
        return false;
    }

    /**
     * Revealing of items are really just saving a public log with the hidden content information
     *
     * @param pbfId
     * @param playerId
     * @param itemDTO  - The item to reveal
     */
    @SuppressWarnings("unchecked")
    public void revealItem(String pbfId, String playerId, ItemDTO itemDTO) {
        Preconditions.checkNotNull(itemDTO);
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);

        //Check if item can be found on the player
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindPlayer);

        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        List<Item> items = playerhand.getItems();

        Optional<SheetName> sheetName = SheetName.find(itemDTO.getSheetName());
        if (!sheetName.isPresent()) {
            log.error("Cannot find Sheetname " + itemDTO.getSheetName());
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Entity.json("{\"msg\": \"Cannot find Sheetname " + itemDTO.getSheetName() + "\"}"))
                    .build());
        }

        Optional<Item> itemToRevealOptional = items.stream()
                .filter(it -> it.getName().equals(itemDTO.getName()))
                .filter(it -> it.getSheetName() == sheetName.get())
                .filter(Item::isHidden)
                .findAny();

        if (!itemToRevealOptional.isPresent()) {
            log.warn("Item " + itemDTO.getName() + " already revealed");
            throw new WebApplicationException(Response.status(Response.Status.NOT_MODIFIED)
                    .entity(Entity.json("{\"msg\": \"Item already revealed\"}"))
                    .build());
        }

        boolean isCiv = isCivilization(playerhand, sheetName);

        Item itemToReveal = itemToRevealOptional.get();
        itemToReveal.setHidden(false);
        if (isCiv) {
            Civ civ = (Civ) itemToReveal;
            playerhand.setCivilization(civ);
            Tech startingTech = civ.getStartingTech();
            startingTech.setHidden(false);
            startingTech.setOwnerId(playerId);
            playerhand.getTechsChosen().add(startingTech);
        }

        pbfCollection.updateById(pbf.getId(), pbf);

        //Create a new log entry
        logAction.createGameLog(itemToReveal, pbf.getId(), GameLog.LogType.REVEAL);
        log.debug("item to be reveal " + itemToReveal);
    }

    private boolean isCivilization(Playerhand playerhand, Optional<SheetName> sheetName) {
        if (sheetName.get() == SheetName.CIV) {
            if (playerhand.getCivilization() != null) {
                log.warn("Cannot choose civilization again");
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(Entity.json("{\"msg\": \"Civilization already chosen\"}"))
                        .build());
            }
            return true;
        }
        return false;
    }

    /**
     * Revealing of techs are really just saving a public log with the hidden content information
     *
     * @param gameLog
     * @param pbfId
     * @param playerId
     */
    public void revealTech(GameLog gameLog, String pbfId, String playerId) {
        Preconditions.checkNotNull(gameLog);
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);

        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        Draw<Tech> draw = gameLog.getDraw();
        if (draw == null || draw.getItem() == null || (draw.getItem() instanceof Tech == false)) {
            log.error("Couldn't find tech to reveal");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Item item = draw.getItem();
        item.setHidden(false);

        gameLogCollection.updateById(gameLog.getId(), gameLog);

        createLog(item, pbf.getId(), GameLog.LogType.REVEAL, playerId);
    }

    /**
     * Returns the remaining techs the player can choose from
     *
     * @param playerId - The player
     * @param pbfId    - The PBF
     * @return
     */
    public List<Tech> getRemaingTechsForPlayer(String playerId, String pbfId) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        Optional<Playerhand> playerhandOptional = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst();

        Set<Tech> techsChosen = playerhandOptional.orElseThrow(PlayerAction::cannotFindPlayer)
                .getTechsChosen();

        Playerhand playerhand = playerhandOptional.get();

        if (playerhand.getCivilization() != null && playerhand.getCivilization().getStartingTech() != null) {
            techsChosen.add(playerhandOptional.get().getCivilization().getStartingTech());
        }
        pbf.getTechs().removeAll(techsChosen);
        return pbf.getTechs();
    }

    /**
     * Method that's checks whether it is players turn.
     * Not the same as #checkYourTurn()
     *
     * @param pbfId    - PBF id
     * @param playerId - Player id
     * @return - true if it is players turn
     * @see #checkYourTurn(String, String)
     */
    public boolean isYourTurn(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        return playerhand.isYourTurn();
    }

    /**
     * Will send the item to the new owner
     *
     * @param item
     * @param playerId
     * @return
     */
    public boolean tradeToPlayer(ItemDTO item, String playerId) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.getPbfId());
        Preconditions.checkNotNull(item.getOwnerId());

        PBF pbf = pbfCollection.findOneById(item.getPbfId());
        Playerhand fromPlayer = getPlayerhandByPlayerId(playerId, pbf);
        Playerhand toPlayer = getPlayerhandByPlayerId(item.getOwnerId(), pbf);
        Optional<SheetName> dtoSheet = SheetName.find(item.getSheetName());
        if (!dtoSheet.isPresent()) {
            log.error("Couldn't find sheetname " + item.getSheetName());
            throw cannotFindItem();
        }

        Item itemToTrade = fromPlayer.getItems().stream()
                .filter(it -> it instanceof Tradable)
                .filter(it -> it.getSheetName() == dtoSheet.get())
                .filter(it -> it.getName().equals(item.getName()))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem);

        boolean remove = fromPlayer.getItems().remove(itemToTrade);
        if (!remove) {
            log.error("Didn't find item from playerhand: " + item);
            return false;
        }
        toPlayer.getItems().add(itemToTrade);

        itemToTrade.setOwnerId(toPlayer.getPlayerId());
        pbfCollection.updateById(pbf.getId(), pbf);
        logAction.createTradeGameLog(itemToTrade, pbf.getId(), GameLog.LogType.TRADE, fromPlayer.getUsername());
        return true;
    }

    public void discardItem(String pbfId, String playerId, ItemDTO itemdto) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        Optional<SheetName> dtoSheet = SheetName.find(itemdto.getSheetName());
        if (!dtoSheet.isPresent()) {
            log.error("Couldn't find sheetname " + itemdto.getSheetName());
            throw cannotFindItem();
        }

        //Find the item, then delete it
        Optional<Item> itemToDeleteOptional = playerhand.getItems().parallelStream()
                .filter(item -> item.getSheetName() == dtoSheet.get() && item.getName().equals(itemdto.getName()))
                .findAny();

        if (!itemToDeleteOptional.isPresent()) throw cannotFindItem();

        Item itemToDelete = itemToDeleteOptional.get();
        itemToDelete.setHidden(true);
        itemToDelete.setOwnerId(null);

        boolean removed = playerhand.getItems().remove(itemToDeleteOptional.get());
        if (removed) {
            pbf.getDiscardedItems().add(itemToDeleteOptional.get());
            createLog(itemToDelete, pbf.getId(), GameLog.LogType.DISCARD, playerId);
            pbfCollection.updateById(pbf.getId(), pbf);
            return;
        }
        log.error("Found the item " + itemToDelete + " , but couldn't delete it for some reason");
        throw cannotFindItem();
    }

    public Player getPlayerById(String playerId) {
        return playerCollection.findOneById(playerId);
    }

    public Set<Tech> getPlayersTechs(String pbfId, String playerId) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        return pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId))
                .findFirst().orElseThrow(PlayerAction::cannotFindPlayer)
                .getTechsChosen();

    }

    /**
     * Returns the id to the player created
     *
     * @return the id of the newly created player
     * @throws PlayerExistException - Throws this exception if username already exists
     */
    @SneakyThrows
    public String createPlayer(String username, String passwordEncoded, String email) throws PlayerExistException {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(passwordEncoded);
        Preconditions.checkNotNull(email);

        if (CivSingleton.instance().playerCache().asMap().containsValue(username)) {
            throw new PlayerExistException();
        }

        Player player = new Player();
        player.setUsername(username);
        String decodedPassword = new String(Base64.getDecoder().decode(passwordEncoded), "UTF-8");
        log.debug("Password decoded as " + decodedPassword);

        player.setPassword(DigestUtils.sha1Hex(decodedPassword));
        player.setEmail(email);
        WriteResult<Player, String> insert = playerCollection.insert(player);
        log.info(String.format("Saving player with id %s", insert.getSavedId()));
        return insert.getSavedId();
    }
}
