package com.wap.app2.gachitayo.domain.fare;

import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Getter
    @Setter
    @Builder.Default
    private int baseFigure = 0;

    @OneToOne(fetch = FetchType.LAZY)
    private Stopover stopover;
}
