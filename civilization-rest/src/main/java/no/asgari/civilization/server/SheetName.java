package no.asgari.civilization.server;

import java.util.EnumSet;
import java.util.Optional;

/**
 * The sheet names of the excel sheet
 */
public enum SheetName {
    CIV("Civ"), CULTURE_1("Culture I"), CULTURE_2("Culture II"),
    CULTURE_3("Culture III"), GREAT_PERSON("Great Person"), INFANTRY("Infantry"), ARTILLERY("Artillery"), MOUNTED("Mounted"),
    AIRCRAFT("Aircraft"), VILLAGES("Villages"), HUTS("Huts"), WONDERS("Wonders"), TILES("Tiles"), CITY_STATES("City-states"),
    LEVEL_1_TECH("Level 1 Tech"), LEVEL_2_TECH("Level 2 Tech"), LEVEL_3_TECH("Level 3 Tech"), LEVEL_4_TECH("Level 4 Tech");

    private String label;

    SheetName(String name) {
        this.label = name;
    }

    public String getName() {
        return label;
    }

    public static final EnumSet<SheetName> SHEETS =
            EnumSet.of(SheetName.CIV, SheetName.CULTURE_1, SheetName.CULTURE_2, SheetName.CULTURE_3,
                    SheetName.GREAT_PERSON, SheetName.INFANTRY, SheetName.ARTILLERY, SheetName.MOUNTED, SheetName.AIRCRAFT,
                    SheetName.VILLAGES, SheetName.HUTS, SheetName.WONDERS, SheetName.TILES, SheetName.CITY_STATES,
                    SheetName.LEVEL_1_TECH, SheetName.LEVEL_2_TECH, SheetName.LEVEL_3_TECH, SheetName.LEVEL_4_TECH);

    public static final EnumSet<SheetName> TECHS = EnumSet.of(SheetName.LEVEL_1_TECH, SheetName.LEVEL_2_TECH, SheetName.LEVEL_3_TECH, SheetName.LEVEL_4_TECH);

    public static final EnumSet<SheetName> UNITS = EnumSet.of(SheetName.AIRCRAFT, SheetName.ARTILLERY, SheetName.INFANTRY, SheetName.MOUNTED);

    public static final EnumSet<SheetName> SHUFFLABLE_ITEMS = EnumSet.of(SheetName.AIRCRAFT, SheetName.ARTILLERY, SheetName.INFANTRY, SheetName.MOUNTED,
            SheetName.GREAT_PERSON, SheetName.CULTURE_1, SheetName.CULTURE_2, SheetName.CULTURE_3);

    public static Optional<SheetName> find(String name) {
        String spacesRemovedName = name.replaceAll("\\s", "");
        Optional<SheetName> found = SHEETS.stream()
                .filter(sheet -> sheet.label.replaceAll("\\s", "").equalsIgnoreCase(spacesRemovedName))
                .findFirst();
        if (!found.isPresent()) {
            try {
                return Optional.of(SheetName.valueOf(name));
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
        return found;
    }
}
