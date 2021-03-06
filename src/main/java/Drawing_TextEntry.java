import java.awt.*;

/**
 * Created by by.dragoon on 10/31/16.
 */
public class Drawing_TextEntry {

    private String text;
    private Color color;

    public Drawing_TextEntry(String text) {
        this(text, Color.black);
    }

    public Drawing_TextEntry(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }
}
