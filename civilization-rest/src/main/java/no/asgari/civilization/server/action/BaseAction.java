package no.asgari.civilization.server.action;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;
import org.mongojack.JacksonDBCollection;

@Log4j
public abstract class BaseAction {
    protected final GameLogAction logAction;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    protected BaseAction(DB db) {
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.logAction = new GameLogAction(db);
    }

    /** Creates public and private logs of draws **/
    protected GameLog createLog(Draw<? extends Spreadsheet> draw) {
        return logAction.createGameLog(draw);
    }

    protected GameLog createLog(Tech chosenTech, String pbfId) {
        return logAction.createGameLog(chosenTech, pbfId);
    }

    protected PBF findPBFById(String pbfId) {
        try {
            return pbfCollection.findOneById(pbfId);
        } catch(Exception ex) {
            log.error("Couldn't find pbf");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot find pbf")
                    .build();
            throw new WebApplicationException(badReq);
        }
    }


}
