package plu.red.reversi.core.util;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Glory to the Red Team.
 *
 * Utility Color class to represent a color. Use this instead of <code>java.awt.Color</code> because Android does not support
 * <code>java.awt.Color</code>. Also used instead of a Vec3 representation because some GUI components use
 * a 32-bit int representation for a color, where each 8-bits represent ARGB, respectively. This Color class will
 * support converting to and from a Vec3 representation as well as a 32-bit int representation, however. In addition,
 * this class will have methods for accessing and changing individual components, as well as applying filter-level
 * changes to the entire color (such as alpha blending of varying types).
 * 
 * Similarly to <code>java.awt.Color</code>, values are final and cannot be changed. If a different Color is needed, it
 * must be newly constructed.
 */
public class Color implements Comparable<Color> {

    static {
        // Register DataMap JSON Conversion
        DataMap.Setting.registerConverter(Color.class, (key, value, json) -> json.put(key, value.composite), (key, json) -> new Color(json.getInt(key)));
    }

    // Some simple static colors to use
    public static final Color BLACK     = new Color(0.0f, 0.0f, 0.0f);
    public static final Color WHITE     = new Color(1.0f, 1.0f, 1.0f);
    public static final Color RED       = new Color(1.0f, 0.0f, 0.0f);
    public static final Color GREEN     = new Color(0.0f, 1.0f, 0.0f);
    public static final Color BLUE      = new Color(0.0f, 0.0f, 1.0f);
    public static final Color YELLOW    = new Color(1.0f, 1.0f, 0.0f);
    public static final Color CYAN      = new Color(0.0f, 1.0f, 1.0f);
    public static final Color PURPLE    = new Color(1.0f, 0.0f, 1.0f);
    public static final Color GRAY      = new Color(0.5f, 0.5f, 0.5f);

    /**
     * Composites <code>red</code>, <code>green</code>, <code>blue</code>, and <code>alpha</code> float values to a
     * 32-bit integer representation of the color. Bits 0-7 are <code>blue</code>, 8-15 are <code>green</code>,
     * 16-23 are <code>red</code>, and 24-31 are <code>alpha</code>. Input float values are clamped between 0.0f and
     * 1.0f before being composited.
     * 
     * @param red Float amount of <code>red</code>
     * @param green Float amount of <code>green</code>
     * @param blue Float amount of <code>blue</code>
     * @param alpha Float amount of <code>alpha</code>
     * @return 32-bit Integer representation of given color values
     */
    public static int compositeInt(float red, float green, float blue, float alpha) {
        return
                (( Math.round( Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f ) & 0xFF) << 24) |
                (( Math.round( Math.max(0.0f, Math.min(1.0f, red))   * 255.0f ) & 0xFF) << 16) | 
                (( Math.round( Math.max(0.0f, Math.min(1.0f, green)) * 255.0f ) & 0xFF) << 8) |
                ( Math.round( Math.max(0.0f, Math.min(1.0f, blue))  * 255.0f ) & 0xFF);
    }

    /**
     * Extracts an alpha value from a 32-bit integer representation of a color. Method assumes that the integer
     * representation stores the alpha value in bits 24 to 31.
     * 
     * @param value Integer color <code>value</code>
     * @return Alpha value between 0.0f and 1.0f
     */
    public static float alphaFromInt(int value) {
        return (0xFF & (value >> 24)) / 255.0f;
    }

    /**
     * Extracts a red value from a 32-bit integer representation of a color. Method assumes that the integer
     * representation stores the red value in bits 16 to 23.
     *
     * @param value Integer color <code>value</code>
     * @return Red value between 0.0f and 1.0f
     */
    public static float redFromInt(int value) {
        return (0xFF & (value >> 16)) / 255.0f;
    }

    /**
     * Extracts a green value from a 32-bit integer representation of a color. Method assumes that the integer
     * representation stores the green value in bits 8 to 15.
     *
     * @param value Integer color <code>value</code>
     * @return Green value between 0.0f and 1.0f
     */
    public static float greenFromInt(int value) {
        return (0xFF & (value >> 8)) / 255.0f;
    }

    /**
     * Extracts a blue value from a 32-bit integer representation of a color. Method assumes that the integer
     * representation stores the blue value in bits 0 to 7.
     *
     * @param value Integer color <code>value</code>
     * @return Blue value between 0.0f and 1.0f
     */
    public static float blueFromInt(int value) {
        return (0xFF & (value)) / 255.0f;
    }

    /**
     * Internal method to clamp a value between 0.0f and 1.0f.
     * 
     * @param val Float value to clamp
     * @return Clamped value
     */
    protected static float clamp(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }



    /**
     * Red component value of this Color, between 0.0f and 1.0f.
     */
    public final float red;

    /**
     * Green component value of this Color, between 0.0f and 1.0f.
     */
    public final float green;

