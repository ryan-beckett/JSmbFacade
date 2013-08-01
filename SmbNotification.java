import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * 
 * A facade for receiving notifcations for SMB file modifications through the
 * JCIFS API.
 * 
 * This class provides notification for file creation and deletion in a SMB/CIFS
 * directory. A callback is produced on modification and the modified file name
 * is given to the default handler.
 * 
 * @author Ryan Beckett
 * @version 1.0
 * 
 */
public class SmbNotification {

    private NtlmPasswordAuthentication authentication;
    private NotificationHandler handler;
    private SmbFile smb;
    private List<String> files;
    private Logger logger;
    private boolean running;
    private boolean stopped;

    /**
     * Creates a new non-running notifier.
     * 
     * @param url
     *            An smb directory path of the form
     *            <code>smb://host/dir/</code>. See {@link SmbFile} for more
     *            information on constructing URLs.
     * 
     * @param authentication
     *            The authentication information.
     * 
     * @param handler
     *            A default handler for notification callbacks.
     * 
     * @throws IllegalArgumentException
     *             If <code>authentication</code> or <code>handler</code> are
     *             null.
     * 
     * @throws MalformedURLException
     *             If <code>dir</code> is a malformed URL.
     * 
     * @throws SmbException
     *             If an underlying communication error occurs.
     */
    public SmbNotification(String url, NtlmPasswordAuthentication auth,
            NotificationHandler handler) throws IllegalArgumentException,
            MalformedURLException, SmbException {
        super();
        if (auth == null)
            throw new IllegalArgumentException();
        this.authentication = auth;
        if (handler == null)
            throw new IllegalArgumentException();
        this.handler = handler;
        logger = Logger.getLogger("SmbNotification");
        files = new ArrayList<String>();
        connect(url);
        createFileList();
    }

    private void connect(String dir) throws MalformedURLException {
        smb = new SmbFile(dir, authentication);
    }

    private void createFileList() throws SmbException {
        for (String f : smb.list()) {
            files.add(f);
        }
    }

    /**
     * Listen for modifications to the directory. A new thread is started that
     * polls for modifications every <code>millis</code> milliseconds. If the
     * notifier is running already, a new thread will not be created.
     * 
     * @param millis
     *            The number of milliseconds to wait per poll.
     * 
     * @return Returns <code>true</code> if the notifier is started and a new
     *         thread is spawned; otherwise, returns <code>false</code> if the
     *         notifier is running already.
     * 
     * @throws IllegalStateException
     *             If <code>listen</code> is called after <code>close</code> is
     *             called.
     */
    public boolean listen(final long millis) {
        if (stopped)
            throw new IllegalStateException("Cannot restart the notifier.");
        if (running)
            return false;
        running = true;
        Thread t = new Thread(new Runnable() {

            public void run() {
                while (running) {
                    try {
                        checkForFileDeletion();
                        checkForNewFile();
                        Thread.sleep(millis);
                    } catch (SmbException e) {
                        log(e.getMessage(), Level.SEVERE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        return true;
    }

    private void checkForFileDeletion() throws SmbException {
        List<String> smbFiles = Arrays.asList(smb.list());
        String f = null;
        for (int i = 0; i < files.size(); i++)
            f = files.get(i);
        if (f != null && !smbFiles.contains(f)) {
            files.remove(f);
            handler.handleDeletedFile(f);
        }
    }

    private void checkForNewFile() throws SmbException {
        for (String f : smb.list()) {
            if (!files.contains(f)) {
                files.add(f);
                handler.handleNewFile(f);
            }
        }
    }

    /**
     * Stop listening for notifications. The polling thread is terminated. Once
     * this method is called, if notifier is started again, an exception is
     * thrown.
     */
    public void stop() {
        running = false;
        stopped = true;
    }

    private void log(String message, Level level) {
        logger.log(level, message);
    }

    /**
     * Check whether the notifier is running.
     * 
     * @return Returns <code>true</code> if the notifier is running; otherwise,
     *         returns <code>false</code>.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get the callback handler.
     * 
     * @return The handler.
     */
    public NotificationHandler getHandler() {
        return handler;
    }

    /**
     * Set the callback handler
     * 
     * @param handler
     *            The new handler.
     */
    public void setHandler(NotificationHandler handler) {
        this.handler = handler;
    }

    /**
     * Get the authentication.
     * 
     * @return The authentication.
     */
    public NtlmPasswordAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * Set the authentication. Setting a new authentication will have no
     * effect. Once the notifier has made the connection, the user session
     * will persist throughout the notifier's life cycle.
     * 
     * @param handler
     *            The new authentication.
     */
    public void setAuthentication(NtlmPasswordAuthentication authentication) {
        this.authentication = authentication;
    }

}
