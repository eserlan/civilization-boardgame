package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of={"type", "name", "description"})
@JsonTypeName("wonder")
@NoArgsConstructor
public class Wonder implements Item {
    @JsonIgnore
    public static String ANCIENT = "Ancient";
    @JsonIgnore
    public static String MEDIEVAL = "Medieval";
    @JsonIgnore
    public static String MODERN = "Modern";
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public Wonder(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.WONDERS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wonder wonder = (Wonder) o;

        if (hidden != wonder.hidden) return false;
        if (used != wonder.used) return false;
        if (description != null ? !description.equals(wonder.description) : wonder.description != null) return false;
        if (!name.equals(wonder.name)) return false;
        if (owner != null ? !owner.equals(wonder.owner) : wonder.owner != null) return false;
        if (type != null ? !type.equals(wonder.type) : wonder.type != null) return false;

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
