package org.rhq.enterprise.server.storage;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.common.composite.SystemSetting;
import org.rhq.core.domain.common.composite.SystemSettings;
import org.rhq.enterprise.server.system.SystemManagerLocal;

/**
 * @author John Sanda
 */
@Singleton
public class StorageClusterSettingsManagerBean {

    @EJB
    private SystemManagerLocal systemManager;

    public StorageClusterSettings getClusterSettings(Subject subject) {
        SystemSettings settings = systemManager.getSystemSettings(subject);
        Map<String, String> settingsMap = settings.toMap();
        StorageClusterSettings clusterSettings = new StorageClusterSettings();

        if (!settingsMap.containsKey(SystemSetting.STORAGE_CQL_PORT)) {
            return null;
        } else {
            clusterSettings.setCqlPort(Integer.parseInt(settingsMap.get(SystemSetting.STORAGE_CQL_PORT)));
        }

        if (!settingsMap.containsKey(SystemSetting.STORAGE_GOSSIP_PORT)) {
            return null;
        } else {
            clusterSettings.setGossipPort(Integer.parseInt(settingsMap.get(SystemSetting.STORAGE_GOSSIP_PORT)));
        }

        return clusterSettings;
    }

    public void setClusterSettings(Subject subject, StorageClusterSettings clusterSettings) {
        SystemSettings settings = new SystemSettings();
        settings.put(SystemSetting.STORAGE_CQL_PORT, Integer.toString(clusterSettings.getCqlPort()));
        settings.put(SystemSetting.STORAGE_GOSSIP_PORT, Integer.toString(clusterSettings.getGossipPort()));
        systemManager.setStorageClusterSettings(subject, settings);
    }

}
