package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla {@code roles}.
 * <p>
 * La relación N:M con {@link User} se gestiona desde el lado del usuario
 * (lado propietario). Aquí solo se declara el lado inverso con {@code mappedBy}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    /** Identificador único del rol. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** VARCHAR(50) NOT NULL UNIQUE - Nombre técnico (ROLE_ADMIN, ROLE_USER...) */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /** VARCHAR(100) NOT NULL - Nombre legible para la interfaz */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** VARCHAR(255) NULL - Descripción opcional del rol */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Relación N:M con User.
     * Lado NO propietario (mappedBy = "roles").
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users = new HashSet<>();

    /** Constructor útil para crear roles sin id ni usuarios. */
    public Role(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }
}
