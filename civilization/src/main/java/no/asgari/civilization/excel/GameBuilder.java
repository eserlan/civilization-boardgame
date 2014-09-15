package no.asgari.civilization.excel;

import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;

public class GameBuilder {

    private ItemReader items = new ItemReader();

    public PBF createNewPBF(int gameId) throws Exception {
        PBF pbf = new PBF();

        items.readItemsFromExcel();
        UnitReader unit = new UnitReader();
        unit.readAllUnitsFromExcel();

        pbf.setMounted(unit.mountedList);
        pbf.setAircraft(unit.aircraftList);
        pbf.setArtillery(unit.artilleryList);
        pbf.setInfantry(unit.infantryList);

        pbf.setCivs(items.shuffledCivs);
        pbf.setCultureIs(items.shuffledCultureI);
        pbf.setCultureIIs(items.shuffledCultureII);
        pbf.setCultureIIIs(items.shuffledCultureIII);
        pbf.setGreatPersons(items.shuffledGPs);
        pbf.setHuts(items.shuffledHuts);
        pbf.setVillages(items.shuffledVillages);
        pbf.setTiles(items.shuffledTiles);
        pbf.getCitystates().addAll(items.shuffledCityStates);
        pbf.getWonders().addAll(items.ancientWonders);
        pbf.getWonders().addAll(items.medivalWonder);
        pbf.getWonders().addAll(items.modernWonder);
        return pbf;
    }

    public static Player createPlayer(String username, int gameId) {
        Player p = new Player();
        p.setUsername(username);
        p.getGameIds().add(gameId);
        return p;
    }

}
