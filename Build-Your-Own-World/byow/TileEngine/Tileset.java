package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "you");
    public static final TETile LIGHT_AVATAR = new TETile('@', Color.darkGray, new Color(255, 123, 0), "lightYou");
    public static final TETile LIGHT_SOURCE_AVATAR = new TETile('@', Color.WHITE, new Color(255, 123, 0), "lightSourceYou");
    public static final TETile WALL = new TETile('☒', new Color(126, 108, 108), new Color(255, 169, 163),
            "wall");
    public static final TETile FLOOR = new TETile('·', Color.PINK, Color.black,
            "floor");
    public static final TETile LIGHT_FLOOR = new TETile('·', Color.PINK, new Color(255, 235, 128),
            "lightFloor");

    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "grass");
    public static final TETile WATER = new TETile('≈', Color.blue, new Color(178, 226, 238), "water");
    public static final TETile LIGHTENING_BOLT = new TETile('☤', Color.red, Color.BLACK, "lighteningBooster");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, new Color(255, 211, 240), "flower");
    public static final TETile LIGHT_FLOWER = new TETile('❀', Color.magenta, new Color(255, 123, 0), "lightFlower");
    public static final TETile LIGHT_SOURCE_FLOWER = new TETile('❀', Color.darkGray, new Color(255, 123, 0), "lightSourceFlower");
    public static final TETile LOCKED_DOOR = new TETile(' ', new Color(255, 90, 82), new Color(255, 90, 82),
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "mountain");
    public static final TETile TREE = new TETile('♣', new Color(35, 158, 55), new Color(211, 255, 201), "tree");
    public static final TETile LIGHT_TREE = new TETile('♣', Color.WHITE, new Color(255, 123, 0), "lightTree");
    public static final TETile LIGHT_SOURCE_TREE = new TETile('♣', Color.darkGray, new Color (255, 123, 0), "lightSourceTree");
    public static final TETile LIGHT_SOURCE = new TETile('☼', Color.black, new Color(248, 194, 84), "lightSource");
    public static final TETile DIAMOND = new TETile('♦', new Color(190, 215, 255), new Color(2, 32, 76),
            "diamond");
}