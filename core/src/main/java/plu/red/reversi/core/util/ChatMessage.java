package plu.red.reversi.core.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

    public final String message;
    public final ZonedDateTime timestamp;

    public final String channel;

    // TODO: When user accounts are implemented, possibly change this to reflect an account instead of a single name
    public final String username;
    public final Color usercolor;

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
        this.message = message;
        this.timestamp = ZonedDateTime.now();
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
        this.message = message;
        this.timestamp = ZonedDateTime.now();
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
    public ChatMessage(String channel, ZonedDateTime timestamp, String username, Color usercolor, String message) {
        this.channel = channel;
        this.username = username;
        this.usercolor = usercolor;
        this.message = message;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + username + ": " + message;
    }

    /**
     * Creates a version of toString() with HTML tags for changing the color of the displayed username.
     *
     * @return HTML formatted String representation
     */
    public String toHTMLString() {
        return "<html>[" + timestamp.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] <font color=rgb("
                + usercolor.getRed() + "," + usercolor.getGreen() + "," + usercolor.getBlue() + ")>" + username
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
        json.put("usercolor", usercolor.getRGB());
        json.put("message", message);

        // Serialize timestamp
        JSONObject time = new JSONObject();
        time.put("year", timestamp.getYear());
        time.put("month", timestamp.getMonthValue());
        time.put("day", timestamp.getDayOfMonth());
        time.put("hour", timestamp.getHour());
        time.put("minute", timestamp.getMinute());
        time.put("second", timestamp.getSecond());
        time.put("nano", timestamp.getNano());
        time.put("zone", timestamp.getZone().getId());
        json.put("timestamp", time);

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

        // De-Serialize timestamp
        JSONObject tObj = json.getJSONObject("timestamp");
        ZonedDateTime timestamp = ZonedDateTime.of(
                tObj.getInt("year"),
                tObj.getInt("month"),
                tObj.getInt("day"),
                tObj.getInt("hour"),
                tObj.getInt("minute"),
                tObj.getInt("second"),
                tObj.getInt("nano"),
                ZoneId.of(tObj.getString("zone"))
        );

        return new ChatMessage(channel, timestamp, username, usercolor, message);
    }
}
