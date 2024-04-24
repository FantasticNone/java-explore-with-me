package ru.practicum.ewm.controller.privates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.service.comment.CommentService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createNewComment(@PathVariable Long userId,
                                       @PathVariable Long eventId,
                                       @RequestBody @Valid NewCommentDto newCommentDto) {
        log.debug("POST: /users/{}/comments/{}", userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentByOwnerId(@PathVariable Long userId,
                                          @PathVariable Long commentId) {
        log.debug("GET: /users/{}/comments/{}", userId, commentId);
        return commentService.getCommentByAuthorId(userId, commentId);
    }

    @PatchMapping("/{eventId}/{commentId}")
    public CommentDto editComment(@PathVariable Long userId,
                                  @PathVariable Long eventId,
                                  @PathVariable Long commentId,
                                  @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("PATCH: /users/{}/comments/{}/{}", userId, eventId, commentId);
        return commentService.editComment(userId, eventId, commentId, newCommentDto);
    }

    @DeleteMapping("/{eventId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByOwner(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @PathVariable Long commentId) {
        log.debug("DELETE: /users/{}/comments/{}/{}", userId, eventId, commentId);
        commentService.deleteCommentByCreator(userId, eventId, commentId);
    }
}
