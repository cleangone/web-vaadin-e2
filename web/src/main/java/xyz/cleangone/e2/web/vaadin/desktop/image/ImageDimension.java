package xyz.cleangone.e2.web.vaadin.desktop.image;

public class ImageDimension
{
    public enum DimensionType { Height, Width }

    private int value;
    private DimensionType dimensionType;

    public static ImageDimension height(int value) { return new ImageDimension(value, DimensionType.Height); }
    public static ImageDimension width(int value)  { return new ImageDimension(value, DimensionType.Width); }


    public ImageDimension(int value, DimensionType dimensionType)
    {
        this.value = value;
        this.dimensionType = dimensionType;
    }

    public String getHtml()
    {
        return (dimensionType == DimensionType.Height ? "height" : "width") + "=" + value;
    }
}
