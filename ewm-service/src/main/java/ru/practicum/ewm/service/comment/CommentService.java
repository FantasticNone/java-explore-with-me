package ru.practicum.ewm.service.comment;

import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(long userId, long eventId, NewCommentDto newDto);

    void deleteComment(long commentId);

    void deleteCommentByCreator(long userId, long eventId, long commentId);

    CommentDto editComment(long userId, long eventId, long commentId, NewCommentDto newCommentDto);

    CommentDto getCommentByAuthorId(long userId, long commentId);

    CommentDto getCommentById(long eventId, long commentId);

    List<CommentDto> getCommentsByEventId(long eventId, int from, int size);

    List<CommentDto> getAllCommentsByParam(long eventId, String text, int from, int size);
}
