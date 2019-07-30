/**
 * This class consists of a point object with variables x and y, and methods hashCode and equals.
 */

package org.avphs.racingline;

class Point {
    int x, y;

    //region Constructors
    public Point() {
        x = 0;
        y = 0;
    }

    public Point(int _x, int _y) {
        x = _x;
        y = _y;
    }
    //endregion

    //region Overrides
    /**
     * Used for hashmap insertion.
     * @return result
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }


    /**
     * Checks if a point is equal to another point.
     * @param obj
     * @return result
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }
    //endregion
}