package com.wap.app2.gachitayo.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Builder.Default
    @Column(length = 10)
    private String author = "익명";

    @NotNull
    @Builder.Default
    private Double score = 0.0;

    @NotNull
    @Builder.Default
    @Column(length = 1000)
    private String comment = "";

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
