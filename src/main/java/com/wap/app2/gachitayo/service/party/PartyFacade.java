package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.*;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.request.*;
import com.wap.app2.gachitayo.dto.response.*;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.service.fare.PaymentStatusService;
import com.wap.app2.gachitayo.service.location.StopoverFacade;
import com.wap.app2.gachitayo.service.member.MemberService;
import com.wap.app2.gachitayo.service.websocket.PartyJoinRequestWebSocketUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartyFacade {
    private final PartyService partyService;
    private final PaymentStatusService paymentStatusService;
    private final MemberService memberService;
    private final PartyMemberService partyMemberService;
    private final StopoverFacade stopoverFacade;
    private final PartyJoinRequestService partyJoinRequestService;
    private final PartyJoinRequestWebSocketUtils partyJoinRequestWebSocketUtils;

    private final StopoverMapper stopoverMapper;

    public ResponseEntity<?> createParty(String email, PartyCreateRequestDto requestDto) {
        // 1. 사용자 조회
        Member host = memberService.getUserByEmail(email);
        if(host == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        
        // 2. 출발/목적지 생성
        Stopover start = stopoverFacade.createStopover(
                requestDto.getStartLocation(), LocationType.START
        );
        Stopover dest = stopoverFacade.createStopover(
                requestDto.getDestinationLocation(), LocationType.DESTINATION
        );

        // 3. 파티 생성
        Party party = Party.builder()
                .stopovers(List.of(start, dest))
                .maxPeople(requestDto.getMaxPerson())
                .allowRadius(requestDto.getRadius())
                .genderOption(resolveGenderOption(requestDto.getGenderOption(), host))
                .build();

        // 4. Stopover - Party 연결
        start.setParty(party);
        dest.setParty(party);

        // 5. PartyMember 생성
        PartyMember hostMember = partyMemberService.connectMemberWithParty(party, host, PartyMemberRole.HOST);

        // 6. 목적지 - PaymentStatus 연결
        PaymentStatus paymentStatus = paymentStatusService.connectPartyMemberWithStopover(hostMember, dest);
        dest.addPaymentStatus(paymentStatus);

        // 7. 파티 저장
        Party reflectedParty = partyService.saveParty(party);

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(reflectedParty.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티가 생성되었습니다.".formatted(reflectedParty.getId()), PartyEventType.PARTY_CREATE);

        return ResponseEntity.ok().body(toPartyCreateResponseDto(party, host, start, dest));
    }

    public ResponseEntity<?> attendParty(String email, Long partyId) {
        // 1. Party 조회
        Party party = partyService.findPartyById(partyId);

        // 2. Member 조회
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        // 3. Validation
        if (party.getPartyMemberList().size() >= party.getMaxPeople()) {
            throw new TagogayoException(ErrorCode.EXCEED_PARTY_MEMBER);
        }
        if (partyMemberService.isInParty(party, member)) {
            throw new TagogayoException(ErrorCode.ALREADY_PARTY_MEMBER);
        }
        validateGenderOption(party.getGenderOption(), member.getGender());

        // 4. PartyMember 등록
        partyMemberService.connectMemberWithParty(party, member, PartyMemberRole.MEMBER);

        // 5. Map 으로 간단히 응답
        return ResponseEntity.ok(Map.of(
                "message", "파티 참가 요청이 완료되었습니다.",
                "party_id", party.getId(),
                "party_members", party.getPartyMemberList().stream()
                                        .map(pm -> PartyMemberResponseDto.builder()
                                        .id(pm.getId())
                                        .name(pm.getMember().getName())
                                        .email(pm.getMember().getEmail())
                                        .gender(pm.getMember().getGender())
                                        .role(pm.getMemberRole())
                                        .additionalRole(pm.getAdditionalRole())
                                        .build()).toList()
        ));
    }

    public ResponseEntity<?> addStopoverToParty(String email, Long partyId, StopoverAddRequestDto requestDto) {
        log.info("\n===== 파티 내 하차 지점 추가 시도 =====");

        // 1. 파티, HOST 검증
        Party party = verifyPartyAndPartyMember(email, partyId, PartyMemberRole.HOST);

        // 2. 추가할 참가자 Member 검증
        Member participant = memberService.getUserByEmail(requestDto.getMemberEmail());
        if (participant == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        PartyMember participantMember = partyMemberService.getPartyMemberByPartyAndMember(party, participant);

        // 3. Stopover 찾거나 생성
        Stopover stopover = stopoverFacade.findOrCreateStopover(party, requestDto.getLocation());

        // 4. PaymentStatus 연결
        PaymentStatus paymentStatus = paymentStatusService.connectPartyMemberWithStopover(participantMember, stopover);
        stopover.addPaymentStatus(paymentStatus);

        log.info("\n===== 하차 지점 추가 성공 =====");

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티의 정보가 업데이트되었습니다.".formatted(party.getId()), PartyEventType.PARTY_UPDATE);
        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, "하차 지점 id: %d에 해당하는 하차 지점이 추가되었습니다. %s님이 내립니다.".formatted(party.getId(), participantMember.getMember().getName()), PartyEventType.PARTY_UPDATE);

        return ResponseEntity.ok(toStopoverAndPartyMemberResponseDtoList(party));
    }

    public ResponseEntity<?> updateStopover(String email, Long partyId, StopoverUpdateDto updateDto) {
        log.info("\n===== 파티 내 하차 지점 수정 시도 =====");

        // 1. 파티, HOST 검증
        Party party = verifyPartyAndPartyMember(email, partyId, PartyMemberRole.HOST);

        // 2. Stopover 조회
        Stopover stopover = party.getStopovers().stream()
                .filter(s -> s.getId().equals(updateDto.getStopoverId()))
                .findFirst()
                .orElseThrow(() -> new TagogayoException(ErrorCode.STOPOVER_NOT_FOUND));

        // 3. Stopover 수정
        boolean isUpdatedStopover = stopoverFacade.updateStopover(stopover, updateDto.getLocation(), updateDto.getStopoverType());

        // 4. PaymentStatus (참가자 변경 시) 수정
        boolean isUpdatedPaymentStatus = false;
        if (updateDto.getMemberEmail() != null) {
            Member member = memberService.getUserByEmail(updateDto.getMemberEmail());
            if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

            PartyMember partyMember = partyMemberService.getPartyMemberByPartyAndMember(party, member);

            isUpdatedPaymentStatus = paymentStatusService.updateStopover(partyMember, stopover);
        }

        // 5. 결과 반환
        if (isUpdatedStopover || isUpdatedPaymentStatus) {
            log.info("\n===== 수정 사항 성공 반영 =====");
            partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티의 정보가 업데이트되었습니다.".formatted(party.getId()), PartyEventType.PARTY_UPDATE);
            partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, "하차 지점 id: %d에 해당하는 하차 지점이 수정되었습니다.".formatted(stopover.getId()), PartyEventType.PARTY_UPDATE);
            return ResponseEntity.ok(toStopoverAndPartyMemberResponseDtoList(party));
        }

        log.info("\n===== 수정 사항 없음 =====");
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPartiesWithDestinationLocation(String email, PartySearchRequestDto requestDto) {
        log.info("\n===== 위치 기반 파티 검색 시도 =====");

        // 1. 현재 사용자 조회
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        // 2. 반경 내 파티 조회
        List<Party> parties = partyService.findPartiesWithinRadius(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius());

        // 3. 필터링 (인원수, 성별옵션, 참가 여부)
        List<PartyResponseDto> partyDtos = parties.stream()
                .filter(party -> {
                    int currentPeople = party.getPartyMemberList().size();
                    String memberGender = member.getGender().name();
                    String partyOption = party.getGenderOption().name().substring(5);

                    return currentPeople != party.getMaxPeople()
                            && !partyMemberService.isInParty(party, member)
                            && (party.getGenderOption().equals(GenderOption.MIXED) || memberGender.equalsIgnoreCase(partyOption));
                })
                .map(party -> PartyResponseDto.builder()
                        .id(party.getId())
                        .members(partyMemberService.getPartyMemberResponseDtoList(party))
                        .stopovers(party.getStopovers().stream().map(stopoverMapper::toDto).toList())
                        .radius(party.getAllowRadius())
                        .maxPeople(party.getMaxPeople())
                        .currentPeople(party.getPartyMemberList().size())
                        .genderOption(party.getGenderOption())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(partyDtos);
    }

    // 재조회 등으로 파티 상태를 최신 상태로 유지하기 위한 조회용 메서드
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPartyDetailsById(String email, Long partyId) {
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Party party = partyService.findPartyWithStopovers(partyId);
        if (!partyMemberService.isInParty(party, member)) {
            throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
        }

        return ResponseEntity.ok(toPartyResponseDto(party));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyPartyList(String email) {
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        List<Party> partyList = partyService.findPartiesWithDetailsByMember(member.getId());
        List<PartyResponseDto> responseDtoList = partyList.stream().map(this::toPartyResponseDto).toList();
        return ResponseEntity.ok(responseDtoList);
    }

    public ResponseEntity<?> electBookkeeper(Long partyId, String hostEmail, Long targetPartyMemberId) {
        Party party = verifyPartyAndPartyMember(hostEmail, partyId, PartyMemberRole.HOST);

        PartyMember targetPartyMember = partyMemberService.getPartyMemberById(targetPartyMemberId);

        for(PartyMember pm : party.getPartyMemberList()) {
            if (pm.getMemberRole().equals(PartyMemberRole.HOST) && pm.getAdditionalRole().equals(AdditionalRole.BOOKKEEPER)) {
                partyMemberService.changeAdditionalRole(pm, AdditionalRole.NONE);
            }
            else if (pm.getMemberRole().equals(PartyMemberRole.BOOKKEEPER)) {
                partyMemberService.changePartyMemberRole(pm, PartyMemberRole.MEMBER);
            }
        }

        if(targetPartyMember.getMemberRole().equals(PartyMemberRole.HOST)) {
            partyMemberService.changeAdditionalRole(targetPartyMember, AdditionalRole.BOOKKEEPER);
        } else if (targetPartyMember.getMemberRole().equals(PartyMemberRole.MEMBER)) {
            partyMemberService.changePartyMemberRole(targetPartyMember, PartyMemberRole.BOOKKEEPER);
        }

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티의 정보가 업데이트되었습니다.".formatted(party.getId()), PartyEventType.PARTY_UPDATE);
        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, "%s님이 파티의 최종 결산자가 되었습니다.".formatted(targetPartyMember.getMember().getName()), PartyEventType.PARTY_UPDATE);

        return ResponseEntity.ok(party.getPartyMemberList().stream().map(this::toPartyMemberResponseDto).toList());
    }

    public ResponseEntity<?> reflectCalculatedFare(Long partyId, String bookkeeperEmail, List<FareRequestDto> requestDtoList) {
        Party party = verifyPartyAndPartyMember(bookkeeperEmail, partyId, PartyMemberRole.BOOKKEEPER);

        stopoverFacade.calculateFinalFare(requestDtoList, party.getStopovers());
        partyService.saveParty(party);

        // Party + Stopover
        Party updatedParty = partyService.findPartyWithStopovers(partyId);
        // PaymentStatus + PartyMember + Member
        List<PaymentStatus> paymentStatusList = paymentStatusService.findPaymentStatusListByStopoverIn(updatedParty.getStopovers());
        Map<Long, List<PaymentStatus>> paymentStatusMap = paymentStatusList.stream()
                .collect(Collectors.groupingBy(ps -> ps.getStopover().getId()));

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티의 정보가 업데이트되었습니다.".formatted(party.getId()), PartyEventType.PARTY_UPDATE);
        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, "파티의 최종 정산 금액이 반영되었습니다.", PartyEventType.PARTY_UPDATE);

        return toFinalPaymentStatusResponseDto(paymentStatusMap, updatedParty);
    }

    public ResponseEntity<?> reflectPayment(Long partyId, String bookkeeperEmail, FareConfirmRequestDto requestDto) {
        Party party = verifyPartyAndPartyMember(bookkeeperEmail, partyId, PartyMemberRole.BOOKKEEPER);

        Stopover targetStopover = party.getStopovers().stream()
                .filter(s -> s.getId().equals(requestDto.getStopoverId())).findFirst().orElseThrow(() -> new TagogayoException(ErrorCode.STOPOVER_NOT_FOUND));
        PaymentStatus targetStatus = targetStopover.getPaymentStatusList().stream()
                .filter(ps -> ps.getPartyMember().getId().equals(requestDto.getPartyMemberId())).findFirst().orElseThrow(() -> new TagogayoException(ErrorCode.PAYMENT_STATUS_NOT_FOUND));

        if (targetStatus.isPaid()) {
            return ResponseEntity.noContent().build();
        }

        paymentStatusService.updatePaidStatus(targetStatus, true);

        //아낀 금액 추가
        paymentStatusService.saveSavedMoney(targetStatus);

        // Party + Stopover
        Party updatedParty = partyService.findPartyWithStopovers(partyId);
        // PaymentStatus + PartyMember + Member
        List<PaymentStatus> paymentStatusList = paymentStatusService.findPaymentStatusListByStopoverIn(updatedParty.getStopovers());
        Map<Long, List<PaymentStatus>> paymentStatusMap = paymentStatusList.stream()
                .collect(Collectors.groupingBy(ps -> ps.getStopover().getId()));

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티의 정보가 업데이트되었습니다.".formatted(party.getId()), PartyEventType.PARTY_UPDATE);
        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, "%s님의 지불이 확인되었습니다.".formatted(targetStatus.getPartyMember().getMember().getName()), PartyEventType.PARTY_UPDATE);

        return toFinalPaymentStatusResponseDto(paymentStatusMap, updatedParty);
    }

    // 파티 업데이트 등으로 재조회를 위한 단순 조회용 메서드
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentStatusListByPartyId(String email, Long partyId) {
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Party targetParty = partyService.findPartyWithStopovers(partyId);
        if (!partyMemberService.isInParty(targetParty, member)) {
            throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
        }
        List<PaymentStatus> paymentStatusList = paymentStatusService.findPaymentStatusListByStopoverIn(targetParty.getStopovers());
        Map<Long, List<PaymentStatus>> paymentStatusMap = paymentStatusList.stream()
                .collect(Collectors.groupingBy(ps -> ps.getStopover().getId()));
        return toFinalPaymentStatusResponseDto(paymentStatusMap, targetParty);
    }

    // 1. 참가 요청 생성
    public void requestToJoinParty(Long partyId, String email) {
        Member requester = memberService.getUserByEmail(email);
        if (requester == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Party party = partyService.findPartyById(partyId);
        if (party.getPartyMemberList().size() >= party.getMaxPeople()) {
            throw new TagogayoException(ErrorCode.EXCEED_PARTY_MEMBER);
        }
        if (partyMemberService.isInParty(party, requester)) {
            throw new TagogayoException(ErrorCode.ALREADY_PARTY_MEMBER);
        }
        validateGenderOption(party.getGenderOption(), requester.getGender());

        if (partyJoinRequestService.isAlreadyRequested(requester, party, JoinRequestStatus.PENDING)) {
            throw new TagogayoException(ErrorCode.ALREADY_JOIN_REQUEST);
        }

        PartyJoinRequest pendingRequest = partyJoinRequestService.requestJoin(requester, party, JoinRequestStatus.PENDING);
        pendingRequest.setRespondedAt(LocalDateTime.now());

        PartyMember host = party.getPartyMemberList().stream()
                .filter(pm -> pm.getMemberRole().equals(PartyMemberRole.HOST)).findFirst().orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));

        partyJoinRequestWebSocketUtils.notifyParticipants(pendingRequest, host, JoinRequestStatus.PENDING, "파티 참가 요청을 보냈습니다.", requester.getName() + "님이 해당 파티에 참가 요청을 보냈습니다.");

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(party.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, requester.getName() + "님이 해당 파티에 참가 요청을 보냈습니다.", PartyEventType.MEMBER_JOIN);
    }

    // 2. 참가 요청 수락
    public void acceptJoinRequest(Long partyId, PartyJoinRequestSocketRequestDto requestDto, String hostMemberEmail) {
        PartyJoinRequest request = partyJoinRequestService.findJoinRequestByIdWithLock(requestDto.getRequestId());
        if (!partyId.equals(request.getParty().getId())) {
            throw new TagogayoException(ErrorCode.JOIN_REQUEST_NOT_MATCH_PARTY);
        }

        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new TagogayoException(ErrorCode.ALREADY_REQUEST_HANDLED);
        }

        Party targetParty = verifyPartyAndPartyMember(hostMemberEmail, partyId, PartyMemberRole.HOST);
        PartyMember hostMember = targetParty.getPartyMemberList().stream()
                .filter(pm -> pm.getMemberRole().equals(PartyMemberRole.HOST)).findFirst().orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));

        if (targetParty.getPartyMemberList().size() >= targetParty.getMaxPeople()) {
            partyJoinRequestWebSocketUtils.notifyParticipants(request, hostMember, JoinRequestStatus.REJECTED, "파티 정원이 초과되어 자동으로 요청이 거절되었습니다.", "파티 정원 초과로 요청 수락이 진행되지 않았습니다.");
            request.setStatus(JoinRequestStatus.AUTO_REJECTED);
            request.setRespondedAt(LocalDateTime.now());
            return;
        }

        request.setStatus(JoinRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());

        partyMemberService.connectMemberWithParty(targetParty, request.getRequester(), PartyMemberRole.MEMBER);

        // 요청자, 방장에게 알림
        partyJoinRequestWebSocketUtils.notifyParticipants(request, hostMember, JoinRequestStatus.ACCEPTED, "파티 참가 요청이 수락되었습니다.", request.getRequester().getName() + "님의 요청을 수락하였습니다.");

        // 브로드 캐스트는 파티에 실질적인 업데이트가 발생해야
        // 알림을 통해 재조회 등을 수행하는 것이 이상적.
        // 따라서 수락만 브로드캐스트 알림하면 될 듯함.
        // 해당 파티원들 내부 브로드캐스트 채널에 알림
        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(targetParty.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, request.getRequester().getName() + "님이 참가하였습니다.", PartyEventType.MEMBER_JOIN);

        // 해당 파티 외부의 유저들 브로드캐스트 채널에 알림
        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(targetParty.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_EXTERNAL, "파티 id: %d에 해당하는 파티가 업데이트되었습니다.".formatted(targetParty.getId()), PartyEventType.PARTY_UPDATE);
    }

    // 3. 참가 요청 거절
    public void rejectJoinRequest(Long partyId, PartyJoinRequestSocketRequestDto requestDto, String hostMemberEmail) {
        PartyJoinRequest request = partyJoinRequestService.findJoinRequestById(requestDto.getRequestId());
        if (!partyId.equals(request.getParty().getId())) {
            throw new TagogayoException(ErrorCode.JOIN_REQUEST_NOT_MATCH_PARTY);
        }
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new TagogayoException(ErrorCode.ALREADY_REQUEST_HANDLED);
        }

        Party targetParty = verifyPartyAndPartyMember(hostMemberEmail, request.getParty().getId(), PartyMemberRole.HOST);

        request.setStatus(JoinRequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());

        PartyMember hostMember = targetParty.getPartyMemberList().stream().filter(pm -> pm.getMemberRole().equals(PartyMemberRole.HOST)).findFirst().orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));

        partyJoinRequestWebSocketUtils.notifyParticipants(request, hostMember, JoinRequestStatus.REJECTED, "파티 참가 요청이 거절되었습니다.", request.getRequester().getName() + "님의 요청을 거절하였습니다.");

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(targetParty.getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, request.getRequester().getName() + "님의 파티 참가 요청을 거절하였습니다.", PartyEventType.MEMBER_REJECT);
    }

    // 4. 참가 요청 취소
    public void cancelJoinRequest(Long partyId, PartyJoinRequestSocketRequestDto requestDto, String memberEmail) {
        PartyJoinRequest request = partyJoinRequestService.findJoinRequestByIdWithRequester(requestDto.getRequestId());
        if (!partyId.equals(request.getParty().getId())) {
            throw new TagogayoException(ErrorCode.JOIN_REQUEST_NOT_MATCH_PARTY);
        }
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new TagogayoException(ErrorCode.ALREADY_REQUEST_HANDLED);
        }
        if (!request.getRequester().getEmail().equals(memberEmail)) {
            throw new TagogayoException(ErrorCode.JOIN_REQUEST_NOT_MATCH_REQUESTER);
        }

        Member requester = memberService.getUserByEmail(memberEmail);
        if (requester == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        request.setStatus(JoinRequestStatus.CANCELED);
        request.setRespondedAt(LocalDateTime.now());

        PartyMember host = request.getParty().getPartyMemberList().stream()
                .filter(pm -> pm.getMemberRole().equals(PartyMemberRole.HOST)).findFirst().orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));

        partyJoinRequestWebSocketUtils.notifyParticipants(request, host, JoinRequestStatus.CANCELED, "파티 참가 요청이 취소되었습니다.", requester.getName() + "님이 요청을 취소하였습니다.");

        partyJoinRequestWebSocketUtils.broadcastPartyUpdate(request.getParty().getId(), PartyJoinRequestWebSocketUtils.BROADCAST_FOR_INTERNAL, requester.getName() + "님이 파티 참가 요청을 취소하였습니다.", PartyEventType.MEMBER_CANCEL);
    }

    private ResponseEntity<?> toFinalPaymentStatusResponseDto(Map<Long, List<PaymentStatus>> paymentStatusMap, Party updatedParty) {
        List<FinalPaymentStatusResponseDto> responseDtoList = updatedParty.getStopovers().stream()
                .filter(stopover -> !stopover.getStopoverType().equals(LocationType.START))
                .flatMap(stopover -> {
                    List<PaymentStatus> psList = paymentStatusMap.getOrDefault(stopover.getId(), List.of());
                    return psList.stream()
                            .map(ps -> FinalPaymentStatusResponseDto.builder()
                                    .partyMemberInfo(toPartyMemberResponseDto(ps.getPartyMember()))
                                    .paymentStatus(toPaymentStatusResponseDto(stopover, ps))
                                    .build());
                })
                .toList();
        return ResponseEntity.ok(responseDtoList);
    }

    private PaymentStatusResponseDto toPaymentStatusResponseDto(Stopover stopover, PaymentStatus paymentStatus) {
        return PaymentStatusResponseDto.builder()
                .stopoverId(stopover.getId())
                .baseFare(stopover.getFare().getBaseFigure())
                .finalFare(paymentStatus.getFinalFigure())
                .isPaid(paymentStatus.isPaid())
                .build();
    }

    private PartyResponseDto toPartyResponseDto(Party party) {
        return PartyResponseDto.builder()
                .id(party.getId())
                .members(partyMemberService.getPartyMemberResponseDtoList(party))
                .stopovers(party.getStopovers().stream().map(stopoverMapper::toDto).toList())
                .radius(party.getAllowRadius())
                .maxPeople(party.getMaxPeople())
                .currentPeople(party.getPartyMemberList().size())
                .genderOption(party.getGenderOption())
                .build();
    }

    private PartyCreateResponseDto toPartyCreateResponseDto(Party partyEntity, Member member, Stopover startStopover, Stopover destStopover) {
        return PartyCreateResponseDto.builder()
                .id(partyEntity.getId())
                .hostName(member.getName())
                .hostEmail(member.getEmail())
                .startLocation(stopoverMapper.toDto(startStopover))
                .destination(stopoverMapper.toDto(destStopover))
                .maxPeople(partyEntity.getMaxPeople())
                .radius(partyEntity.getAllowRadius())
                .genderOption(partyEntity.getGenderOption())
                .build();
    }

    private List<StopoverAndPartyMemberResponseDto> toStopoverAndPartyMemberResponseDtoList(Party party) {
        return party.getStopovers().stream()
                .map(s -> StopoverAndPartyMemberResponseDto.builder()
                        .stopover(stopoverMapper.toDto(s))
                        .partyMembers(
                            s.getPaymentStatusList().stream()
                                .map(ps -> toPartyMemberResponseDto(ps.getPartyMember())
                                ).toList()
                        )
                        .build()
                ).toList();
    }

    private PartyMemberResponseDto toPartyMemberResponseDto(PartyMember partyMember) {
        return PartyMemberResponseDto.builder()
                .id(partyMember.getId())
                .name(partyMember.getMember().getName())
                .email(partyMember.getMember().getEmail())
                .gender(partyMember.getMember().getGender())
                .role(partyMember.getMemberRole())
                .additionalRole(partyMember.getAdditionalRole())
                .build();
    }

    private Party verifyPartyAndPartyMember(String email, Long partyId, PartyMemberRole role) {
        Party party = partyService.findPartyWithStopovers(partyId);

        Member hostMember = memberService.getUserByEmail(email);
        if (hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        PartyMember partyMember = partyMemberService.getPartyMemberByPartyAndMember(party, hostMember);
        validatePartyMemberRole(partyMember, role);

        return party;
    }

    private void validatePartyMemberRole(PartyMember partyMember, PartyMemberRole requiredRole) {
        switch (requiredRole) {
            case HOST -> {
                if(!partyMember.getMemberRole().equals(PartyMemberRole.HOST)) throw new TagogayoException(ErrorCode.NOT_HOST);
            }
            case BOOKKEEPER -> {
                if(partyMember.getMemberRole().equals(PartyMemberRole.MEMBER)) throw new TagogayoException(ErrorCode.NOT_BOOKKEEPER);
                else if(partyMember.getMemberRole().equals(PartyMemberRole.HOST) && !partyMember.getAdditionalRole().equals(AdditionalRole.BOOKKEEPER)) throw new TagogayoException(ErrorCode.NOT_BOOKKEEPER);
            }
        }
    }

    private void validateGenderOption(GenderOption genderOption, Gender memberGender) {
        if (genderOption == GenderOption.MIXED) return;
        String optionGender = genderOption.name().substring(5); // "ONLY_MALE" → "MALE"
        if (!optionGender.equalsIgnoreCase(memberGender.name())) {
            throw new TagogayoException(ErrorCode.NOT_MATCH_GENDER_OPTION);
        }
    }

    private GenderOption resolveGenderOption(RequestGenderOption requestGenderOption, Member host) {
        if(requestGenderOption == RequestGenderOption.ONLY){
            if(host.getGender() == Gender.MALE) return GenderOption.ONLY_MALE;
            else return GenderOption.ONLY_FEMALE;
        }
        return GenderOption.MIXED;
    }
}