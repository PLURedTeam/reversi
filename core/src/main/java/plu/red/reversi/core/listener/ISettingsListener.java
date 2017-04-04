package plu.red.reversi.core.listener;

/**
 * Glory to the Red Team.
 *
 * Interface for when a setting has been changed.
 */
public interface ISettingsListener extends IListener {

    /**
     * Called when the client's settings have been changed.
     */
    void onClientSettingsChanged();
}
