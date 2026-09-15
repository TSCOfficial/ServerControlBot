package ch.frily.scb.service;

public record Color (int r, int g, int b){

    public Color (int r, int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color (java.awt.Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }
}
