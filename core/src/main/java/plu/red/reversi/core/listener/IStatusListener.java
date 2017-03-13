package plu.red.reversi.core.listener;

/**
 * Glory to the Red Team.
 *
 * Interface for when a status message is produced.
 */
public interface IStatusListener {

    /**
     * Called when a status message is produced and passed around.
     *
     * @param message String representing the message
     */
    void onStatusMessage(String message);
}
