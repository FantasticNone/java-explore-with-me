package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.exceptions.ConflictDataException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.comment.Comment;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm.model.event.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventsRepository eventRepository;
    private final UserRepository userRepository;


    @Override
    public CommentDto createComment(long userId, long eventId, NewCommentDto newDto) {
        User user = checkUserId(userId);
        Event event = checkEventId(eventId);
        if (event.getState() != PUBLISHED) {
            throw new ConflictDataException("Comment can be added to a published event only");
        }

        Comment newComment = CommentMapper.toComment(newDto, user, event);

        return CommentMapper.toCommentDto(commentRepository.save(newComment), event.getId());
    }

    @Override
    public void deleteComment(long commentId) {
        checkCommentId(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteCommentByCreator(long userId, long eventId, long commentId) {
        Comment comment = checkCommentId(commentId);
        User creator = checkUserId(userId);
        if (Objects.equals(comment.getAuthor().getId(), creator.getId())) {
            commentRepository.delete(comment);
        } else {
            throw new ConflictDataException("Сan't delete someone else's comment");
        }
    }

    @Override
    public CommentDto editComment(long userId, long eventId, long commentId, NewCommentDto newCommentDto) {
        User creator = checkUserId(userId);
        Event event = checkEventId(eventId);
        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        if (Objects.equals(creator.getId(), comment.getAuthor().getId())) {
            comment.setText(newCommentDto.getText());
            comment.setEdited(LocalDateTime.now());
            return CommentMapper.toCommentDto(commentRepository.save(comment), event.getId());
        }
        throw new ConflictDataException("Сan't edit someone else's comment");
    }

    @Override
    public CommentDto getCommentByAuthorId(long userId, long commentId) {
        User author = checkUserId(userId);
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, author.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        return CommentMapper.toCommentDto(comment, comment.getEvent().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(long eventId, long commentId) {
        Event event = checkEventId(eventId);
        Comment comment = commentRepository.findByIdAndEventId(commentId, event.getId()).orElseThrow(
                () -> new NotFoundException(String.format("Comment with id=%d was not found", commentId))
        );
        return CommentMapper.toCommentDto(comment, comment.getId());
    }

    @Override
    public List<CommentDto> getCommentsByEventId(long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Event savedEvent = checkEventId(eventId);

        return commentRepository.findAllByEventId(eventId, pageable).stream()
                .map(x -> CommentMapper.toCommentDto(x, savedEvent.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getAllCommentsByParam(long eventId, String text, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Event savedEvent = checkEventId(eventId);
        System.out.println(text);

        if (StringUtils.isNotBlank(text))
            return commentRepository.searchByText(text.toLowerCase(), eventId, pageable).stream()
                    .map(x -> CommentMapper.toCommentDto(x, savedEvent.getId()))
                    .collect(Collectors.toList());

        else return List.of();
    }

    private Comment checkCommentId(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        return comment;
    }

    private User checkUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        return user;
    }

    private Event checkEventId(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }
}
