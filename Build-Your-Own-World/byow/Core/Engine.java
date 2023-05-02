package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.Utils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import java.util.Timer;
import java.util.TimerTask;

public class Engine implements Serializable{
    /* Room class, containing bottom-right coordinate, dimensions, and connected rooms */
    private static class Room implements Serializable{
        private int x;
        private int y;
        private int width;
        private int height;
        private Point center;

        public Room(int x, int y, int width, int height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            center = new Point(x + width / 2, y + height / 2);
        }
    }

    class Task extends TimerTask {
        private String name;
        //        private Integer timeLimit;
        private int time = 0;
        public Task(String name) {
            this.name = name;
//            this.timeLimit = timeLimit;

        }
        public void run() {
//            System.out.println("[" + new Date() + "] " + name + ": task executed!");
            if (name.equals("GameTimer")) {
                playerTime = playerTime.intValue() + 1;

                if (DEBUG){
                    ter.renderFrame(map);
                    drawHUD();
                }
                if (playerTime >= GAME_LIMIT) {
                    gameOver();
                }
            }
        }
    }

    public static final TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 65;
    public static final int HEIGHT = 40;
    public static Long seed = null;
    public static Random RANDOM = null;
    public static LinkedList<Room> rooms = new LinkedList<>();
    private static final boolean DEBUG = true; //change to false before submitting to ag
    private static Point avatarLocation;
    private LinkedList<Room> roomsWherelightSourceAre = new LinkedList<Room>();
    private TETile[][] map;
    private TETile avatar = Tileset.AVATAR;
    private TETile lightModeAvatar = Tileset.LIGHT_AVATAR;
    private TETile lightSourceAvatar = Tileset.LIGHT_SOURCE_AVATAR;
    private TETile lightFloor = Tileset.LIGHT_FLOOR;
    private Task gameTimer;
    private Room roomWithEndDoor = new Room(0, 0, 0, 0);
    private boolean gameOver = false;
    private static final int GAME_LIMIT = 20; //in seconds
    private static Integer leaderBoard = null;
    private static Integer playerTime = 0;
    private LinkedList<Point> waterPuddleLocations = new LinkedList<Point>();
    private LinkedList<Point> lighteningBoosterLocations = new LinkedList<Point>();

    private Point mouseLocation = setMouseLocation();
    private boolean areLightsOn = false;
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The savedCurrentMap text file. */
    public static final File savedCurrentMap = new File(CWD, "currentMapSerialized.txt");
    public static final File savedLightsRoom = new File(CWD, "lightsRoom.txt");
    public static final File savedLightsStatus = new File(CWD, "lightsStatus.txt");
    public static final File savedAvatar = new File(CWD, "avatar.txt");
    public static final File savedLeaderBoard = new File(CWD, "leaderBoard.txt");

    void gameOver() {
        gameTimer.cancel();
        drawLoseScreen();

    }
    public void initializeFiles(){
        try {
            savedCurrentMap.createNewFile();
            savedLeaderBoard.createNewFile();
            //Initialize leader board
            if(!Utils.readContentsAsString(savedLeaderBoard).equals("")) {
                leaderBoard = Utils.readObject(savedLeaderBoard, Integer.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Point setMouseLocation(){
        Point location = null;
        if(DEBUG){
            location = new Point((int) StdDraw.mouseX(), (int) StdDraw.mouseY());
        }
        return location;
    }

    public void drawMainMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT * (2.75/4), "CS61BL: THE GAME");

        Font fontSmall = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontSmall);

        StdDraw.text(WIDTH / 2, HEIGHT * (3.5/8), "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * (3.0/8), "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT * (2.5/8), "Quit (Q)");
        StdDraw.text(WIDTH / 2, HEIGHT * (2.0/8), "Choose Avatar (V)"); //ambition #1 (2 pts), changing your avatar

        if (DEBUG){
            StdDraw.show();
        }
    }

    public void updateMainScreen(String seedToDisplay){
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT * (2.75/4), "CS61BL: The Game");

        Font fontSmall = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontSmall);

        StdDraw.text(WIDTH / 2, HEIGHT * (3.5/8), "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * (3.0/8), seedToDisplay);

        if (DEBUG){
            StdDraw.show();
        }
    }

