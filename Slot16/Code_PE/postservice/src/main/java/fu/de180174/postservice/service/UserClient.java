package fu.de180174.postservice.service;

import fu.de180174.postservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userservice", url = "${user.service.url:http://localhost:8081}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    ApiResponse getUserById(@PathVariable("userId") Long userId);
}
