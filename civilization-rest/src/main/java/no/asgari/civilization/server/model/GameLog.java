package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * This class will be used to store all the actions performed in the game.
 * It can be creating game, joining game, drawing items
 *
 * All private logs/draws can be requested undo
 *
 * Private view:
 * 14.04.2014 - 14:24 - cash1981 drew Civ Japan - <hidden button> - <undo button>
 * 14.04.2014 - 14:25 - cash1981 drew Civ Greece - <hidden button> - <undo button>
 *
 * Public view:
 * <14.04.2014 - 14:25 - cash1981 drew Infantry
 * <14.04.2014 - 14:25 - cash1981 drew Mounted
 * <14.04.2014 - 14:25 - cash1981 drew Artillery
 *
 */
@JsonRootName("gameLog")
@NoArgsConstructor
@ToString(of="log")
@Data
public class GameLog {
    public static final String COL_NAME = "gameLog";

    public enum LogType {
        TRADE, BATTLE, ITEM, TECH;
    }

    @Id
    @ObjectId
    private String id;

    @NotEmpty
    private String privateLog;

    @NotEmpty
    private String publicLog;

    /** Each log belongs to a pbf **/
    @NotEmpty
    private String pbfId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @NotEmpty
    private String username;

    /** Each log is from a corresponding draw **/
    @NotNull
    private Draw draw;

    @JsonIgnore
    public void createAndSetLog(LogType logType) {
        final String DELIM = " - ";
        StringBuilder sb = new StringBuilder();
        sb.append(created + DELIM);
        sb.append(username + DELIM);
        switch (logType) {
            case ITEM:
                privateLog = sb.toString() + "drew " + DELIM + draw.getItem().revealAll();
                publicLog = sb.toString() + "reveals " + DELIM + draw.getItem().revealPublic();
                break;
            case BATTLE:
                privateLog = sb.toString() + "plays " + DELIM + draw.getItem().revealAll();
                publicLog = sb.toString() + "reveals " + DELIM + draw.getItem().revealPublic();
                break;
            case TRADE:
                privateLog = sb.toString() + "has received from trade " + DELIM + draw.getItem().revealAll();
                publicLog = sb.toString() + "has received from trade " + DELIM + draw.getItem().revealPublic();
                break;
            case TECH:
                privateLog = sb.toString() + "has teched  " + DELIM + draw.getItem().revealAll();
                publicLog = sb.toString() + "has teched " + DELIM + draw.getItem().revealPublic();
                break;
        }
    }
}
