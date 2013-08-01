/**
 * 
 * Defines a default handler for notification callbacks.
 * 
 * @author Ryan Beckett
 * @version 1.0
 * 
 */
public abstract class NotificationHandler {

    /**
     * A callback for the addition of a file in the watched directory.
     * 
     * @param file
     *            The relative file name of the newly created file.
     */
    public abstract void handleNewFile(String file);

    /**
     * A callback for the deletion of a file in the watched directory.
     * 
     * @param file
     *            The relative file name of the deleted file.
     */
    public abstract void handleDeletedFile(String file);
}
