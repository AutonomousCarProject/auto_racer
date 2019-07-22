package org.avphs.image.fsmgeneration;


import org.avphs.image.ImageProcessing.PosterColor;

public class ExampleRunner {
    public static void main(String[] args) {
        Table myTable = new Table();
        Thing wall = myTable.newNamedThing("wall");
        
        ThingEndState success = new ThingEndState(wall, myTable);
        
        TableState initial = myTable.getInitialState();
        TableState firstState = myTable.newState();
        TableState secondState = myTable.newState();
        TableState thirdState = myTable.newState();
        TableState fourthState = myTable.newState();
        
        ThingStartState beginWall = new ThingStartState(myTable, wall, firstState);
        
        initial.bindPlusLoop(beginWall, 
                PosterColor.BLACK,
                PosterColor.GREY1);
        
        firstState.bindPlusLoop(secondState, PosterColor.BLUE);

        secondState.bindPlusLoop(thirdState, PosterColor.RED);
        
        thirdState.bindPlusLoop(fourthState, PosterColor.GREEN);
        
        fourthState.bind(success, PosterColor.WHITE);
        
//        int[] intTable = myTable.generateTable();
//        System.out.println(Table.niceFormat(intTable));
        
        Table myTable2 = new Table();
        Thing wall2 = myTable2.newNamedThing("wall_2");
        
        ThingEndState success2 = new ThingEndState(wall2, myTable2);
        
        TableState initial2 = myTable2.getInitialState();
        TableState secondState2 = myTable2.newState();
        TableState thirdState2 = myTable2.newState();
        
        ThingStartState beginWall2 = new ThingStartState(myTable2, wall2, secondState2);
        
        initial2.bindAll(initial2);
        initial2.bindPlusLoop(beginWall2, PosterColor.BLACK);
        
        secondState2.bindPlusLoop(thirdState2, 
                PosterColor.GREY1, 
                PosterColor.GREY2, 
                PosterColor.GREY3, 
                PosterColor.GREY4);
        
        thirdState2.bind(success2, PosterColor.WHITE);

//        int[] intTable2 = myTable2.generateTable();
//        System.out.println(Table.niceFormat(intTable2));
        System.out.println(myTable.debugTableStates(""));
    }
}
