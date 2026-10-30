package fu.de180174.userservice.controller;

import fu.de180174.userservice.dto.ApiResponse;
import fu.de180174.userservice.dto.PageDTO;
import fu.de180174.userservice.dto.UserDTO;
import fu.de180174.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Endpoints for managing users")
public class DE180174UserController {

    private final UserService userService;

    public DE180174UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserDTO userDTO) {
        UserDTO created = userService.createUser(userDTO);
        ApiResponse response = new ApiResponse(1, "Created successfully", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // HTTP 201
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable("userId") Long userId, @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(userId, userDTO);
        ApiResponse response = new ApiResponse(1, "Updated successfully", updated);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user details by userId")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable("userId") Long userId) {
        UserDTO user = userService.getUserById(userId);
        ApiResponse response = new ApiResponse(1, "Successful", user);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Deactivate user by userId")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable("userId") Long userId) {
        userService.deactivateUser(userId);
        ApiResponse response = new ApiResponse(1, "Set to DEACTIVATED successfully", null);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @GetMapping
    @Operation(summary = "Get user list with pagination and filtering")
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status) {
        PageDTO pageDTO = userService.getUsers(page, size, role, status);
        ApiResponse response = new ApiResponse(1, "Successful", pageDTO);
        return ResponseEntity.ok(response); // HTTP 200
    }
}