    public void drawWinScreen(){
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH/2,HEIGHT * (2.75 / 4), "You win!");
        StdDraw.text(WIDTH / 2, HEIGHT * (3.2/4), "Treasure reached in " + "seconds");

        StdDraw.text(WIDTH / 2, HEIGHT * (3.5/4), "Press Q to quit");

        if (DEBUG){
            StdDraw.show();
            while (true){
                if (StdDraw.hasNextKeyTyped()){
                    char command = StdDraw.nextKeyTyped();
                    if (command == 'Q' || command == 'q') {
                        saveAndExit();
                    }
                }
            }
        }
    }

    public void drawLoseScreen(){
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH/2,HEIGHT * (2.75 / 4), "You lose!");
        StdDraw.text(WIDTH / 2, HEIGHT * (2.25/4), "You did not reach the diamond in time :(");
        StdDraw.text(WIDTH / 2, HEIGHT * (2.00/4), "Better luck next time!");

        StdDraw.text(WIDTH / 2, HEIGHT * (1.5/4), "Press Q to quit");

        if (DEBUG){
            StdDraw.show();
            while (true){
                if (StdDraw.hasNextKeyTyped()){
                    if (StdDraw.hasNextKeyTyped()) {//additional check as it was failing
                        char command = StdDraw.nextKeyTyped();
                        if (command == 'Q' || command == 'q') {
                            saveAndExit();
                        }
                    }
                }
            }
        }
    }
    public void drawAvatarSelectionScreen(){
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT * (2.75/4), "Choose your player");

        Font fontSmall = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontSmall);

        StdDraw.text(WIDTH / 2, HEIGHT * (3.5/8), "Flower (F)");
        StdDraw.text(WIDTH / 2, HEIGHT * (3.0/8), "Tree (T)");

        if (DEBUG){
            StdDraw.show();

        }
    }

    public void interactWithAvatarSelectionMenu(){
        while (true){
            if (StdDraw.hasNextKeyTyped()){
                char command = StdDraw.nextKeyTyped();
                if (command == 'F' || command == 'f') {
                    setAvatartToFlower();
                } else if (command == 'T' || command == 't'){
                    setAvatarToTree();
                }
                break;
            }
        }
    }

    private void setAvatarToTree() {
        avatar = Tileset.TREE;
        lightModeAvatar = Tileset.LIGHT_TREE;
        lightSourceAvatar = Tileset.LIGHT_SOURCE_TREE;
    }

    private void setAvatartToFlower() {
        avatar = Tileset.FLOWER;
        lightModeAvatar = Tileset.LIGHT_FLOWER;
        lightSourceAvatar = Tileset.LIGHT_SOURCE_FLOWER;
    }

    public void interactWithMenu(){
        while (true){
            if (StdDraw.hasNextKeyTyped()){
                char command = StdDraw.nextKeyTyped();
                if (command == 'N' || command == 'n') {
                    String seed = command + "";
                    String seedToBeDisplayed = "Seed: ";
                    updateMainScreen(seedToBeDisplayed);
                    while (!seed.contains("s") & !seed.contains("S")){
                        if (StdDraw.hasNextKeyTyped()) {
                            char nextKeyTyped = StdDraw.nextKeyTyped();
                            seed += nextKeyTyped;
                            seedToBeDisplayed += nextKeyTyped;
                            updateMainScreen(seedToBeDisplayed);
                        }
                    }
                    map = interactWithInputString(seed);



                    moveAvatarThroughMap(map);
                    break;
                } else if (command == 'L' || command == 'l'){
                    if(!Utils.readContentsAsString(savedCurrentMap).equals("")){
                        loadWorldMap();
                        break;
                    } else{ //if L is pressed but no map has been saved, exit program
                        if (DEBUG){
                            System.exit(0);
                        }
                    }

                } else if (command == 'Q' || command == 'q'){
                    if (DEBUG){
                        System.exit(0);
                    }
                } else if (command == 'V' || command == 'v'){
                    drawAvatarSelectionScreen();
                    interactWithAvatarSelectionMenu();
                    drawMainMenu();
                }
            }
        }
    }



    public void moveAvatarThroughMap(TETile[][] tiles){
        char previousCommand = ' ';
        while(true){
            if(StdDraw.hasNextKeyTyped()){

                char nextKeyTyped = StdDraw.nextKeyTyped();
                moveAvatarBasedOnCommand(nextKeyTyped, previousCommand, tiles);
                previousCommand = nextKeyTyped;

                if (DEBUG){
                    ter.renderFrame(map);
                    drawHUD();
                }
            }
            updateHUDOnScreen();
        }
    }

    public void moveAvatarBasedOnCommand(char currCmd, char prevCmd, TETile[][] tiles){
        if (currCmd == 'a' || currCmd == 'A'){ //a = move left
            moveAvatar(tiles, "left");
        } else if (currCmd == 'd' || currCmd == 'D'){ //d = move right
            moveAvatar(tiles, "right");
        }
        else if (currCmd == 'w' || currCmd == 'W'){ //w = move up
            moveAvatar(tiles, "up");
        } else if (currCmd == 's' || currCmd == 'S'){ //s = move down
            moveAvatar(tiles, "down");
        } else if (currCmd == 'q' || currCmd == 'Q') { //q = second part of quit string
            if (prevCmd == ':') {
                saveAndExit();
            }
        } else if (currCmd == 'o' || currCmd == 'O'){ //toggle lights on or off
            if (areLightsOn){
                toggleLightsOff(map);
            } else{
                toggleLightsOn(map);
            }
        } else if (currCmd == 'g' || currCmd == 'G') { //start the game
            if (gameTimer == null) {//initiate the timer on the first key
                //Start the timer
                gameTimer = new Task("GameTimer");
                Timer t = new Timer();
                t.schedule(gameTimer, 0, 1000); //  executes for every 1 seconds
            }
        }
    }

    public void toggleLightsOn(TETile[][] tiles){
        for (Room r: roomsWherelightSourceAre){
            for (int x = r.x; x < r.x + r.width; x++) {
                for (int y = r.y; y < r.y + r.height; y++) {
                    if (tiles[x][y] != Tileset.WALL && tiles[x][y] != Tileset.LIGHT_SOURCE && !isARoomBorderTile(r, x, y)) {
                        if (x == avatarLocation.x && y == avatarLocation.y) {//it's avatar
                            if (tiles[x][y] != lightSourceAvatar) {
                                tiles[x][y] = lightModeAvatar;
                            }
                        } else {
                            tiles[x][y] = lightFloor; //change to be some lighted up tile
                        }
                    }
                }
            }
        }
        areLightsOn = true;
    }

    public void toggleLightsOff(TETile[][] tiles){
        for (Room r: roomsWherelightSourceAre) {
            for (int x = r.x; x < r.x + r.width; x++) {
                for (int y = r.y; y < r.y + r.height; y++) {
                    if (tiles[x][y] != Tileset.WALL && tiles[x][y] != Tileset.LIGHT_SOURCE && !isARoomBorderTile(r, x, y)) {
                        if (x == avatarLocation.x && y == avatarLocation.y) {//it's avatar
                            if (tiles[x][y] != lightSourceAvatar) {
                                tiles[x][y] = avatar;
                            }
                        } else {
                            tiles[x][y] = Tileset.FLOOR; //change to be some lighted up tile
                        }
                    }
                }
            }
        }
        areLightsOn = false;
    }

    private boolean isARoomBorderTile(Room r, int xPos, int yPos){
        if (xPos == r.x || xPos == r.x + r.width - 1 || yPos == r.y || yPos == r.y + r.height - 1){
            return true;
        }
        return false;
    }



    public void saveAndExit(){
        Utils.writeObject(savedCurrentMap, convertTileMapToStringMatrix());
        Utils.writeObject(savedLightsRoom, roomsWherelightSourceAre);
        Utils.writeObject(savedLightsStatus, areLightsOn);
        Utils.writeObject(savedAvatar, avatar.description());
        Utils.writeObject(savedLeaderBoard, leaderBoard);
        if (DEBUG){
            System.exit(0);
        }
    }

    private void loadWorldMap() {
        //Load the tiles
        String[][] tileDescriptions = Utils.readObject(savedCurrentMap, String[][].class);
        convertStringMatrixToTileMap(tileDescriptions);

        //load the light rooms
        roomsWherelightSourceAre = Utils.readObject(savedLightsRoom, LinkedList.class);
        areLightsOn = Utils.readObject(savedLightsStatus, Boolean.class);


        //load Avatar
        String avatarDesc = Utils.readObject(savedAvatar, String.class);
        if (Tileset.TREE.description().equals(avatarDesc)) {
            setAvatarToTree();
        } else  if (Tileset.FLOWER.description().equals(avatarDesc)) {
            setAvatartToFlower();
        } else  {
            avatar = Tileset.AVATAR;
            lightModeAvatar = Tileset.LIGHT_AVATAR;
            lightSourceAvatar = Tileset.LIGHT_SOURCE_AVATAR;
        }

        Font font = new Font("Monaco", Font.BOLD, 14); //force setting tile font size
        StdDraw.setFont(font);
        if (DEBUG){
            ter.renderFrame(map);
            drawHUD();
        }

        moveAvatarThroughMap(map);
    }

    public String[][] convertTileMapToStringMatrix(){
        String[][] tileDescriptions = new String[map.length][map[0].length];
        for (int x = 0; x < map.length; x++){
            for (int y = 0; y < map[0].length; y++){
                tileDescriptions[x][y] = map[x][y].description();
            }
        }
        return tileDescriptions;
    }

    public void convertStringMatrixToTileMap(String[][] tileDescriptionMatrix){
        TETile[][] tileMap = new TETile[tileDescriptionMatrix.length][tileDescriptionMatrix[0].length];
        for (int x = 0; x < tileDescriptionMatrix.length; x++){
            for (int y = 0; y < tileDescriptionMatrix[0].length; y++){
                if (tileDescriptionMatrix[x][y].equals(Tileset.FLOOR.description())){ //floor
                    tileMap[x][y] = Tileset.FLOOR;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.WALL.description())){ //wall
                    tileMap[x][y] = Tileset.WALL;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.NOTHING.description())){ //nothing
                    tileMap[x][y] = Tileset.NOTHING;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_FLOOR.description())){ //floor
                    tileMap[x][y] = Tileset.LIGHT_FLOOR;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_SOURCE.description())){ //floor
                    tileMap[x][y] = Tileset.LIGHT_SOURCE;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.DIAMOND.description())){ //floor
                    tileMap[x][y] = Tileset.DIAMOND;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.WATER.description())){ //floor
                    tileMap[x][y] = Tileset.WATER;
                } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHTENING_BOLT.description())){ //floor
                    tileMap[x][y] = Tileset.LIGHTENING_BOLT;
                } else{ //avatar
                    if (tileDescriptionMatrix[x][y].equals(Tileset.AVATAR.description())){ //avatar = @
                        tileMap[x][y] = Tileset.AVATAR;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.FLOWER.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.FLOWER;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.TREE.description())){ //avatar = tree
                        tileMap[x][y] = Tileset.TREE;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_AVATAR.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.LIGHT_AVATAR;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_SOURCE_AVATAR.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.LIGHT_SOURCE_AVATAR;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_FLOWER.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.LIGHT_FLOWER;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_SOURCE_FLOWER.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.LIGHT_SOURCE_FLOWER;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_TREE.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.LIGHT_TREE;
                    } else if (tileDescriptionMatrix[x][y].equals(Tileset.LIGHT_SOURCE_TREE.description())){ //avatar = flower
                        tileMap[x][y] = Tileset.LIGHT_SOURCE_TREE;
                    }
                    avatarLocation = new Point(x, y); //set class var avatarLocation
                }
            }
        }
        map = tileMap;
    }

    private void moveAvatar(TETile[][] tiles, String movePosition){
        int currentAvatarX = avatarLocation.x;
        int currentAvatarY = avatarLocation.y;
        TETile currentAvatar = tiles[currentAvatarX][currentAvatarY];
        int newPositionX = currentAvatarX;
        int newPositionY = currentAvatarY;

        if (movePosition.equals("left")) {
            newPositionX--;
        } else if (movePosition.equals("right")) {
            newPositionX++;
        }

        if (movePosition.equals("down")) {
            newPositionY--;
        } else if (movePosition.equals("up")) {
            newPositionY++;
        }

        if (!tiles[newPositionX][newPositionY].equals(Tileset.WALL)) { //move only if you're not hitting a wall
            //Move the avatar to new location
            avatarLocation = new Point(newPositionX, newPositionY);

            if (tiles[newPositionX][newPositionY].equals(Tileset.DIAMOND)){ //game over if player reaches diamond
                gameTimer.cancel();
                gameOver = true;
                //check if this is a new record
                if (leaderBoard == null || playerTime < leaderBoard) {
                    leaderBoard = playerTime;
                }
                drawWinScreen();
            }  else if (tiles[newPositionX][newPositionY].equals(Tileset.WATER)) {//next tile is puddle
                tiles[newPositionX][newPositionY] = avatar;
                playerTime += 3;
            } else if (tiles[newPositionX][newPositionY].equals(Tileset.LIGHTENING_BOLT)) {//next tile is puddle
                tiles[newPositionX][newPositionY] = avatar;
                playerTime -= 3;
            } else if (tiles[newPositionX][newPositionY].equals(lightFloor)) {//next tile is light floor
                tiles[newPositionX][newPositionY] = lightModeAvatar;
            } else if (tiles[newPositionX][newPositionY].equals(Tileset.LIGHT_SOURCE)) { //next tile = light source
                tiles[newPositionX][newPositionY] = lightSourceAvatar;
            } else { //next tile is regular floor
                tiles[newPositionX][newPositionY] = avatar;
            }

            //change the current position of avatar
            if (currentAvatar.equals(lightModeAvatar)) {//current tile is light avatar
                tiles[currentAvatarX][currentAvatarY] = lightFloor;
            } else if (currentAvatar.equals(lightSourceAvatar)) { //current tile is light source avatar
                tiles[currentAvatarX][currentAvatarY] = Tileset.LIGHT_SOURCE;
            } else {//current tile is regular avatar
                tiles[currentAvatarX][currentAvatarY] = Tileset.FLOOR;
            }
        }
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        initializeFiles();
//        gameTimer = new Task();
        ter.initialize(WIDTH, HEIGHT);
        drawMainMenu();
        interactWithMenu();
    }

    /** Initializes tiles[][] to all NOTHING tiles */
    public void initializeTiles(TETile[][] tiles){
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT - 2; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Checks if rooms dimensions will be within the tile[][] dimensions */
    private boolean isInBounds(int x, int y, int width, int height, TETile[][] tiles){
        if (x + width < tiles.length && y + height < tiles[0].length){
            return true;
        }
        return false;
    }

    /** Checks if two rooms are overlapping */
    private boolean overlaps(int x1, int y1, int width1, int height1, TETile[][] tiles){
        for (int x = x1; x < x1 + width1; x++){
            for (int y = y1; y < y1 + height1; y++){
                if (tiles[x][y].equals(Tileset.WALL) || tiles[x][y].equals(Tileset.FLOOR)){
                    return true;
                }
            }
        }
        return false;
    }

    /** */
    public void createRooms(TETile[][] tiles){
        int currNumRooms = 0;
        int numRooms = 13 + RandomUtils.uniform(RANDOM, 10);
        while (currNumRooms <  numRooms){
            int nextRandomIntX = RandomUtils.uniform(RANDOM, tiles.length);
            int nextRandomIntY = RandomUtils.uniform(RANDOM, tiles[0].length);
            int width = RandomUtils.uniform(RANDOM, (int) tiles.length / 10) + 5;
            int height = RandomUtils.uniform(RANDOM, (int) tiles[0].length / 10) + 5;

            if(isInBounds(nextRandomIntX, nextRandomIntY, width, height, tiles)){
                if (!overlaps(nextRandomIntX, nextRandomIntY, width, height, tiles)){
                    Room newRoom = new Room(nextRandomIntX, nextRandomIntY, width, height);
                    rooms.add(newRoom);

                    for (int x = nextRandomIntX; x < nextRandomIntX + width; x++){
                        for (int y = nextRandomIntY; y < nextRandomIntY + height; y++){
                            tiles[x][y].setRoomNumber(currNumRooms + 1);
                            if (x == nextRandomIntX || y == nextRandomIntY || x == nextRandomIntX + width - 1 || y == nextRandomIntY + height - 1) {
                                tiles[x][y] = Tileset.WALL;
                            } else{
                                tiles[x][y] = Tileset.FLOOR;
                            }
                            tiles[x][y].setRoomNumber(currNumRooms + 1);
                        }
                    }
                    currNumRooms++;
                }
            }
        }
    }

    /** Randomly spawns a user avatar (@, flower, or tree) on any floor tile */
    public void spawnAvatar(TETile[][] tiles){
        int x = RandomUtils.uniform(RANDOM, tiles.length);
        int y = RandomUtils.uniform(RANDOM, tiles[0].length);
        while (!tiles[x][y].equals(Tileset.FLOOR)){
            x = RandomUtils.uniform(RANDOM, tiles.length);
            y = RandomUtils.uniform(RANDOM, tiles[0].length);
        }
        tiles[x][y] = avatar;
        avatarLocation = new Point(x, y);
    }

    /** Randomly selects 3 rooms to place light sources in the center of */
    public void placeThreeLightSources(TETile[][] tiles){
        for (int i = 0; i < 3; i++){
            Room r = rooms.get(RandomUtils.uniform(RANDOM, rooms.size()));
            while(roomsWherelightSourceAre.contains(r)){
                r = rooms.get(RandomUtils.uniform(RANDOM, rooms.size()));
            }
            roomsWherelightSourceAre.add(r);

            int x = (int) r.center.getX();
            int y = (int) r.center.getY();

            tiles[x][y] = Tileset.LIGHT_SOURCE;
        }
    }

    /** Randomly selects 3 rooms to place light sources in the center of */
    public void placeGoalDiamond(TETile[][] tiles){
        Room r = rooms.get(RandomUtils.uniform(RANDOM, rooms.size()));
        while (roomWithEndDoor.equals(r) || roomsWherelightSourceAre.contains(r)) {
            r = rooms.get(RandomUtils.uniform(RANDOM, rooms.size()));
        }
        roomWithEndDoor = r;

        int x = (int) r.center.getX();
        int y = (int) r.center.getY();

        tiles[x][y] = Tileset.DIAMOND;
    }

    /* Calculates distance between centers of two rooms (2 points) */
    private double distanceBetweenPoints(Point point1, Point point2){
        return (Math.sqrt(Math.pow((point1.getX() - point2.getX()), 2) + Math.pow((point1.getY() - point2.getY()), 2)));
    }

    //SOURCE: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
    /** Function to sort hashmap by values */
    public HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm){
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double> > list = new LinkedList<Map.Entry<Integer, Double> >(hm.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >() { // Sort the list
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2){
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list){ // put data from sorted list to hashmap
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private int[][] orderedDistanceBetweenPoints() {
        int[][] distancesBetweenPoints = new int[rooms.size()][rooms.size() - 1];
        for (int i = 0; i < rooms.size(); i++){
            Point p1 = rooms.get(i).center;
            HashMap<Integer, Double> distanceToPoint1Map = new HashMap<Integer, Double>();
            for (int j = 0; j < rooms.size(); j++){
                Point p2 = rooms.get(j).center;
                double distBetweenPoints = distanceBetweenPoints(p1, p2);
                if (distBetweenPoints != 0){
                    distanceToPoint1Map.put(j, distBetweenPoints);
                }
            }
            distanceToPoint1Map = sortByValue(distanceToPoint1Map);
            int col = 0;
            for (Map.Entry mapElement : distanceToPoint1Map.entrySet()) {
                distancesBetweenPoints[i][col] = (int) mapElement.getKey();
                col++;
            }
        }
        return distancesBetweenPoints;
    }

    private int findClosestUnvisitedRoom(int[] orderedClosestRooms, LinkedList<Integer> visitedRooms){
        for (int i: orderedClosestRooms){
            if(!visitedRooms.contains(i)){
                return i;
            }
        }
        return visitedRooms.get(0);
    }

    private void setAdjacentNothingTilesToWalls(int x, int y, TETile[][] tiles){
        int topLeftCornerX = x - 1;
        int topLeftCornerY = y + 1;
        for (int i = topLeftCornerX; i < topLeftCornerX + 3; i++){
            for (int j = topLeftCornerY; j >= topLeftCornerY - 2; j--){
                if (tiles[i][j] == Tileset.NOTHING){ //add walls to nothing tiles adjacent to floor tiles
                    tiles[i][j] = Tileset.WALL;
                }
            }
        }
    }

    private void drawHallway(Point p1, Point p2, TETile[][] tiles){
        int p1X = (int) p1.getX();
        int p1Y = (int) p1.getY();
        int p2X = (int) p2.getX();
        int p2Y = (int) p2.getY();

        if(p1Y > p2Y) { //p1 is above p2
            for (int y = p1Y; y >= p2Y; y--) {
                tiles[p1X][y] = Tileset.FLOOR;
                setAdjacentNothingTilesToWalls(p1X, y, tiles);
            }
        } else { // p1 is below p2
            for (int y = p1Y; y <= p2Y; y++){
                tiles[p1X][y] = Tileset.FLOOR;
                setAdjacentNothingTilesToWalls(p1X, y, tiles);
            }
        }
        if (p1X > p2X){ //p1 is to the right of p2
            for (int x = p1X; x >= p2X; x--){
                tiles[x][p2Y] = Tileset.FLOOR;
                setAdjacentNothingTilesToWalls(x, p2Y, tiles);
            }
        } else{ //p1 is to the left of p2
            for (int x = p1X; x <= p2X; x++){
                tiles[x][p2Y] = Tileset.FLOOR;
                setAdjacentNothingTilesToWalls(x, p2Y, tiles);
            }
        }
    }

    public void createHallways(TETile[][] tiles){
        int[][] orderedDistArray = orderedDistanceBetweenPoints();
        LinkedList<Integer> visited = new LinkedList<>();
        for (int i = 0; i < orderedDistArray.length; i++){
            int closestUnvisitedRoomNum = findClosestUnvisitedRoom(orderedDistArray[i], visited);
            drawHallway(rooms.get(i).center, rooms.get(closestUnvisitedRoomNum).center, tiles);
            visited.add(i);
        }
    }


    public void drawHUD(){
        Font fontSmall = new Font("Monaco", Font.BOLD, 15);
        StdDraw.setFont(fontSmall);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.line(0, HEIGHT - 2, WIDTH, HEIGHT - 2);

        int mousePosX = (int) StdDraw.mouseX();
        int mousePosY = (int) StdDraw.mouseY();

        if (mousePosY < HEIGHT - 2) { //If the mouse is in the time map, then show the mouse tile description
            String tileDescription = map[mousePosX][mousePosY].description();
            StdDraw.text(WIDTH * 0.12, HEIGHT - 1, "Tile Type: " + tileDescription);
            String lightStatus = "on";
            if (!areLightsOn){
                lightStatus = "off";
            }
            StdDraw.textRight(WIDTH * 0.85, HEIGHT - 1, "Lights on/off (O): " + lightStatus);
            StdDraw.text(WIDTH * 0.4, HEIGHT - 1, "Time left: " + (GAME_LIMIT-playerTime) + " (Leader: " + (leaderBoard == null?GAME_LIMIT:GAME_LIMIT-leaderBoard) + ")");

        }
        if (DEBUG){
            StdDraw.show();
        }
    }

    /** Updates HUD's Tile Type if cursor has been moved */
    public void updateHUDOnScreen(){
        int mousePosX = (int) StdDraw.mouseX();
        int mousePosY = (int) StdDraw.mouseY();
        if (mousePosX != mouseLocation.getX() || mousePosY != mouseLocation.getY()){
            mouseLocation.setLocation(mousePosX, mousePosY);
            if (DEBUG){
                ter.renderFrame(map);
                drawHUD();
            }
        }
    }

//    public void timerDecrementing(){
//        //timer code
//        gameTimer.scheduleAtFixedRate(new TimerTask() {
//            int i = 20;
//            public void run() {
//                i--;
//                if (i < 0) {
//                    gameTimer.cancel();
//                } else{
//                    StdDraw.textRight(WIDTH * 0.50, HEIGHT - 1, "Timer: " + i);
//                }
//            }
//        }, 0, 1000);
//    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it runs the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        TETile[][] finalWorldFrame = null;
        input = input.toLowerCase(Locale.ROOT); //make all input commands lower-case
        char[] inputChars = input.toCharArray(); //convert input to char[]

        if (inputChars[0] == 'n') {
            int indexOfS = input.indexOf('s');

            seed = Long.parseLong(input.substring(1, indexOfS));
            RANDOM = new Random(seed);
            rooms = new LinkedList<Room>();

            finalWorldFrame = new TETile[WIDTH][HEIGHT - 2];
            initializeTiles(finalWorldFrame); //initialize tiles
            createRooms(finalWorldFrame); //create rooms
            createHallways(finalWorldFrame); //draw hallways
            spawnAvatar(finalWorldFrame); //spawn avatar
            placeThreeLightSources(finalWorldFrame); //spawn light sources in 3 random rooms
            placeGoalDiamond(finalWorldFrame);
            placeSixPuddles(finalWorldFrame);
            placeFourLighteningBoosters(finalWorldFrame);

            map = finalWorldFrame;

            if (DEBUG) {
                ter.initialize(WIDTH, HEIGHT);
                ter.renderFrame(finalWorldFrame);
                drawHUD();
            }

            if (inputChars.length > indexOfS+1){ //if there are more commands after the n###s
                processMapCommands(inputChars, indexOfS + 1, map);
            }

        } else if (inputChars[0] == 'l'){
            if(!Utils.readContentsAsString(savedCurrentMap).equals("")){ //if some map has been saved before
                String[][] tileDescriptions = Utils.readObject(savedCurrentMap, String[][].class);
                convertStringMatrixToTileMap(tileDescriptions);
                if (DEBUG){
                    Font font = new Font("Monaco", Font.BOLD, 14); //force setting tile font size
                    StdDraw.setFont(font);
                    ter.initialize(WIDTH, HEIGHT);
                    ter.renderFrame(map);
                    drawHUD();
                }
                processMapCommands(inputChars, 1, map);
            } else{ //if L is pressed but no map has been saved, exit program
                if (DEBUG){
                    System.exit(0);
                }
            }
        } else if (inputChars[0] == 'q'){
            if (DEBUG){
                System.exit(0);
            }
        }

        return map;
    }

    private void processMapCommands(char[] inputChars, int startOfCommand, TETile[][] map) {
        for (int i = startOfCommand; i < inputChars.length; i++){
            char prevCommand = inputChars[i - 1];
            if (i == startOfCommand){
                prevCommand = inputChars[i];
            }
            moveAvatarBasedOnCommand(inputChars[i], prevCommand, map);
            if (DEBUG) {
                ter.renderFrame(map);
                drawHUD();
            }
        }
    }

    /** Randomly spawns 6 water puddles on any plain floor tile */
    public void placeSixPuddles(TETile[][] tiles){
        int x = RandomUtils.uniform(RANDOM, tiles.length);
        int y = RandomUtils.uniform(RANDOM, tiles[0].length);
        Point p = new Point(x, y);

        for (int i = 0; i < 6; i++){
            while (!tiles[x][y].equals(Tileset.FLOOR) || waterPuddleLocations.contains(p) || pointIsInRoomWithLightSource(tiles, p)){
                x = RandomUtils.uniform(RANDOM, tiles.length);
                y = RandomUtils.uniform(RANDOM, tiles[0].length);
                p = new Point(x, y);
            }
            waterPuddleLocations.add(p);
            tiles[x][y] = Tileset.WATER;
        }
    }

    /** Randomly spawns 6 water puddles on any plain floor tile */
    public void placeFourLighteningBoosters(TETile[][] tiles){
        int x = RandomUtils.uniform(RANDOM, tiles.length);
        int y = RandomUtils.uniform(RANDOM, tiles[0].length);
        Point p = new Point(x, y);

        for (int i = 0; i < 4; i++){
            while (!tiles[x][y].equals(Tileset.FLOOR) || lighteningBoosterLocations.contains(p) || pointIsInRoomWithLightSource(tiles, p)){
                x = RandomUtils.uniform(RANDOM, tiles.length);
                y = RandomUtils.uniform(RANDOM, tiles[0].length);
                p = new Point(x, y);
            }
            lighteningBoosterLocations.add(p);
            tiles[x][y] = Tileset.LIGHTENING_BOLT;
        }
    }

    private boolean pointIsInRoomWithLightSource(TETile[][] tiles, Point p){
        for (Room r: roomsWherelightSourceAre){
            if (p.getX() > r.x && p.getX() < r.x + r.width){
                if (p.getY() > r.y && p.getY() < r.y + r.height){
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Engine e = new Engine();
        e.interactWithInputString("N999SDDDWWWDDD");
        //e.interactWithInputString("N999SDDD:q");
        //e.interactWithInputString("LWWWAaA");
        //e.interactWithInputString("n7193300625454684331saaawasdaawd:q");
    }
}