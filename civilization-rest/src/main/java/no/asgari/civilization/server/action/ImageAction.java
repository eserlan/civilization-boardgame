package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

@Log4j
public class ImageAction extends BaseAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public ImageAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public void saveImage(String pbfId, String playerId, byte[] imageByte) {

        //I will allow upload of image
        //Create temp image and store in mongodb
        //put the name of file as metadata, and date and who uploaded which will show the latest image
        //Store the metadata in the pbf collection
        //use the collection to view the data
    }

}
