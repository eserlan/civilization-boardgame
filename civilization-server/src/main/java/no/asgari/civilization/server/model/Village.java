package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of="name")
@JsonTypeName("village")
@NoArgsConstructor
public class Village implements Item {
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public Village(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.VILLAGES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Village village = (Village) o;

        if (hidden != village.hidden) return false;
        if (used != village.used) return false;
        if (description != null ? !description.equals(village.description) : village.description != null) return false;
        if (!name.equals(village.name)) return false;
        if (owner != null ? !owner.equals(village.owner) : village.owner != null) return false;
        if (type != null ? !type.equals(village.type) : village.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }
}
