package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j
public class ImageAction extends BaseAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public ImageAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public String saveImage(String pbfId, String playerId, InputStream imageStream) {

        //I will allow upload of image
        //Create temp image and store in mongodb
        //put the name of file as metadata, and date and who uploaded which will show the latest image
        //Store the metadata in the pbf collection
        //use the collection to view the data



        //Path outputPath = FileSystems.getDefault().getPath(<upload-folder-on-server>, fileName);
        //Files.copy(imageStream, outputPath);


        return null;
    }

}
