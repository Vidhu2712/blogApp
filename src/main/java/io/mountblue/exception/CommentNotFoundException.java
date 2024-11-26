package io.mountblue.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String s) {
        super("Comment not found");
    }
}