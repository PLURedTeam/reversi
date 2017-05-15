package plu.red.reversi.core.util;

import com.vdurmont.emoji.EmojiParser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Glory to the Red Team.
 *
 * Object representing a single chat message. Stores data such as timestamps, username that sent it, and the actual
 * message. Also contains methods for serializing to and from JSON.
 */
public class ChatMessage implements Comparable<ChatMessage> {

    // Register JSON Converter
    static {
        DataMap.Setting.registerConverter(ChatMessage.class,
                (key, value, json) -> json.put(key, value.toJSON()),
                (key, json) -> ChatMessage.fromJSON(json.getJSONObject(key)));
    }

    /**
     * Not instantiable static container class that stores channel id's and prefixes.
     */
    public static final class Channel {

        // Can't create a Channel object
        private Channel() {}

        public static final String GLOBAL = "global";
        public static final String LOBBY_PREFIX = "lobby_";
        public static final String GAME_PREFIX = "game_";

        public static String lobby(String id) { return LOBBY_PREFIX + id; }
        public static String game(String id) { return GAME_PREFIX + id; }

    }

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public final String message;
    public final Date timestamp;

    public final String channel;

    // TODO: When user accounts are implemented, possibly change this to reflect an account instead of a single name
    public final String username;
    public final Color usercolor;

    /**
     * Create a blank ChatMesssage object. The <code>username</code> and <code>message</code> is defaulted to blank,
     * <code>channel</code> is defaulted to GLOBAL, <code>usercolor</code> is defaulted to BLACK, and a
     * <code>timestamp</code> is generated.
     */
    public ChatMessage() {
        this.message = "";
        this.channel = Channel.GLOBAL;
        this.usercolor = Color.BLACK;
        this.username = "";
        this.timestamp = new Date();
    }

    /**
     * Create a new ChatMessage object. The player name color is defaulted to black, and a timestamp is generated on
     * creation.
     *
     * @param channel String ID of the channel this ChatMessage belongs to
     * @param username Name of the person who sent this ChatMessage
     * @param message Contents of the ChatMessage
     */
    public ChatMessage(String channel, String username, String message) {
        this.channel = channel;
        this.username = username;
        this.usercolor = Color.BLACK;
        this.message = EmojiParser.parseToUnicode(message);
        this.timestamp = new Date();
    }

    /**
     * Create a new ChatMessage object. A timestamp is generated on creation.
     *
     * @param channel String ID of the channel this ChatMessage belongs to
     * @param username Name of the person who sent this ChatMessage
     * @param usercolor Color to use for the displayed username
     * @param message Contents of the ChatMessage
     */
    public ChatMessage(String channel, String username, Color usercolor, String message) {
        this.channel = channel;
        this.username = username;
        this.usercolor = usercolor;
        this.message = EmojiParser.parseToUnicode(message);
        this.timestamp = new Date();
    }

    /**
     * Create a new ChatMessage object.
     *
     * @param channel String ID of the channel this ChatMessage belongs to
     * @param timestamp Timestamp of when this ChatMessage was created/sent
     * @param username Name of the person who sent this ChatMessage
     * @param usercolor Color to use for the displayed username
     * @param message Contents of the ChatMessage
     */
    public ChatMessage(String channel, Date timestamp, String username, Color usercolor, String message) {
        this.channel = channel;
        this.username = username;
        this.usercolor = usercolor;
        this.message = EmojiParser.parseToUnicode(message);
        this.timestamp = new Date();
    }

    /**
     * Creates a string representing this ChatMessage's timestamp.
     *
     * @return String version of <code>timestamp</code>
     */
    public String getTimeString() {
        return dateFormat.format(timestamp);
    }

    @Override
    public String toString() {
        return "[" + dateFormat.format(timestamp) + "] " + username + ": " + message;
    }

    /**
     * Creates a version of toString() with HTML tags for changing the color of the displayed username.
     *
     * @return HTML formatted String representation
     */
    public String toHTMLString() {
        return "<html>[" + getTimeString() + "] <font color=rgb("
                + usercolor.red + "," + usercolor.green + "," + usercolor.blue + ")>" + username
                + "</font>: " + message + "</html>";
    }

    @Override
    public int hashCode() {
        return timestamp.hashCode();
    }

    @Override
    public int compareTo(ChatMessage other) {
        int c = timestamp.compareTo(other.timestamp);
        if(c == 0) return message.compareTo(other.message);
        else return c;
    }

    /**
     * Serializes this ChatMessage into a JSONObject.
     *
     * @return New JSONObject from this ChatMessage
     * @throws JSONException if there is a problem during serialization
     */
    public JSONObject toJSON() throws JSONException {

        // Serialize data
        JSONObject json = new JSONObject();
        json.put("channel", channel);
        json.put("username", username);
        json.put("usercolor", usercolor.composite);
        json.put("message", message);

        Calendar cal = new GregorianCalendar();

        json.put("timestamp", timestamp.getTime());

        return json;
    }

    /**
     * De-Serializes a JSONObject into a ChatMessage.
     *
     * @param json JSONObject to de-serialize
     * @return New ChatMessage from the JSONObject
     * @throws JSONException if there is a problem during de-serialization
     */
    public static ChatMessage fromJSON(JSONObject json) throws JSONException {

        // De-Serialize data
        String channel = json.getString("channel");
        String username = json.getString("username");
        Color usercolor = new Color(json.getInt("usercolor"));
        String message = json.getString("message");
        Date timestamp = new Date(json.getInt("timestamp"));

        return new ChatMessage(channel, timestamp, username, usercolor, message);
    }
}
