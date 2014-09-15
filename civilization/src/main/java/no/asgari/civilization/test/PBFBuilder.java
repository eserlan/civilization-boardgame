package no.asgari.civilization.test;

import no.asgari.civilization.excel.ItemReader;
import no.asgari.civilization.excel.UnitReader;
import no.asgari.civilization.representations.GameType;
import no.asgari.civilization.representations.PBF;

public class PBFBuilder {

    public PBF createGameTest(int gameId) throws Exception {
        PBF pbf = new PBF();
        pbf.setGameId(gameId);
        pbf.setNumOfPlayers(4); //TODO
        pbf.setName("First civ game"); //TODO
        pbf.setType(GameType.WAW);  //TODO


        ItemReader items = new ItemReader();
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

}
