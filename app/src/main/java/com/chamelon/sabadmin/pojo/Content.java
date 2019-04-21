package com.chamelon.sabadmin.pojo;

import java.util.ArrayList;

public class Content {

    private String title;
    private String source;
    private String hostUrl;
    private String contentUrl;
    private String description;
    private String thumbnailUrl;

    private long likes;
    private long viewCount;
    private long contentId;
    private long publishedOn;
    private long appViewsCount;
    private long toBePublishedOn;

    private ArrayList<String> tags;

    public long getContentId() {
        return contentId;
    }

    public void setContentId(long contentId) {
        this.contentId = contentId;
    }

    public long getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(long publishedOn) {
        this.publishedOn = publishedOn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getToBePublishedOn() {
        return toBePublishedOn;
    }

    public void setToBePublishedOn(long toBePublishedOn) {
        this.toBePublishedOn = toBePublishedOn;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getAppViewsCount() {
        return appViewsCount;
    }

    public void setAppViewsCount(long appViewsCount) {
        this.appViewsCount = appViewsCount;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    @Override
    public String toString() {
        return "Content{" +
                "title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", hostUrl='" + hostUrl + '\'' +
                ", contentUrl='" + contentUrl + '\'' +
                ", description='" + description + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", likes=" + likes +
                ", viewCount=" + viewCount +
                ", contentId=" + contentId +
                ", publishedOn=" + publishedOn +
                ", appViewsCount=" + appViewsCount +
                ", toBePublishedOn=" + toBePublishedOn +
                ", tags=" + tags +
                '}';
    }
}
