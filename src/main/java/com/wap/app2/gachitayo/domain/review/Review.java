package com.wap.app2.gachitayo.domain.review;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @ElementCollection
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();
}
