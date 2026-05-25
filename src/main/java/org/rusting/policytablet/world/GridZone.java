package org.rusting.policytablet.world;

public class GridZone {
    public static final int ORIGIN_X = 0;
    public static final int ORIGIN_Z = 0;
    public static final int CELL_SIZE = 16;
    public static final int GRID_SIZE = 11;

    public static String getLabelAt(double worldX, double worldZ) {
        int cellX = (int) Math.floor((worldX - ORIGIN_X) / CELL_SIZE);
        int cellZ = (int) Math.floor((worldZ - ORIGIN_Z) / CELL_SIZE);

        if (cellX < 0 || cellX >= GRID_SIZE || cellZ < 0 || cellZ >= GRID_SIZE) {
            return null;
        }

        char rowLetter = (char) ('A' + cellZ);
        return (cellX + 1) + "" + rowLetter;
    }
}
