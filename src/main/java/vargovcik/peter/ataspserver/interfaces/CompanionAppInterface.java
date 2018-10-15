package vargovcik.peter.ataspserver.interfaces;

import vargovcik.peter.ataspserver.server.SearchMode;

public interface CompanionAppInterface {
    void stopSearch();
    void startSearch();
    void remoteControll(boolean isRemoteControlled);
    void remoteControlCommand(byte[] command);
    void connectionBroken();
    void connected();
    void platformMaxPower(int power);
    void ignoreProximity(boolean ignore);    
    void headLight(boolean on);
    void holdSearch(boolean searchOnHold);
    void rgbSet(boolean redIsOn,boolean greenIsOn,boolean blueIsOn);
    public void panTiltContollCommand(byte[] command);
    public void teardown();
    public void setSearchMode(SearchMode searchMode);

}
