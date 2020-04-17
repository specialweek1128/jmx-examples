package examples.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public interface LoginMonitorMBean {

    public static final String NAME = "examples.jmx:type=LoginMonitoring";

    public static final String LUI_ITEM_ID = "id";

    public static final String LUI_ITEM_NAME = "name";

    public int getLoginCount();

    public CompositeData[] getLoginInfos();

    void addLoginInfo(CompositeData loginUserInfo);

    public void removeLoginInfo(int id);

    public void resetLoginInfo();

    public int getMaxLoginCount();

    public void setMaxLoginCount(int count);

    public int[] getLoginLockIds();

    public void addLoginLockId(int id);

    public void removeLoginLockId(int id);

    public void resetLoginLockId();

    public static ObjectName createObjectName() {
        try {
            return new ObjectName(LoginMonitorMBean.NAME);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
