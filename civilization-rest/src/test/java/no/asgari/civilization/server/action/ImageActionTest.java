package no.asgari.civilization.server.action;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.ObjectId;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

public class ImageActionTest extends AbstractMongoDBTest {

    private InputStream in;
    private URL url;

    @Before
    public void checkThatFileExist() throws Exception {
        in = this.getClass().getClassLoader().getResourceAsStream("image.png");
        url = this.getClass().getClassLoader().getResource("image.png");

        assertNotNull(in);
        assertNotNull(url);
    }

    @Test
    public void saveImageThenReadIt() throws Exception {
        File file = new File(url.toURI().getPath());
        assertNotNull(file);


        Path path = Paths.get(url.toURI());
        assertNotNull(path);
        System.out.println(path);

        ImageAction imageAction = new ImageAction(db);

        //I will allow upload of image
        //Create temp image and store in mongodb
        //put the name of file as metadata, and date and who uploaded which will show the latest image
        //Store the metadata in the pbf collection
        //use the collection to view the data

        GridFS gfsImage = new GridFS(db, "mapTest");
        GridFSInputFile gfsFile = gfsImage.createFile(in, "image");
        gfsFile.save();
        GridFSDBFile imageForOutput = gfsImage.findOne("image");
        System.out.println(imageForOutput);

        org.bson.types.ObjectId id = (org.bson.types.ObjectId) gfsFile.getId();
        //gfsImage.remove(id);
        gfsImage.remove("image");
    }
}
