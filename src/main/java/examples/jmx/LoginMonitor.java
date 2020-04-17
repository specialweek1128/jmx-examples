package examples.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;

public class LoginMonitor implements LoginMonitorMBean {

    public static final int DEFAULT_MAX_LOGIN_COUNT = 100;

    private final Map<Integer, CompositeData> loginUserInfoMap = new HashMap<>();

    private int maxLoginCount = DEFAULT_MAX_LOGIN_COUNT;

    private final List<Integer> loginLockList = new ArrayList<>();

    @Override
    public int getLoginCount() {
        return loginUserInfoMap.size();
    }

    @Override
    public CompositeData[] getLoginInfos() {
        return loginUserInfoMap.values().stream().toArray(CompositeData[]::new);
    }

    @Override
    public void addLoginInfo(CompositeData loginUserInfo) {
        synchronized (loginUserInfoMap) {
            int loginId = LoginUserInfo.getMBeanId(loginUserInfo);
            if (!loginUserInfoMap.containsKey(loginId)) {
                loginUserInfoMap.put(loginId, loginUserInfo);
            }
        }
    }

    @Override
    public void removeLoginInfo(int id) {
        synchronized (loginUserInfoMap) {
            if (loginUserInfoMap.containsKey(id)) {
                loginUserInfoMap.remove(id);
            }
        }
    }

    @Override
    public void resetLoginInfo() {
        synchronized (loginUserInfoMap) {
            loginUserInfoMap.clear();
        }
    }

    @Override
    public int getMaxLoginCount() {
        return maxLoginCount;
    }

    @Override
    public void setMaxLoginCount(int count) {
        this.maxLoginCount = count;
    }

    @Override
    public int[] getLoginLockIds() {
        int[] ret = new int[loginLockList.size()];
        for (int i = 0; i < loginLockList.size(); i++) {
            ret[i] = loginLockList.get(i).intValue();
        }
        return ret;
    }

    @Override
    public void addLoginLockId(int id) {
        synchronized (loginLockList) {
            if (!loginLockList.contains(id)) {
                loginLockList.add(id);
            }
        }
    }

    @Override
    public void removeLoginLockId(int id) {
        synchronized (loginLockList) {
            if (loginLockList.contains(id)) {
                loginLockList.remove(id);
            }
        }
    }

    @Override
    public void resetLoginLockId() {
        synchronized (loginLockList) {
            loginLockList.clear();
        }
    }

}
