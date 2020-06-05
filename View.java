import java.awt.event.ActionListener;

public interface View extends ActionListener {
    /**
     * Register argument as observer/listener of this; this must be done first,
     * before any other methods of this class are called.
     * 
     * @param c
     *            client to register
     */
    void registerObserver(FileClient c);
}