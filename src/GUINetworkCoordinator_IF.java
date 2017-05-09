public interface GUINetworkCoordinator_IF
{
    public void connectionStatus(boolean established, boolean usernameOk);
    public void uploadStarted(String user, String filename);
    public void IOexception();
    public void downloadPercentComplete(String username, String filename, double percent);
    public void uploadPercentComplete(String username, String filename, double percent);
}
