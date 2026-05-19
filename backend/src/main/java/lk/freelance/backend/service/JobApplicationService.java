package lk.freelance.backend.service;

import lk.freelance.backend.dto.JobApplicationDTO;
import lk.freelance.backend.dto.JobApplicationRequest;

import java.util.List;
import java.util.UUID;

public interface JobApplicationService {

    // Freelancer applies to a job
    JobApplicationDTO apply(UUID freelancerId, UUID listingId, JobApplicationRequest request);

    // Freelancer withdraws their application
    JobApplicationDTO withdraw(UUID freelancerId, UUID applicationId);

    // Client views all applicants for their job
    List<JobApplicationDTO> getApplicants(UUID clientId, UUID listingId);

    // Client accepts a specific applicant
    JobApplicationDTO accept(UUID clientId, UUID applicationId);

    // Client rejects a specific applicant
    JobApplicationDTO reject(UUID clientId, UUID applicationId);

    // Freelancer views all their own applications
    List<JobApplicationDTO> getMyApplications(UUID freelancerId);
}
