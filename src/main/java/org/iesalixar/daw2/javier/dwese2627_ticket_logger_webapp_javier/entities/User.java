package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla {@code users}.
 * <p>
 * Relaciones:
 * <ul>
 *     <li>N:M con {@link Role} a través de la tabla intermedia {@code user_roles}
 *     (User es el lado propietario).</li>
 *     <li>1:1 con {@link UserProfile} (la FK está en {@code user_profiles},
 *     por eso aquí se usa {@code mappedBy}).</li>
 * </ul>
 * Las validaciones de formulario NO están aquí: están en los DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** VARCHAR(100) NOT NULL UNIQUE */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** VARCHAR(500) NOT NULL */
    @Column(name = "password_hash", nullable = false, length = 500)
    private String passwordHash;

    /** BOOLEAN NOT NULL DEFAULT TRUE */
    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

    /** BOOLEAN NOT NULL DEFAULT TRUE */
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = Boolean.TRUE;

    /** DATETIME NULL */
    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    /** DATETIME NULL */
    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    /** INT DEFAULT 0 */
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    /** BOOLEAN NOT NULL DEFAULT FALSE */
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = Boolean.FALSE;

    /** BOOLEAN NOT NULL DEFAULT FALSE */
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = Boolean.FALSE;

    /**
     * Relación N:M con Role. Lado propietario: aquí se define la tabla intermedia.
     * Hibernate inserta/borra filas de user_roles al añadir/quitar roles del Set.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    /**
     * Relación 1:1 con la entidad UserProfile.
     * cascade ALL + orphanRemoval: al borrar el usuario se borra también su perfil.
     */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserProfile profile;
}
