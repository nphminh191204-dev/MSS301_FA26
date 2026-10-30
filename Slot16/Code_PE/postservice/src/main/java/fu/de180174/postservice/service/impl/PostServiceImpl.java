package fu.de180174.postservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import fu.de180174.postservice.common.ResourceNotFoundException;
import fu.de180174.postservice.common.UserNotActiveException;
import fu.de180174.postservice.common.ValidationException;
import fu.de180174.postservice.dto.ApiResponse;
import fu.de180174.postservice.dto.PageDTO;
import fu.de180174.postservice.dto.PostDTO;
import fu.de180174.postservice.dto.PostDetailDTO;
import fu.de180174.postservice.dto.UserDTO;
import fu.de180174.postservice.entity.Post;
import fu.de180174.postservice.entity.PostStatus;
import fu.de180174.postservice.repository.PostRepository;
import fu.de180174.postservice.service.PostService;
import fu.de180174.postservice.service.UserClient;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final PostRepository postRepository;
    private final UserClient userClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostServiceImpl(PostRepository postRepository, UserClient userClient) {
        this.postRepository = postRepository;
        this.userClient = userClient;
    }

    @Override
    public PostDTO createPost(PostDTO postDTO) {
        validatePostDTOForCreate(postDTO);

        UserDTO user = fetchAndValidateUser(postDTO.getUserId());

        if (user.getMaxDailyPosts() != null && postDTO.getNumberOfPosts() > user.getMaxDailyPosts()) {
            throw new ValidationException("Validation failed: numberOfPosts (" + postDTO.getNumberOfPosts()
                    + ") exceeds user's maxDailyPosts (" + user.getMaxDailyPosts() + ")");
        }

        LocalDate startLocal = toLocalDate(postDTO.getStartDate());
        LocalDate endLocal = toLocalDate(postDTO.getEndDate());
        long days = ChronoUnit.DAYS.between(startLocal, endLocal);
        if (days <= 0) {
            throw new ValidationException("Validation failed: endDate must be after startDate");
        }

        BigDecimal rate = (user.getRatePerPost() != null) ? user.getRatePerPost() : BigDecimal.ZERO;
        BigDecimal totalCost = rate.multiply(BigDecimal.valueOf(days));

        Post post = new Post();
        post.setAdvertiserName(postDTO.getAdvertiserName().trim());
        post.setAdvertiserEmail(postDTO.getAdvertiserEmail().trim());
        post.setAdvertiserPhone(postDTO.getAdvertiserPhone().trim());
        post.setStartDate(postDTO.getStartDate());
        post.setEndDate(postDTO.getEndDate());
        post.setNumberOfPosts(postDTO.getNumberOfPosts());
        post.setTotalCost(totalCost);
        post.setStatus(PostStatus.SCHEDULED);
        post.setUserId(postDTO.getUserId());

        Post saved = postRepository.save(post);
        return mapToDTO(saved);
    }

    @Override
    public PostDTO updatePost(Long postId, PostDTO postDTO) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Date effectiveStart = (postDTO.getStartDate() != null) ? postDTO.getStartDate() : post.getStartDate();
        Date effectiveEnd = (postDTO.getEndDate() != null) ? postDTO.getEndDate() : post.getEndDate();

        LocalDate startLocal = toLocalDate(effectiveStart);
        LocalDate endLocal = toLocalDate(effectiveEnd);
        long days = ChronoUnit.DAYS.between(startLocal, endLocal);
        if (days <= 0) {
            throw new ValidationException("Validation failed: endDate must be after startDate");
        }

        post.setStartDate(effectiveStart);
        post.setEndDate(effectiveEnd);

        if (postDTO.getAdvertiserName() != null) {
            String name = postDTO.getAdvertiserName().trim();
            if (name.isEmpty() || name.length() > 100) {
                throw new ValidationException("Validation failed: advertiserName must not be empty and maximum length is 100");
            }
            post.setAdvertiserName(name);
        }

        if (postDTO.getAdvertiserEmail() != null) {
            String email = postDTO.getAdvertiserEmail().trim();
            if (email.isEmpty() || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new ValidationException("Validation failed: invalid email format or maximum length 100 exceeded");
            }
            post.setAdvertiserEmail(email);
        }

        if (postDTO.getAdvertiserPhone() != null) {
            String phone = postDTO.getAdvertiserPhone().trim();
            if (phone.isEmpty() || phone.length() > 20) {
                throw new ValidationException("Validation failed: advertiserPhone must not be empty and maximum length is 20");
            }
            post.setAdvertiserPhone(phone);
        }

        if (postDTO.getStatus() != null) {
            post.setStatus(parsePostStatus(postDTO.getStatus()));
        }

        UserDTO user = fetchUserSilently(post.getUserId());

        if (postDTO.getNumberOfPosts() != null) {
            if (postDTO.getNumberOfPosts() < 1) {
                throw new ValidationException("Validation failed: numberOfPosts must be greater than or equal to 1");
            }
            if (user != null && user.getMaxDailyPosts() != null && postDTO.getNumberOfPosts() > user.getMaxDailyPosts()) {
                throw new ValidationException("Validation failed: numberOfPosts (" + postDTO.getNumberOfPosts()
                        + ") exceeds user's maxDailyPosts (" + user.getMaxDailyPosts() + ")");
            }
            post.setNumberOfPosts(postDTO.getNumberOfPosts());
        }

        if (user != null && user.getRatePerPost() != null) {
            post.setTotalCost(user.getRatePerPost().multiply(BigDecimal.valueOf(days)));
        }

        Post updated = postRepository.save(post);
        return mapToDTO(updated);
    }

    @Override
    public PostDetailDTO getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        UserDTO user = fetchUserSilently(post.getUserId());
        return mapToDetailDTO(post, user);
    }

    @Override
    public void cancelPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.setStatus(PostStatus.CANCELLED);
        postRepository.save(post);
    }

    @Override
    public PageDTO getPosts(Integer page, Integer size, String advertiserName, String status) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        if (pageSize > 100 || pageSize < 1 || pageNum < 0) {
            throw new ValidationException("Validation failed: size must be between 1 and 100, page must be >= 0");
        }

        final PostStatus statusFilter = (status != null && !status.trim().isEmpty()) ? parsePostStatus(status) : null;
        final String nameFilter = (advertiserName != null && !advertiserName.trim().isEmpty()) ? advertiserName.trim().toLowerCase() : null;

        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (nameFilter != null) {
                predicates.add(cb.like(cb.lower(root.get("advertiserName")), "%" + nameFilter + "%"));
            }
            if (statusFilter != null) {
                predicates.add(cb.equal(root.get("status"), statusFilter));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Post> postPage = postRepository.findAll(spec, PageRequest.of(pageNum, pageSize));

        List<PostDetailDTO> detailList = postPage.getContent().stream()
                .map(p -> mapToDetailDTO(p, fetchUserSilently(p.getUserId())))
                .collect(Collectors.toList());

        return new PageDTO(
                postPage.getSize(),
                postPage.getNumber(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isFirst(),
                postPage.isLast(),
                detailList
        );
    }

    private void validatePostDTOForCreate(PostDTO dto) {
        if (dto == null) {
            throw new ValidationException("Validation failed: request body is missing");
        }
        if (dto.getAdvertiserName() == null || dto.getAdvertiserName().trim().isEmpty() || dto.getAdvertiserName().trim().length() > 100) {
            throw new ValidationException("Validation failed: advertiserName is required and maximum length is 100");
        }
        if (dto.getAdvertiserEmail() == null || dto.getAdvertiserEmail().trim().isEmpty()
                || dto.getAdvertiserEmail().trim().length() > 100 || !EMAIL_PATTERN.matcher(dto.getAdvertiserEmail().trim()).matches()) {
            throw new ValidationException("Validation failed: advertiserEmail is required, must be valid and maximum length is 100");
        }
        if (dto.getAdvertiserPhone() == null || dto.getAdvertiserPhone().trim().isEmpty() || dto.getAdvertiserPhone().trim().length() > 20) {
            throw new ValidationException("Validation failed: advertiserPhone is required and maximum length is 20");
        }
        if (dto.getStartDate() == null) {
            throw new ValidationException("Validation failed: startDate is required");
        }
        if (dto.getEndDate() == null) {
            throw new ValidationException("Validation failed: endDate is required");
        }
        if (!dto.getEndDate().after(dto.getStartDate())) {
            throw new ValidationException("Validation failed: endDate must be after startDate");
        }
        if (dto.getNumberOfPosts() == null || dto.getNumberOfPosts() < 1) {
            throw new ValidationException("Validation failed: numberOfPosts is required and must be greater than or equal to 1");
        }
        if (dto.getUserId() == null) {
            throw new ValidationException("Validation failed: userId is required");
        }
    }

    private UserDTO fetchAndValidateUser(Long userId) {
        try {
            ApiResponse response = userClient.getUserById(userId);
            if (response == null || response.getStatus() == 4 || response.getData() == null) {
                throw new ResourceNotFoundException("User not found");
            }
            UserDTO user = objectMapper.convertValue(response.getData(), UserDTO.class);
            if (user == null) {
                throw new ResourceNotFoundException("User not found");
            }
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                throw new UserNotActiveException("User is not ACTIVE");
            }
            return user;
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("User not found");
        } catch (ResourceNotFoundException | UserNotActiveException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ValidationException("Validation failed: unable to verify user: " + ex.getMessage());
        }
    }

    private UserDTO fetchUserSilently(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            ApiResponse response = userClient.getUserById(userId);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), UserDTO.class);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private PostStatus parsePostStatus(String statusStr) {
        try {
            return PostStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (Exception e) {
            throw new ValidationException("Validation failed: status must be DRAFT, SCHEDULED, PUBLISHED, or CANCELLED");
        }
    }

    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private PostDTO mapToDTO(Post post) {
        return new PostDTO(
                post.getPostId(),
                post.getAdvertiserName(),
                post.getAdvertiserEmail(),
                post.getAdvertiserPhone(),
                post.getStartDate(),
                post.getEndDate(),
                post.getNumberOfPosts(),
                post.getTotalCost(),
                post.getStatus().name(),
                post.getUserId()
        );
    }

    private PostDetailDTO mapToDetailDTO(Post post, UserDTO user) {
        return new PostDetailDTO(
                post.getPostId(),
                post.getAdvertiserName(),
                post.getAdvertiserEmail(),
                post.getAdvertiserPhone(),
                post.getStartDate(),
                post.getEndDate(),
                post.getNumberOfPosts(),
                post.getTotalCost(),
                post.getStatus().name(),
                user
        );
    }
}
