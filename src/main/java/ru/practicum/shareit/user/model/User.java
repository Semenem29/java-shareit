package ru.practicum.shareit.user.model;

import lombok.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.group.Update;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}
