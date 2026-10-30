package fu.de180174.postservice.service;

import fu.de180174.postservice.dto.PageDTO;
import fu.de180174.postservice.dto.PostDTO;
import fu.de180174.postservice.dto.PostDetailDTO;

public interface PostService {

    PostDTO createPost(PostDTO postDTO);

    PostDTO updatePost(Long postId, PostDTO postDTO);

    PostDetailDTO getPostById(Long postId);

    void cancelPost(Long postId);

    PageDTO getPosts(Integer page, Integer size, String advertiserName, String status);
}
