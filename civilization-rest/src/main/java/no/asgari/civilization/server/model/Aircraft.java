/*
 * Copyright (c) 2015 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

@Getter
@Setter
@JsonTypeName("aircraft")
@NoArgsConstructor
//@EqualsAndHashCode(of = {"used", "attack", "health", "type"}, callSuper = true)
public class Aircraft extends Unit implements Image {
    private String ownerId; // id of the player which owns this item
    private boolean hidden = true;
    private boolean used;
    private boolean killed;
    private int attack;
    private int health;
    private boolean isInBattle;
    private String image;
    private SheetName sheetName;

    public Aircraft(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public String getType() {
        return type = getClass().getSimpleName();
    }

    /**
     * Aircrafts have no level
     *
     * @return 0
     */
    @JsonIgnore
    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public SheetName getSheetName() {
        return sheetName = SheetName.AIRCRAFT;
    }

    @Override
    public String revealPublic() {
        return getType();
    }

    @Override
    public String revealAll() {
        return toString();
    }

    @Override
    public String toString() {
        return getType() + " " + attack + "." + health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aircraft aircraft = (Aircraft) o;

        if (attack != aircraft.attack) return false;
        if (killed != aircraft.killed) return false;
        if (health != aircraft.health) return false;
        if (hidden != aircraft.hidden) return false;
        if (used != aircraft.used) return false;
        return isInBattle == aircraft.isInBattle;

    }

    @Override
    public int hashCode() {
        int result = (hidden ? 1 : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (killed ? 1 : 0);
        result = 31 * result + attack;
        result = 31 * result + health;
        result = 31 * result + (isInBattle ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @Override
    public String getImage() {
        image = toString() + PNG;
        return image.replaceAll(" ", "");
    }
}
