package fu.de180174.postservice.controller;

import fu.de180174.postservice.dto.ApiResponse;
import fu.de180174.postservice.dto.PageDTO;
import fu.de180174.postservice.dto.PostDTO;
import fu.de180174.postservice.dto.PostDetailDTO;
import fu.de180174.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post Controller", description = "Endpoints for managing sponsored posts")
public class DE180174PostController {

    private final PostService postService;

    public DE180174PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "Create a new post campaign")
    public ResponseEntity<ApiResponse> createPost(@RequestBody PostDTO postDTO) {
        PostDTO created = postService.createPost(postDTO);
        ApiResponse response = new ApiResponse(1, "Created successfully", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // HTTP 201
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update an existing post campaign")
    public ResponseEntity<ApiResponse> updatePost(@PathVariable("postId") Long postId, @RequestBody PostDTO postDTO) {
        PostDTO updated = postService.updatePost(postId, postDTO);
        ApiResponse response = new ApiResponse(1, "Updated successfully", updated);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get post details by postId")
    public ResponseEntity<ApiResponse> getPostById(@PathVariable("postId") Long postId) {
        PostDetailDTO detail = postService.getPostById(postId);
        ApiResponse response = new ApiResponse(1, "Successful", detail);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Cancel post by postId")
    public ResponseEntity<ApiResponse> cancelPost(@PathVariable("postId") Long postId) {
        postService.cancelPost(postId);
        ApiResponse response = new ApiResponse(1, "Cancelled successfully", null);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @GetMapping
    @Operation(summary = "Get post list with pagination and filtering")
    public ResponseEntity<ApiResponse> getPosts(
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "advertiserName", required = false) String advertiserName,
            @RequestParam(value = "status", required = false) String status) {
        PageDTO pageDTO = postService.getPosts(page, size, advertiserName, status);
        ApiResponse response = new ApiResponse(1, "Successful", pageDTO);
        return ResponseEntity.ok(response); // HTTP 200
    }
}
