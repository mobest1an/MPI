package com.iverpa.mpi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    @ElementCollection
    @CollectionTable(
            name = "roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
