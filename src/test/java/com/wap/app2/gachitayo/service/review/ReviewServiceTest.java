package com.wap.app2.gachitayo.service.review;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.domain.review.Review;
import com.wap.app2.gachitayo.dto.request.ReviewMemberRequest;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import com.wap.app2.gachitayo.repository.member.ReviewRepository;
import com.wap.app2.gachitayo.service.party.PartyService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void addMemberReview_Success() {
        // given
        Member author = Member.builder().id(1L).name("작성자").build();
        Member target = Member.builder().id(2L).name("대상자").build();

        Party party = Party.builder().id(1L).build();
        PartyMember partyMember = PartyMember.builder().member(target).party(party).build();

        // 💡 파티에 직접 멤버 추가 (리스트 수정)
//        List<PartyMember> partyMemberList = new ArrayList<>();
        party.getPartyMemberList().add(partyMember);
//        party.setPartyMemberList(partyMemberList);

        ReviewMemberRequest request = new ReviewMemberRequest(1L, 4.5, "좋은 파티!");

        when(memberRepository.findByEmail("author@domain.com")).thenReturn(Optional.of(author));
        when(memberRepository.findByEmail("target@domain.com")).thenReturn(Optional.of(target));
        when(partyService.findPartyById(1L)).thenReturn(party);
        when(reviewRepository.existsByPartyIdAndAuthorIdAndTargetId(1L, 1L, 2L)).thenReturn(false);

        // when
        ResponseEntity<?> response = reviewService.addMemberReview("author@domain.com", "target@domain.com", request);

        // then
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void addMemberReview_AlreadyExists() {
        // given
        Member author = Member.builder().id(1L).name("작성자").build();
        Member target = Member.builder().id(2L).name("대상자").build();

        Party party = Party.builder().id(1L).build();
        PartyMember partyMember = PartyMember.builder().member(target).party(party).build();

        // 💡 파티에 직접 멤버 추가 (리스트 수정)
//        List<PartyMember> partyMemberList = new ArrayList<>();
        party.getPartyMemberList().add(partyMember);
//        party.setPartyMemberList(partyMemberList);

        when(memberRepository.findByEmail("author@domain.com")).thenReturn(Optional.of(author));
        when(memberRepository.findByEmail("target@domain.com")).thenReturn(Optional.of(target));
        when(partyService.findPartyById(1L)).thenReturn(party);
        when(reviewRepository.existsByPartyIdAndAuthorIdAndTargetId(1L, 1L, 2L)).thenReturn(true);

        // when & then
        TagogayoException exception = assertThrows(TagogayoException.class, () ->
                reviewService.addMemberReview("author@domain.com", "target@domain.com",
                        new ReviewMemberRequest(1L, 4.5, "중복 리뷰"))
        );
        assertEquals(ErrorCode.ALREADY_REVIEW, exception.getErrorCode());
    }

    @Test
    void getReviewRateByMember_NoReviews() {
        // given
        Member target = Member.builder().id(2L).build();
        when(reviewRepository.findByTargetId(anyLong())).thenReturn(new ArrayList<>());

        // when
        double rate = reviewService.getReviewRateByMember(target.getId());

        // then
        assertEquals(0.0, rate);
    }

    @Test
    void getReviewRateByMember_SingleReview() {
        // given
        Member target = Member.builder().id(2L).build();
        Review review = Review.builder().score(4.5).build();
        when(reviewRepository.findByTargetId(anyLong())).thenReturn(List.of(review));

        // when
        double rate = reviewService.getReviewRateByMember(target.getId());

        // then
        assertEquals(4.5, rate);
    }

    @Test
    void getReviewRateByMember_MultipleReviews() {
        // given
        Member target = Member.builder().id(2L).build();
        Review review1 = Review.builder().score(4.5).build();
        Review review2 = Review.builder().score(3.0).build();
        when(reviewRepository.findByTargetId(anyLong())).thenReturn(List.of(review1, review2));

        // when
        double rate = reviewService.getReviewRateByMember(target.getId());

        // then
        assertEquals(3.75, rate);
    }
}
