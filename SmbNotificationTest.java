import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * Unit tests for the {@link SmbNotification} API.
 * 
 * @author Ryan Beckett
 */
public class SmbNotificationTest {

    /* Note: If your running unit tests these strings need valid values. */

    private final String url = "smb://host/dir/"; //directories must end with '/'
    private final String domain = "Your WORKGROUP name";
    private final String user = "Your username";
    private final String pass = "Your password";
    private NtlmPasswordAuthentication auth;
    private NotificationHandler handler;

    @Before
    public final void setUp() {
        String userInfo = domain+";"+user+":"+pass;
        auth = new NtlmPasswordAuthentication(userInfo);
        handler = new NotificationHandler() {

            @Override
            public void handleNewFile(String file) {
                System.out.println(file + " added.");
            }

            @Override
            public void handleDeletedFile(String file) {
                System.out.println(file + " deleted.");
            }

        };
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testNullAuthentication() throws Exception {
        new SmbNotification(url, null, handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testNullHandler() throws Exception {
        new SmbNotification(url, auth, null);
    }

    @Test(expected = MalformedURLException.class)
    public final void testNullDirURL() throws Exception {
        new SmbNotification(null, auth, handler);
    }

    @Test(expected = MalformedURLException.class)
    public final void testMalformedDirURL() throws Exception {
        new SmbNotification("smb//", auth, handler);
    }

    @Test(expected = SmbException.class)
    public final void testInvalidAuthenticationCredentials() throws Exception {
        String invalidUser = "invalidUser";
        String invalidPass = "invalidPass";
        
        String userInfo = domain+";"+invalidUser+":"+pass;
        NtlmPasswordAuthentication invalidAuth = new NtlmPasswordAuthentication(userInfo);
        new SmbNotification(url, invalidAuth, handler);
        
        userInfo = domain+";"+user+":"+invalidPass;
        invalidAuth = new NtlmPasswordAuthentication(userInfo);
        new SmbNotification(url, invalidAuth, handler);
    }

    @Test(expected = IllegalStateException.class)
    public final void testIllegalListenAfterClose() throws Exception {
        SmbNotification notifier = new SmbNotification(url, auth, handler);
        notifier.listen(500);
        notifier.stop();
        notifier.listen(500);
    }

    @Test
    public final void testConsecutiveCallsToListen() throws Exception {
        SmbNotification notifier = new SmbNotification(url, auth, handler);
        assertEquals(new Boolean(true), new Boolean(notifier.listen(500)));
        assertEquals(new Boolean(false), new Boolean(notifier.listen(500)));
        notifier.stop();
    }

    @Test
    public final void testNotificationTermination() throws Exception {
        SmbNotification notifier = new SmbNotification(url, auth, handler);
        assertEquals(new Boolean(false), new Boolean(notifier.isRunning()));
        notifier.listen(500);
        assertEquals(new Boolean(true), new Boolean(notifier.isRunning()));
        notifier.stop();
        assertEquals(new Boolean(false), new Boolean(notifier.isRunning()));
    }
}
