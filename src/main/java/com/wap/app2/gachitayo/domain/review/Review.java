package com.wap.app2.gachitayo.domain.review;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
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

    // 리뷰 작성자
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Member author;

    // 리뷰 대상자
    @ManyToOne
    @JoinColumn(name = "target_id")
    private Member target;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("5.0")
    private double score;

    @Column(length = 2100) // 최대 500자 검증
    private String contents;
}
