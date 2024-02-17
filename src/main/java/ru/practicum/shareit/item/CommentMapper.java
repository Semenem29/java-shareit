package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() == null ? null : comment.getAuthor().getName())
                .itemId(comment.getItem() == null ? null : comment.getItem().getId())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentRequestDto commentRequestDto, User author, Item item) {
        return Comment.builder()
                .id(commentRequestDto.getId())
                .text(commentRequestDto.getText())
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build();
    }

    public static List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }
}
