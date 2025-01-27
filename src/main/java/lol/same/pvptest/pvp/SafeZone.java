package lol.same.pvptest.pvp;

public class SafeZone {
    private final double x;
    private final double y;
    private final double radius;

    public SafeZone(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public boolean isInside(double heroX, double heroY) {
        double distance = Math.sqrt(Math.pow(heroX - this.x, 2) + Math.pow(heroY - this.y, 2));
        return distance <= this.radius;
    }
}