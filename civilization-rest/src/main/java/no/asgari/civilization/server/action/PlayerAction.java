package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.BasicDBObject;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j
public class PlayerAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public PlayerAction(JacksonDBCollection<Player, String> playerCollection, JacksonDBCollection<PBF, String> pbfCollection) {
        this.playerCollection = playerCollection;
        this.pbfCollection = pbfCollection;
    }

    /**
     * Returns the id to the player created
     * @param playerDTO - The DTO object
     * @return the id of the newly created player
     * @throws PlayerExistException - Throws this exception if username already exists
     */
    public String createPlayer(PlayerDTO playerDTO) throws PlayerExistException {
        Preconditions.checkNotNull(playerDTO);
        Preconditions.checkNotNull(playerDTO.getUsername());
        Preconditions.checkNotNull(playerDTO.getEmail());
        Preconditions.checkNotNull(playerDTO.getPassword());
        Preconditions.checkNotNull(playerDTO.getPasswordCopy());

        if(!playerDTO.getPassword().equals(playerDTO.getPasswordCopy())) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                            .entity("Passwords are not identical")
                                            .build());
        }
        if(findPlayerByUsername(playerDTO.getUsername()).isPresent()) {
            throw new PlayerExistException();
        }

        Player player = new Player();
        player.setUsername(playerDTO.getUsername());
        player.setPassword(DigestUtils.sha1Hex(playerDTO.getPassword()));
        player.setEmail(playerDTO.getEmail());
        WriteResult<Player, String> insert = playerCollection.insert(player);

        log.info(String.format("Saving player with id %s", insert.getSavedId()));

        return insert.getSavedId();
    }

    public Optional<Player> findPlayerByUsername(String username) {
        @Cleanup DBCursor<Player> player = playerCollection.find(
                DBQuery.is("username", username),
                new BasicDBObject());

        if(player == null || !player.hasNext()) {
            return Optional.empty();
        }

        return Optional.of(player.next());
    }

    public List<PBF> getGames(Player player) {
        Preconditions.checkNotNull(player);
        log.debug("Getting all games for player " + player.getUsername());

        return player.getGameIds()
                .stream()
                .map(this::getPBF)
                .filter(PBF::isActive)
                .collect(Collectors.toList());

    }

    private PBF getPBF(String pbfId) {
        return pbfCollection.findOneById(pbfId);
    }
}
