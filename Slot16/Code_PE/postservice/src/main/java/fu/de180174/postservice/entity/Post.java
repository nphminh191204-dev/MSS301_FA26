package fu.de180174.postservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "advertiser_name", length = 100, nullable = false)
    private String advertiserName;

    @Column(name = "advertiser_email", length = 100, nullable = false)
    private String advertiserEmail;

    @Column(name = "advertiser_phone", length = 20, nullable = false)
    private String advertiserPhone;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "number_of_posts", nullable = false)
    private int numberOfPosts;

    @Column(name = "total_cost", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private PostStatus status;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Post() {
    }

    public Post(Long postId, String advertiserName, String advertiserEmail, String advertiserPhone,
                Date startDate, Date endDate, int numberOfPosts, BigDecimal totalCost,
                PostStatus status, Long userId) {
        this.postId = postId;
        this.advertiserName = advertiserName;
        this.advertiserEmail = advertiserEmail;
        this.advertiserPhone = advertiserPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfPosts = numberOfPosts;
        this.totalCost = totalCost;
        this.status = status;
        this.userId = userId;
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

    public int getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(int numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
