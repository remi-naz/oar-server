package dev.ridill.oar_server.user;

import dev.ridill.oar_server.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "fcm_token", columnDefinition = "text")
    private String fcmToken;
}
