package org.cloudfoundry.autosleep.access.dao.model;

import org.cloudfoundry.autosleep.config.Config;
import org.junit.Test;

import java.time.Duration;
import java.util.regex.Pattern;


public class OrgEnrollmentConfigTest {

    private static final Duration duration = Duration.parse("PT2M");


    @Test
    public void supports_default_orgs_enrollment_use_cases_from_environment_variables() {
        //Given autosleep configured with default,
        //when a new orgs gets scanned
        //then it gets autoenrolled
        OrgEnrollmentConfig orgEnrollmentConfig = OrgEnrollmentConfig.builder().organizationGuid("default-org-guid")
                .state(Config.OrgEnrollmentParameters.EnrollmentState.auto_enrolled)
                .idleDuration(aDefaultDuration())
                .defaultAutoEnrollment(Config.ServiceInstanceParameters.Enrollment.forced).build();

        //and when a new space gets auto-enrolled in this org

        //then generated space enrollment config is
        SpaceEnrollerConfig.builder()
                .forcedAutoEnrollment(true)
                .idleDuration(aDefaultDuration()).build();
    }

    @Test
    public void supports_enrolling_an_org_excluded_by_default() {
        //given an org is not autoenrolled from config

        //when receiving an enrollement request from API

        //then resulting org config is
        OrgEnrollmentConfig optedOutOrgConfig = OrgEnrollmentConfig.builder().organizationGuid("trial-non-payong-org-guid")
                .state(Config.OrgEnrollmentParameters.EnrollmentState.backoffice_enrolled)
                .defaultAutoEnrollment(Config.ServiceInstanceParameters.Enrollment.forced).build();//will be transient-opt-outs soon
    }


    @Test
    public void supports_sequential_optin_optouts() {
        //given an org enrolled from API
        OrgEnrollmentConfig orgEnrollmentConfig = OrgEnrollmentConfig.builder().organizationGuid("trial-non-payong-org-guid")
                .state(Config.OrgEnrollmentParameters.EnrollmentState.backoffice_enrolled)
                .defaultAutoEnrollment(Config.ServiceInstanceParameters.Enrollment.forced) //will be transient-opt-outs soon
                .build();

        //when API request opt it out
        orgEnrollmentConfig.setState(Config.OrgEnrollmentParameters.EnrollmentState.backoffice_opted_out);
        //then new spaces won't be enrolled anymore

        //and when API request opt it in
        orgEnrollmentConfig.setState(Config.OrgEnrollmentParameters.EnrollmentState.backoffice_enrolled);
        //then new spaces will be automatically enrolled
    }


    @Test
    public void supports_private_service_provider_use_case() {
        //given the service service provider desire to leave app teams possibly to opt-outs
        OrgEnrollmentConfig.builder().organizationGuid("org-for-division-1")
                .state(Config.OrgEnrollmentParameters.EnrollmentState.backoffice_enrolled)
                .excludeSpaceFromAutoEnrollment(Pattern.compile(".*-prod"))
                .defaultAutoEnrollment(Config.ServiceInstanceParameters.Enrollment.standard);

        //when a new space gets auto-enrolled in this org

        //then generated space enrollment config is
        SpaceEnrollerConfig.builder()
                .forcedAutoEnrollment(false)
                .build();
    }

    @Test
    public void supports_free_tier_public_service_provider_use_case() {
        //given the desired of a public free tier operator to assign a transient-opts-outs default value.

        OrgEnrollmentConfig.builder().organizationGuid("a-free-tier-org-guid")
                .excludeSpaceFromAutoEnrollment(Pattern.compile(".*-premium"))
                .defaultAutoEnrollment(Config.ServiceInstanceParameters.Enrollment.forced); //will be transient-opt-outs soon

        //when a new space gets auto-enrolled in this org

        //then generated space enrollment config is
        SpaceEnrollerConfig.builder()
                .forcedAutoEnrollment(true)
                .build();
    }

    private Duration aDefaultDuration() {
        return duration;
    }
}