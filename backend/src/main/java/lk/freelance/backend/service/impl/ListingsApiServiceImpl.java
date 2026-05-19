package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.CreateListingGigRequest;
import lk.freelance.backend.dto.CreateListingJobRequest;
import lk.freelance.backend.dto.ListingResponseDTO;
import lk.freelance.backend.dto.PagedResponse;
import lk.freelance.backend.dto.UpdateListingRequest;
import lk.freelance.backend.entity.Gig;
import lk.freelance.backend.entity.Listing;
import lk.freelance.backend.entity.PartTimeJob;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.enums.EmploymentType;
import lk.freelance.backend.enums.ListingStatus;
import lk.freelance.backend.enums.ListingType;
import lk.freelance.backend.enums.PricingModel;
import lk.freelance.backend.exception.BadRequestException;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.exception.UnauthorizedException;
import lk.freelance.backend.repository.GigRepository;
import lk.freelance.backend.repository.ListingRepository;
import lk.freelance.backend.repository.PartTimeJobRepository;
import lk.freelance.backend.repository.UserRepository;
import lk.freelance.backend.service.ListingsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingsApiServiceImpl implements ListingsApiService {

    private final ListingRepository listingRepository;
    private final GigRepository gigRepository;
    private final PartTimeJobRepository partTimeJobRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ListingResponseDTO createGig(UUID userId, CreateListingGigRequest request) {
        User creator = getUser(userId);
        LocalDateTime now = LocalDateTime.now();

        Listing listing = listingRepository.save(Listing.builder()
                .user(creator)
                .listingType(ListingType.GIG)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ListingStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());

        Gig gig = gigRepository.save(Gig.builder()
                .listing(listing)
                .category(request.getCategory())
                .pricingModel(parsePricingModel(request.getPricingModel()))
                .basePrice(request.getBasePrice())
                .deliveryDays(request.getDeliveryDays())
                .createdAt(now)
                .updatedAt(now)
                .build());

        return toResponse(listing, gig, null);
    }

    @Override
    @Transactional
    public ListingResponseDTO createJob(UUID userId, CreateListingJobRequest request) {
        validateCompensation(request.getHourlyRate(), request.getMonthlySalary());
        User creator = getUser(userId);
        LocalDateTime now = LocalDateTime.now();

        Listing listing = listingRepository.save(Listing.builder()
                .user(creator)
                .listingType(ListingType.PART_TIME_JOB)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ListingStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());

        PartTimeJob job = partTimeJobRepository.save(PartTimeJob.builder()
                .listing(listing)
                .employmentType(parseEmploymentType(request.getEmploymentType()))
                .hourlyRate(request.getHourlyRate())
                .monthlySalary(request.getMonthlySalary())
                .location(request.getLocation())
                .isRemote(request.getIsRemote())
                .createdAt(now)
                .updatedAt(now)
                .build());

        return toResponse(listing, null, job);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ListingResponseDTO> getListings(
            String type,
            String category,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer page,
            Integer size
    ) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        ListingType listingType = parseListingType(type);

        List<Listing> baseResults = listingRepository.findByTypeAndKeyword(listingType, normalize(keyword));
        List<ListingResponseDTO> filtered = new ArrayList<>();
        for (Listing listing : baseResults) {
            ListingResponseDTO dto = mapListing(listing);
            if (dto == null) {
                continue;
            }
            if (!matchesCategory(dto, category)) {
                continue;
            }
            if (!matchesPrice(dto, minPrice, maxPrice)) {
                continue;
            }
            filtered.add(dto);
        }

        filtered.sort(Comparator.comparing(ListingResponseDTO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        int from = Math.min(safePage * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        List<ListingResponseDTO> content = filtered.subList(from, to);

        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / safeSize);
        return PagedResponse.<ListingResponseDTO>builder()
                .content(content)
                .currentPage(safePage)
                .totalPages(totalPages)
                .totalElements((long) filtered.size())
                .pageSize(safeSize)
                .hasNext(to < filtered.size())
                .hasPrevious(safePage > 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponseDTO getListingById(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));
        ListingResponseDTO response = mapListing(listing);
        if (response == null) {
            throw new ResourceNotFoundException("Listing details not found for id: " + listingId);
        }
        return response;
    }

    @Override
    @Transactional
    public ListingResponseDTO updateListing(UUID userId, UUID listingId, UpdateListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));
        assertOwnership(userId, listing);

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setUpdatedAt(LocalDateTime.now());

        if (listing.getListingType() == ListingType.GIG) {
            Gig gig = gigRepository.findByListing_ListingId(listingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Gig", "listingId", listingId));

            if (isBlank(request.getCategory()) || isBlank(request.getPricingModel())
                    || request.getBasePrice() == null || request.getDeliveryDays() == null) {
                throw new BadRequestException("category, pricingModel, basePrice and deliveryDays are required for GIG updates");
            }

            gig.setCategory(request.getCategory());
            gig.setPricingModel(parsePricingModel(request.getPricingModel()));
            gig.setBasePrice(request.getBasePrice());
            gig.setDeliveryDays(request.getDeliveryDays());
            gig.setUpdatedAt(LocalDateTime.now());

            listingRepository.save(listing);
            gigRepository.save(gig);
            return toResponse(listing, gig, null);
        }

        PartTimeJob job = partTimeJobRepository.findByListing_ListingId(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "listingId", listingId));
        if (isBlank(request.getEmploymentType())) {
            throw new BadRequestException("employmentType is required for JOB updates");
        }
        validateCompensation(request.getHourlyRate(), request.getMonthlySalary());

        job.setEmploymentType(parseEmploymentType(request.getEmploymentType()));
        job.setHourlyRate(request.getHourlyRate());
        job.setMonthlySalary(request.getMonthlySalary());
        job.setLocation(request.getLocation());
        job.setIsRemote(request.getIsRemote() != null ? request.getIsRemote() : Boolean.FALSE);
        job.setUpdatedAt(LocalDateTime.now());

        listingRepository.save(listing);
        partTimeJobRepository.save(job);
        return toResponse(listing, null, job);
    }

    @Override
    @Transactional
    public void deleteListing(UUID userId, UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));
        assertOwnership(userId, listing);

        if (listing.getListingType() == ListingType.GIG) {
            gigRepository.findByListing_ListingId(listingId).ifPresent(gigRepository::delete);
        } else if (listing.getListingType() == ListingType.PART_TIME_JOB) {
            partTimeJobRepository.findByListing_ListingId(listingId).ifPresent(partTimeJobRepository::delete);
        }

        listingRepository.delete(listing);
    }

    private void assertOwnership(UUID userId, Listing listing) {
        UUID ownerId = listing.getUser() != null ? listing.getUser().getUserId() : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new UnauthorizedException("Only the creator can modify this listing");
        }
    }

    private ListingResponseDTO mapListing(Listing listing) {
        if (listing.getListingType() == ListingType.GIG) {
            return gigRepository.findByListing_ListingId(listing.getListingId())
                    .map(gig -> toResponse(listing, gig, null))
                    .orElse(null);
        }
        if (listing.getListingType() == ListingType.PART_TIME_JOB) {
            return partTimeJobRepository.findByListing_ListingId(listing.getListingId())
                    .map(job -> toResponse(listing, null, job))
                    .orElse(null);
        }
        return null;
    }

    private ListingResponseDTO toResponse(Listing listing, Gig gig, PartTimeJob job) {
        return ListingResponseDTO.builder()
                .listingId(listing.getListingId())
                .type(listing.getListingType().name())
                .status(listing.getStatus() != null ? listing.getStatus().name() : null)
                .creatorId(listing.getUser() != null ? listing.getUser().getUserId() : null)
                .title(listing.getTitle())
                .description(listing.getDescription())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .category(gig != null ? gig.getCategory() : null)
                .pricingModel(gig != null && gig.getPricingModel() != null ? gig.getPricingModel().name() : null)
                .basePrice(gig != null ? gig.getBasePrice() : null)
                .deliveryDays(gig != null ? gig.getDeliveryDays() : null)
                .employmentType(job != null && job.getEmploymentType() != null ? job.getEmploymentType().name() : null)
                .hourlyRate(job != null ? job.getHourlyRate() : null)
                .monthlySalary(job != null ? job.getMonthlySalary() : null)
                .location(job != null ? job.getLocation() : null)
                .isRemote(job != null ? job.getIsRemote() : null)
                .build();
    }

    private boolean matchesCategory(ListingResponseDTO dto, String category) {
        if (isBlank(category)) {
            return true;
        }
        return dto.getCategory() != null && dto.getCategory().equalsIgnoreCase(category.trim());
    }

    private boolean matchesPrice(ListingResponseDTO dto, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return true;
        }

        BigDecimal price = dto.getBasePrice();
        if (price == null) {
            price = dto.getHourlyRate() != null ? dto.getHourlyRate() : dto.getMonthlySalary();
        }
        if (price == null) {
            return false;
        }
        if (minPrice != null && price.compareTo(minPrice) < 0) {
            return false;
        }
        if (maxPrice != null && price.compareTo(maxPrice) > 0) {
            return false;
        }
        return true;
    }

    private void validateCompensation(BigDecimal hourlyRate, BigDecimal monthlySalary) {
        if (hourlyRate == null && monthlySalary == null) {
            throw new BadRequestException("Either hourlyRate or monthlySalary must be provided for a job");
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ListingType parseListingType(String rawType) {
        if (isBlank(rawType)) {
            return null;
        }
        String normalized = rawType.trim().toUpperCase();
        return switch (normalized) {
            case "GIG" -> ListingType.GIG;
            case "JOB", "PART_TIME_JOB" -> ListingType.PART_TIME_JOB;
            default -> throw new BadRequestException("Invalid listing type. Use GIG or JOB");
        };
    }

    private PricingModel parsePricingModel(String rawPricingModel) {
        try {
            return PricingModel.valueOf(rawPricingModel.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid pricingModel. Use FIXED, HOURLY or PACKAGE");
        }
    }

    private EmploymentType parseEmploymentType(String rawEmploymentType) {
        try {
            return EmploymentType.valueOf(rawEmploymentType.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid employmentType. Use PART_TIME, CONTRACT or FREELANCE");
        }
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