    /**
     * Blue component value of this Color, between 0.0f and 1.0f.
     */
    public final float blue;

    /**
     * Alpha component value of this Color, between 0.0f and 1.0f.
     */
    public final float alpha;

    /**
     * 32-bit Integer composite of the ARGB values. Bits 0-7 are <code>blue</code>, 8-15 are <code>green</code>,
     * 16-23 are <code>red</code>, and 24-31 are <code>alpha</code>.
     */
    public final int composite;
    
    

    /**
     * Default Constructor. Constructs a new Color with a default value equivalent of black. IE, full alpha and no red,
     * blue, or green components.
     */
    public Color() {
        this.alpha  = 1.0f;
        this.red    = 0.0f;
        this.green  = 0.0f;
        this.blue   = 0.0f;
        this.composite = compositeInt(this.red, this.green, this.blue, this.alpha);
    }

    /**
     * Simple Constructor. Constructs a new Color with given <code>red</code>, <code>green</code>, and <code>blue</code>
     * values and full alpha. Will clamp values between 0.0f and 1.0f.
     * 
     * @param red Float amount of <code>red</code> to have
     * @param green Float amount of <code>green</code> to have
     * @param blue Float amount of <code>blue</code> to have
     */
    public Color(float red, float green, float blue) {
        this.alpha  = 1.0f;
        this.red    = clamp(red);
        this.green  = clamp(green);
        this.blue   = clamp(blue);
        this.composite = compositeInt(this.red, this.green, this.blue, this.alpha);
    }

    /**
     * Full Constructor. Constructs a new Color with given <code>red</code>, <code>green</code>, <code>blue</code>, and
     * <code>alpha</code> vlues. Will clamp values between 0.0f, and 1.0f.
     * 
     * @param red Float amount of <code>red</code> to have
     * @param green Float amount of <code>green</code> to have
     * @param blue Float amount of <code>blue</code> to have
     * @param alpha Float amount of <code>alpha</code> to have
     */
    public Color(float red, float green, float blue, float alpha) {
        this.alpha  = clamp(alpha);
        this.red    = clamp(red);
        this.green  = clamp(green);
        this.blue   = clamp(blue);
        this.composite = compositeInt(this.red, this.green, this.blue, this.alpha);
    }

    /**
     * Integer constructor. Constructs a new Color from the given 32-bit integer <code>color</code> representation.
     * 
     * @param color 32-bit Integer representation of a <code>color</code>
     */
    public Color(int color) {
        this.alpha  = clamp(alphaFromInt(color));
        this.red    = clamp(redFromInt(color));
        this.green  = clamp(greenFromInt(color));
        this.blue   = clamp(blueFromInt(color));
        this.composite = compositeInt(this.red, this.green, this.blue, this.alpha);
    }

    /**
     * Vector3f conversion constructor. Constructs a new Color from the given Vector3f object, interpreting its
     * <code>x</code>, <code>y</code> and <code>z</code> values as <code>red</code>, <code>green</code> and
     * <code>blue</code> values, respectively.
     *
     * @param vector Vector3f representation of a Color
     */
    public Color(Vector3f vector) {
        this(vector.x, vector.y, vector.z);
    }

    /**
     * Vector4f conversion constructor. Constructs a new Color from the given Vector4f object, interpreting its
     * <code>x</code>, <code>y</code>, <code>z</code> and <code>w</code> values as <code>red</code>, <code>green</code>,
     * <code>blue</code> and <code>alpha</code> values, respectively.
     *
     * @param vector Vector4f representation of a Color
     */
    public Color(Vector4f vector) {
        this(vector.x, vector.y, vector.z, vector.w);
    }

    /**
     * Vector3f converter. Creates a new Vector3f object out of the <code>red</code>, <code>green</code>, and
     * <code>blue</code> values of this Color.
     *
     * @return Vector3f representation of this Color
     */
    public Vector3f toVec3() {
        return new Vector3f(red, green, blue);
    }

    /**
     * Vector4f converter. Creates a new Vector4f object out of the <code>red</code>, <code>green</code>,
     * <code>blue</code>, and <code>alpha</code> values of this Color.
     *
     * @return Vector4f representation of this Color
     */
    public Vector4f toVec4() {
        return new Vector4f(red, green, blue, alpha);
    }


    
    /**
     * Multiply this Color's values by a given <code>multiplier</code>. Returns the result in a new Color object.
     * 
     * @param multiplier Float value to multiply by
     * @param multiplyAlpha True to additionally multiply the <code>alpha</code>, otherwise <code>alpha</code> is the same as the original
     * @return Newly created Color object with multiplied values
     */
    public Color multiply(float multiplier, boolean multiplyAlpha) {
        return new Color(
                red*multiplier,
                green*multiplier,
                blue*multiplier,
                multiplyAlpha ? alpha*multiplier : alpha
        );
    }

