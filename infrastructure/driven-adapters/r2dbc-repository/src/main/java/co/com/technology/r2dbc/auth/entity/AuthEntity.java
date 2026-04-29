package co.com.technology.r2dbc.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("auths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthEntity {
    @Id
    private Long id;
    private String email;
    @Column("token")
    private String token;
    @Column("expires_in")
    private Integer expiresIn;
    @Column("created_at")
    private LocalDateTime createdAt;
}
