package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of={"name", "type"})
@JsonTypeName("greatperson")
@NoArgsConstructor
@EqualsAndHashCode(exclude={"ownerId", "hidden", "used"})
public class GreatPerson implements Item {
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String ownerId; // game_id or player_id (username)

    public GreatPerson(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.GREAT_PERSON;
    }

    @Override
    public String revealPublic() {
        return type;
    }

    @Override
    public String revealAll() {
        return name + " " + type;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return 0;
    }
}
