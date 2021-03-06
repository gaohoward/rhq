package org.rhq.enterprise.gui.configuration.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.group.ResourceGroup;
import org.rhq.core.gui.configuration.propset.ConfigurationSet;
import org.rhq.core.gui.configuration.propset.ConfigurationSetMember;
import org.rhq.enterprise.server.util.LookupUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper methods shared by various group Resource config managed beans.
 *
 * @author Ian Springer
 */
public class GroupResourceConfigurationUtility {
    private static final Log LOG = LogFactory.getLog(GroupResourceConfigurationUtility.class);

    public static ConfigurationSet buildConfigurationSet(Subject subject, ResourceGroup group,
        Map<Integer, Configuration> configs) {
        LOG.info("Calculating group config for " + group + "...");
        long startTime = System.currentTimeMillis();
        List<ConfigurationSetMember> configurationSetMembers = new ArrayList<ConfigurationSetMember>(configs.size());
        for (Integer resourceId : configs.keySet()) {
            String label = GroupResourceConfigurationUtility.createResourceHierarchyLabel(resourceId);
            Configuration configuration = configs.get(resourceId);
            ConfigurationSetMember configurationSetMember = new ConfigurationSetMember(label, configuration);
            configurationSetMembers.add(configurationSetMember);
        }
        ConfigurationDefinition definition = getConfigurationDefinition(subject, group);
        ConfigurationSet configurationSet = new ConfigurationSet(definition, configurationSetMembers);
        long elapsedTime = System.currentTimeMillis() - startTime;
        LOG.info("Calculated group config in " + elapsedTime + " ms.");
        return configurationSet;
    }

    private static String createResourceHierarchyLabel(Integer resourceId) {
        List<Resource> resourceLineage = LookupUtil.getResourceManager().getResourceLineage(resourceId);
        String previousName = resourceLineage.get(0).getName();
        StringBuilder label = new StringBuilder(previousName);
        for (int i = 1; i < resourceLineage.size(); i++) {
            Resource resource = resourceLineage.get(i);
            String name = resource.getName();
            name = (name.startsWith(previousName)) ? name.substring(previousName.length()) : name;
            label.append(" > ").append(name);
        }
        return label.toString();
    }

    public static ConfigurationDefinition getConfigurationDefinition(Subject subject, ResourceGroup group) {
        Integer resourceTypeId = group.getResourceType().getId();
        ConfigurationDefinition configurationDefinition = LookupUtil.getConfigurationManager()
            .getResourceConfigurationDefinitionForResourceType(subject, resourceTypeId);
        return configurationDefinition;
    }
}
