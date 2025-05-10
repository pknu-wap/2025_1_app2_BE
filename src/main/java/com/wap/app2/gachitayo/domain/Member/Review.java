package com.wap.app2.gachitayo.domain.Member;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("5.0")
    private double score;

    @Column(length = 2100) // 최대 500자 검증
    private String contents;
}
