package com.wap.app2.gachitayo.domain.fare;

import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Integer baseFigure = 0;

    private Integer finalFigure;

    @ManyToOne
    @JoinColumn(name = "stopover_id")
    private Stopover stopover;
}
