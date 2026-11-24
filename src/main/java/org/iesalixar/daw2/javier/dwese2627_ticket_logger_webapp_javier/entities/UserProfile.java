package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.User;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla {@code user_profiles}.
 * <p>
 * Relación 1:1 con {@link User} usando "shared primary key":
 * la columna {@code user_id} es a la vez PK de user_profiles y FK hacia users.id.
 * Con {@link MapsId} le decimos a JPA que la clave primaria de esta entidad
 * es la misma que la del User asociado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    /** PK = user_id (se "hereda" del User gracias a @MapsId). */
    @Id
    @Column(name = "user_id")
    private Long id;

    /** Usuario propietario del perfil (lado propietario de la relación 1:1). */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /** VARCHAR(60) NOT NULL */
    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    /** VARCHAR(80) NOT NULL */
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    /** VARCHAR(30) NULL */
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    /** VARCHAR(255) NULL - Ruta/URL de la imagen de perfil */
    @Column(name = "profile_image", length = 255)
    private String profileImage;

    /** VARCHAR(500) NULL - Pequeña descripción / biografía */
    @Column(name = "bio", length = 500)
    private String bio;

    /** VARCHAR(10) NULL - Código de idioma/locale (es_ES, en_US, ...) */
    @Column(name = "locale", length = 10)
    private String locale;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP (gestionado por la BD) */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP (BD) */
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor de conveniencia sin campos de auditoría.
     * El id se tomará del User asociado (por @MapsId).
     */
    public UserProfile(User user, String firstName, String lastName, String phoneNumber,
                       String profileImage, String bio, String locale) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.bio = bio;
        this.locale = locale;
    }
}
