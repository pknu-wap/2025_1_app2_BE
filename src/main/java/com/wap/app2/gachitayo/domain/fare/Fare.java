package com.wap.app2.gachitayo.domain.fare;

import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Builder.Default
    private int baseFigure = 0;

    @NotNull
    @Builder.Default
    private int finalFigure = 0;

    @OneToOne(fetch = FetchType.LAZY)
    private Stopover stopover;
}
