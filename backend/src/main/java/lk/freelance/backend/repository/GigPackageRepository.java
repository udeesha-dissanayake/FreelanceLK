package lk.freelance.backend.repository;

import lk.freelance.backend.entity.GigPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface GigPackageRepository extends JpaRepository<GigPackage, UUID> {
    // FIX: Add this method
    void deleteAllByGig_GigId(UUID gigId);
}