package org.neov.unicorn.server.repository;

import org.neov.unicorn.common.models.SchedulerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<SchedulerSettings, Long> {
}
