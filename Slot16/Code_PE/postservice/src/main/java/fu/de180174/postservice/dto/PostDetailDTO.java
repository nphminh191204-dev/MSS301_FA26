package fu.de180174.postservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class PostDetailDTO {

    private Long postId;
    private String advertiserName;
    private String advertiserEmail;
    private String advertiserPhone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "GMT+7")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "GMT+7")
    private Date endDate;

    private Integer numberOfPosts;
    private BigDecimal totalCost;
    private String status;
    private UserDTO user;

    public PostDetailDTO() {
    }

    public PostDetailDTO(Long postId, String advertiserName, String advertiserEmail, String advertiserPhone,
                         Date startDate, Date endDate, Integer numberOfPosts, BigDecimal totalCost,
                         String status, UserDTO user) {
        this.postId = postId;
        this.advertiserName = advertiserName;
        this.advertiserEmail = advertiserEmail;
        this.advertiserPhone = advertiserPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfPosts = numberOfPosts;
        this.totalCost = totalCost;
        this.status = status;
        this.user = user;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    public void setAdvertiserName(String advertiserName) {
        this.advertiserName = advertiserName;
    }

    public String getAdvertiserEmail() {
        return advertiserEmail;
    }

    public void setAdvertiserEmail(String advertiserEmail) {
        this.advertiserEmail = advertiserEmail;
    }

    public String getAdvertiserPhone() {
        return advertiserPhone;
    }

    public void setAdvertiserPhone(String advertiserPhone) {
        this.advertiserPhone = advertiserPhone;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(Integer numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
