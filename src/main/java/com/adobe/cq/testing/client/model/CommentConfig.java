package com.adobe.cq.testing.client.model;

/**
 * Represents configuration options for a comment component.
 */
public class CommentConfig {
    private final String defaultMessage;
    private final boolean isModerated;
    private final boolean allowReplies;
    private final boolean displayAsTree;
    private final boolean closed;

    public CommentConfig(String defaultMessage, boolean isModerated,
                         boolean allowReplies, boolean displayAsTree, boolean closed) {
        this.defaultMessage = defaultMessage;
        this.isModerated = isModerated;
        this.allowReplies = allowReplies;
        this.displayAsTree = displayAsTree;
        this.closed = closed;
    }
    /**
     * the topic for the comments.
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * true if comments are moderated.
     */
    public boolean isModerated() {
        return isModerated;
    }

    /**
     * true if replies are allowed.
     */
    public boolean isAllowReplies() {
        return allowReplies;
    }

    /**
     * true if the comments are displayed as tree.
     */
    public boolean isDisplayAsTree() {
        return displayAsTree;
    }

    /**
     * true if topic is closed (no posting of comments possible anymore.
     */
    public boolean isClosed() {
        return closed;
    }
}


