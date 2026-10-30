package fu.de180174.userservice.service;

import fu.de180174.userservice.dto.PageDTO;
import fu.de180174.userservice.dto.UserDTO;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    UserDTO getUserById(Long userId);

    void deactivateUser(Long userId);

    PageDTO getUsers(Integer page, Integer size, String role, String status);
}
