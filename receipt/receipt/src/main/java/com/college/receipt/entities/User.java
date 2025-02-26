    package com.college.receipt.entities;

    import jakarta.persistence.*;
    import jakarta.validation.constraints.*;
    import lombok.*;

    import java.util.HashSet;
    import java.util.Set;


    @Table(name = "users")
    @Entity
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 3, max = 20)
        private String username;
        @NotBlank(message = "Почта не может быть пустой")
        @Email(message = "Некорректный адрес почты")
        private String email;
        @NotBlank
        private String password;
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "users_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id")
        )
        private Set<Role> roles = new HashSet<>();

        public User(Long id, String username, String mail) {
            this.id = id;
            this.username = username;
            this.email = mail;
        }
    }