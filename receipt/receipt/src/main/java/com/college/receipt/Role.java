package com.college.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "roles")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Роль не может быть пустой")
    @Column(unique = true)
    private String name;

    public Role(String name) {
        this.name = name;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = (String) name;
    }
}
