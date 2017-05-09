public interface GUIFileManager_IF
{
    public void fileOpSuccess();
    public void fileNotFound();
    public void addNewFile(String user, String filename);
    public void removeFile(String user, String filename);
    public void updateFile(String user, String filename);
}