package org.apereo.cas.mgmt.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasManagementConfigurationProperties;
import org.apereo.cas.mgmt.PendingRequests;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.apereo.cas.mgmt.controller.DelegatedUtil;
import org.apereo.cas.mgmt.controller.EmailManager;
import org.apereo.cas.mgmt.controller.NoteController;
import org.apereo.cas.mgmt.controller.PullController;
import org.apereo.cas.mgmt.controller.SubmitController;
import org.apereo.cas.mgmt.factory.RepositoryFactory;
import org.apereo.cas.notifications.CommunicationsManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

/**
 * Configuration class for version control.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Configuration("casManagementDelegatedConfiguration")
@EnableConfigurationProperties({CasConfigurationProperties.class, CasManagementConfigurationProperties.class})
@Slf4j
public class CasManagementDelegatedConfiguration {

    @Autowired
    private CasManagementConfigurationProperties managementProperties;

    @Autowired
    private ObjectProvider<RepositoryFactory> repositoryFactory;

    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    @Qualifier("mailSender")
    private ObjectProvider<JavaMailSender> mailSender;


    @Bean
    @ConditionalOnProperty(prefix = "mgmt.delegated", name = "enabled", havingValue = "true")
    public SubmitController submitController() {
        return new SubmitController(repositoryFactory.getIfAvailable(),
                managementProperties, communicationsManager.getIfAvailable());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mgmt.delegated", name = "enabled", havingValue = "true")
    public PullController pullController() {
        return new PullController(repositoryFactory.getIfAvailable(),
                managementProperties, communicationsManager.getIfAvailable());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mgmt.delegated", name = "enabled", havingValue = "true")
    public NoteController noteController() {
        return new NoteController(repositoryFactory.getIfAvailable());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mgmt.delegated", name = "enabled", havingValue = "true")

    public EmailManager emailManager() {
        return new EmailManager(mailSender.getIfAvailable());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mgmt.delegated", name = "enabled", havingValue = "true")
    public PendingRequests pendingRequests() {
        return authentication -> {
            val user = Objects.requireNonNull(CasUserProfile.from(authentication));
            if (user.isAdministrator()) {
                val git = Objects.requireNonNull(repositoryFactory.getIfAvailable()).masterRepository();
                try {
                    return (int) git.branches()
                            .map(git::mapBranches)
                            .filter(r -> DelegatedUtil.filterPulls(r, new boolean[]{true, false, false}))
                            .count();
                } catch (final Exception e) {
                    return 0;
                }
            }
            return 0;
        };
    }
}
