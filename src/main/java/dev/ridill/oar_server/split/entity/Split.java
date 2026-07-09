package dev.ridill.oar_server.split.entity;

import dev.ridill.oar_server.common.model.BaseEntity;
import dev.ridill.oar_server.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "splits")
public class Split extends BaseEntity {

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "note", nullable = false, columnDefinition = "text")
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "split_status")
    private SplitStatus status = SplitStatus.ACTIVE;
}
