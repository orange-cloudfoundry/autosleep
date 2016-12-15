package org.cloudfoundry.autosleep.access.dao.repositories;

import org.cloudfoundry.autosleep.access.dao.model.OrgEnrollmentConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrolledOrganizationConfigRepository extends JpaRepository<OrgEnrollmentConfig,String> {

}
