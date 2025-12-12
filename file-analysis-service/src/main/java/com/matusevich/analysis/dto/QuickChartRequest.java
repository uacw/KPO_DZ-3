package com.matusevich.analysis.dto;

public class QuickChartRequest {
    private String format = "png";
    private int width = 800;
    private int height = 600;
    private String text;

    public QuickChartRequest(String text) {
        this.text = text;
    }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
