package com.wap.app2.gachitayo.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report")
public class MemberReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String reporter;

    @NotNull
    private String target;

    @NotNull
    @Size(max = 500)
    private String content;
}