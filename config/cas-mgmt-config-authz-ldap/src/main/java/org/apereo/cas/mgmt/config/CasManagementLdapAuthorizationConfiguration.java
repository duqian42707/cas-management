package org.apereo.cas.mgmt.config;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authorization.LdapUserAttributesToRolesAuthorizationGenerator;
import org.apereo.cas.authorization.LdapUserGroupsToRolesAuthorizationGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasManagementConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.SearchOperation;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This is {@link CasManagementLdapAuthorizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties({CasConfigurationProperties.class, CasManagementConfigurationProperties.class})
public class CasManagementLdapAuthorizationConfiguration {

    private static SearchOperation ldapAuthorizationGeneratorUserSearchExecutor(final CasManagementConfigurationProperties managementProperties) {
        val ldapAuthz = managementProperties.getLdap().getLdapAuthz();
        val operation = LdapUtils.newLdaptiveSearchOperation(ldapAuthz.getBaseDn(), ldapAuthz.getSearchFilter(),
            new ArrayList<>(0), CollectionUtils.wrap(ldapAuthz.getRoleAttribute()));
        operation.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(managementProperties.getLdap()));
        return operation;
    }

    private static SearchOperation ldapAuthorizationGeneratorGroupSearchExecutor(final CasManagementConfigurationProperties managementProperties) {
        val ldapAuthz = managementProperties.getLdap().getLdapAuthz();
        val operation = LdapUtils.newLdaptiveSearchOperation(ldapAuthz.getGroupBaseDn(), ldapAuthz.getGroupFilter(),
            new ArrayList<>(0), CollectionUtils.wrap(ldapAuthz.getGroupAttribute()));
        operation.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(managementProperties.getLdap()));
        return operation;
    }

    @Bean
    public AuthorizationGenerator authorizationGenerator(final CasManagementConfigurationProperties managementProperties) {
        val ldapAuthz = managementProperties.getLdap().getLdapAuthz();
        var generator = StringUtils.isNotBlank(ldapAuthz.getGroupFilter()) && StringUtils.isNotBlank(ldapAuthz.getGroupAttribute())
            ? new LdapUserGroupsToRolesAuthorizationGenerator(
            ldapAuthorizationGeneratorUserSearchExecutor(managementProperties),
            ldapAuthz.isAllowMultipleResults(),
            ldapAuthz.getGroupAttribute(),
            ldapAuthz.getGroupPrefix(),
            ldapAuthorizationGeneratorGroupSearchExecutor(managementProperties))
            : new LdapUserAttributesToRolesAuthorizationGenerator(
            ldapAuthorizationGeneratorUserSearchExecutor(managementProperties),
            ldapAuthz.isAllowMultipleResults(),
            ldapAuthz.getRoleAttribute(),
            ldapAuthz.getRolePrefix());
        return (callContext, userProfile) -> {
            val result = generator.apply((Principal) userProfile::getId);
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(userProfile);
        };
    }
}
