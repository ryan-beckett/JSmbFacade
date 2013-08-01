import jcifs.smb.NtlmPasswordAuthentication;

/**
 * 
 * Runs the SMB directory notifier. Specify the SMB parameters as command-line
 * arguments.
 * 
 * @author Ryan Beckett
 * 
 */
public class SmbNotifier {

    private SmbNotification notifier;
    private NtlmPasswordAuthentication auth;
    private NotificationHandler handler;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out
                    .println("Usage: java SmbNotifier \"<smb url>\""
                            + " \"<domain>\"" + " \"<login name>\""
                            + " \"<password>\"");
            System.exit(1);
        }
        new SmbNotifier().run(args[0], args[1], args[2], args[3]);
    }

    public void run(final String url, String domain, String user, String pass) {
        String userInfo = domain + ";" + user + ":" + pass;
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
        try {
            notifier = new SmbNotification(url, auth, handler);
            notifier.listen(500);
            while (true)
                ;
            // modify the SMB directory and watch the console!!
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
