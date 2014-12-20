package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonRootName("gameLogDTO")
@Data
@NoArgsConstructor
public class GameLogDTO {
    private String id;
    private String log;
    private long created;

    public GameLogDTO(String id, String log, long created) {
        this.id = id;
        this.log = log;
        this.created = created;
    }
}
