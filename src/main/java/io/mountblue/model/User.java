package io.mountblue.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String role;
    private String name;
    private String email;
    private String password;

    @OneToMany(mappedBy = "user")
    List<Post> post;
}
