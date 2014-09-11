package no.asgari.civilization.representations;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.ExcelSheet;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of="name")
@JsonTypeName("cultureIII")
public class CultureIII implements Item {
    @NotEmpty
    private final String name;
    private String id;
    private String description;
    private String type;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public CultureIII(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.CULTURE_3;
    }

}
