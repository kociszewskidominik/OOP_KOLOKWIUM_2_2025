package org.example.circleapp.shared;

public record Dot(double x, double y, double radius, String color) {
    public String toMessage() {
        return String.format("%s;%f;%f;%f", color, radius, x, y);
    }

    public static Dot fromMessage(String message) {
        String[] parts = message.split(";");
        return new Dot(Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[1]), parts[0]);
    }
}
