package ru.practicum.ewm.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.service.comment.CommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentPublicController {

    private final CommentService commentService;

    @GetMapping("/{eventId}/{commentId}")
    public CommentDto getCommentById(@PathVariable Long eventId,
                                     @PathVariable Long commentId) {
        log.debug("/comments/{}/{}", eventId, commentId);
        return commentService.getCommentById(eventId, commentId);
    }

    @GetMapping("/{eventId}")
    public List<CommentDto> getCommentsByEventId(@PathVariable  Long eventId,
                                                 @RequestParam(defaultValue = "0")  Integer from,
                                                 @RequestParam(defaultValue = "10")  Integer size) {
        log.debug("GET: /comments/{}?from={}&size={}", eventId, from, size);
        return commentService.getCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/{eventId}/search")
    public List<CommentDto> getAllCommentsByParams(@PathVariable  Long eventId,
                                                   @RequestParam(required = false) String text,
                                                   @RequestParam(defaultValue = "0")  Integer from,
                                                   @RequestParam(defaultValue = "10")  Integer size) {
        log.debug("GET: /comments/{}/search?text={}&from={}&size={}", eventId, text, from, size);
        return commentService.getAllCommentsByParam(eventId, text, from, size);
    }
}