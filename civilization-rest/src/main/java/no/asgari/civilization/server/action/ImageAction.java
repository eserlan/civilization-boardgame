package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Log4j
public class ImageAction extends BaseAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public ImageAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public String saveImage(String pbfId, String playerId, InputStream imageStream, String filename) throws IOException {

        //I will allow upload of image
        //Create temp image and store in mongodb
        //put the name of file as metadata, and date and who uploaded which will show the latest image
        //Store the metadata in the pbf collection
        //use the collection to view the data



        //Path outputPath = FileSystems.getDefault().getPath(<upload-folder-on-server>, fileName);
        //Files.copy(imageStream, outputPath);


        String imageMimeType = URLConnection.guessContentTypeFromStream(imageStream);

        Optional<String> imageTypeOptional = guessImageType(imageMimeType);
        Path outputPath = Files.createTempDirectory(pbfId);
        Path tempFile = Files.createTempFile(outputPath, pbfId, imageTypeOptional.orElse("jpeg"));
        Files.copy(imageStream, tempFile);

        return null;
    }

    public Optional<String> guessImageType(@NonNull String mimetype) {
        if(mimetype.matches(".*jpeg.*")) return Optional.of("jpg");
        else if(mimetype.matches(".*png.*")) return Optional.of("png");
        else if(mimetype.matches(".*gif.*")) return Optional.of("gif");

        return Optional.empty();
    }

}
