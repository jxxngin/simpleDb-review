package modules;

import java.time.LocalDateTime;

public class Article {
    private Long id;
    private String title;
    private String body;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private boolean isBlind;

    public Long getId() {
        return id;
    }

    public Article setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Article setBody(String body) {
        this.body = body;
        return this;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Article setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public Article setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public boolean isBlind() {
        return isBlind;
    }

    public Article setBlind(boolean isBlind) {
        this.isBlind = isBlind;
        return this;
    }
}