    /**
     * Multiply this Color's values by a given <code>multiplier</code>. Returns the result in a new Color object. Does
     * not multiply the <code>alpha</code> value, but instead passes the original to the new object.
     *
     * @param multiplier Float value to multiply by
     * @return Newly created Color object with multiplied values
     */
    public Color multiply(float multiplier) {
        return multiply(multiplier, false);
    }

    /**
     * Blends this Color with another using the specified blending algorithm.
     * 
     * @param input Color to blend with
     * @param top Whether or not this Color is to be used as the 'top' layer or 'bottom' layer
     * @param method BlendMethod representing the blending algorithm to use
     * @return Color result of blending this Color with the <code>input</code> color
     */
    public Color blend(Color input, boolean top, BlendMethod method) {
        return method.apply(
                top ? input : this,
                top ? this : input
        );
    }

    /**
     * Blends this Color with another using the specified blending algorithm. Uses this Color as the 'bottom' layer.
     * 
     * @param input Color to blend with
     * @param method BlendMethod representing the blending algorithm to use
     * @return Color result of blending this Color with the <code>input</code> color
     */
    public Color blend(Color input, BlendMethod method) {
        return method.apply(this, input);
    }


    
    /**
     * Static Overlay blending algorithm. Anonymous BlendMethod class representing an Overlay blending algorithm.
     */
    public static final BlendMethod BLEND_OVERLAY = new BlendMethod() {
        /**
         * Method to blend two Colors together. Applies an Overlay blending algorithm to two Colors, resulting in a new
         * Color.
         *
         * @param bottom Color that is 'below' the other Color
         * @param top Color that is 'above' the other Color
         * @return Result of blending the two Colors together
         */
        @Override public Color apply(Color bottom, Color top) {
            
            float red   = (bottom.red < 0.5f)   ? (2.0f * bottom.red   * top.red)   : (1.0f - 2.0f * (1.0f - bottom.red)   * (1.0f - top.red));
            float green = (bottom.green < 0.5f) ? (2.0f * bottom.green * top.green) : (1.0f - 2.0f * (1.0f - bottom.green) * (1.0f - top.green));
            float blue  = (bottom.blue < 0.5f)  ? (2.0f * bottom.blue  * top.blue)  : (1.0f - 2.0f * (1.0f - bottom.blue)  * (1.0f - top.blue));

            return new Color(red, green, blue, bottom.alpha);
        }
    };

    /**
     * Static Overlay blending algorithm. Anonymous BlendMethod class representing an Overlay blending algorithm. This
     * specific Overlay blending algorithm applies a darken filter to values that are originally too bright, in order
     * to avoid a 'whiteout' effect with brighter colors.
     */
    public static final BlendMethod BLEND_OVERLAY_ARTIFICAL_DARKEN = new BlendMethod() {
        /**
         * Method to blend two Colors together. Applies an Overlay blending algorithm to two Colors, resulting in a new
         * Color.
         *
         * @param bottom Color that is 'below' the other Color
         * @param top Color that is 'above' the other Color
         * @return Result of blending the two Colors together
         */
        @Override public Color apply(Color bottom, Color top) {
            
            float red = bottom.red;
            float green = bottom.green;
            float blue = bottom.blue;
            
            float avg = (red + green + blue) / 3.0f;
            if(avg > 0.9f) {
                red *= 0.7f;
                green *= 0.7f;
                blue *= 0.7f;
            }

            red   = (red < 0.5f)   ? (2.0f * red   * top.red)   : (1.0f - 2.0f * (1.0f - red)   * (1.0f - top.red));
            green = (green < 0.5f) ? (2.0f * green * top.green) : (1.0f - 2.0f * (1.0f - green) * (1.0f - top.green));
            blue  = (blue < 0.5f)  ? (2.0f * blue  * top.blue)  : (1.0f - 2.0f * (1.0f - blue)  * (1.0f - top.blue));

            return new Color(red, green, blue, bottom.alpha);
        }
    };
    
    
    
    /**
     * Interface to represent a color blending algorithm. Is lambda compliant.
     */
    public interface BlendMethod {

        /**
         * Method to blend two Colors together. Applies a specific blending algorithm to two Colors, resulting in a new
         * Color.
         * 
         * @param bottom Color that is 'below' the other Color
         * @param top Color that is 'above' the other Color
         * @return Result of blending the two Colors together
         */
        Color apply(Color bottom, Color top);
    }

    @Override
    public int compareTo(Color other) {
        int hash_this = this.hashCode();
        int hash_other = other.hashCode();
        if(hash_this < hash_other) return -1;
        else if(hash_this > hash_other) return 1;
        else return 0;
    }

    @Override
    public int hashCode() {
        return composite;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Color && hashCode() == other.hashCode();
    }
}