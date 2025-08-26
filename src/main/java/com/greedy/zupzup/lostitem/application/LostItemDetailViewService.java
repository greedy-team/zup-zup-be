package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemDetailViewService {

    private final LostItemRepository lostItemRepository;
    private final LostItemImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final PledgeRepository pledgeRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    /**
     * 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public LostItemDetailViewCommand getDetail(Long lostItemId, Long memberId) {

        Member member = memberRepository.getById(memberId);
        LostItem item = lostItemRepository.getWithCategoryAndAreaById(lostItemId);

        boolean quizRequired = !item.isNotQuizCategory();

        boolean pledgedByMe = pledgeRepository.existsByLostItem_IdAndOwner_Id(item.getId(), member.getId());

        boolean quizPassed = quizAttemptRepository
                .existsByLostItem_IdAndMember_IdAndIsCorrectTrue(item.getId(), member.getId());

        boolean authorized = quizRequired ? (pledgedByMe && quizPassed) : pledgedByMe;
        if (!authorized) {
            throw new ApplicationException(LostItemException.ACCESS_FORBIDDEN);
        }

        String depositArea = item.getDepositArea();

        List<String> imageUrls = imageRepository.findImageUrlsByLostItemId(item.getId());

        return LostItemDetailViewCommand.of(item, imageUrls, depositArea, quizRequired, quizPassed, pledgedByMe);
    }

    /**
     * 서약 전 사진과 상세 정보 공개
     */
    @Transactional(readOnly = true)
    public LostItemDetailViewCommand getImagesAfterQuiz(Long lostItemId, Long memberId) {
        Member member = memberRepository.getById(memberId);
        LostItem item = lostItemRepository.getWithCategoryAndAreaById(lostItemId);

        boolean quizRequired = !item.isNotQuizCategory();
        boolean quizPassed = quizAttemptRepository
                .existsByLostItem_IdAndMember_IdAndIsCorrectTrue(item.getId(), member.getId());

        boolean authorized = quizRequired ? quizPassed : true;
        if (!authorized) {
            throw new ApplicationException(LostItemException.ACCESS_FORBIDDEN);
        }

        List<String> imageUrls = imageRepository.findImageUrlsByLostItemId(item.getId());

        return LostItemDetailViewCommand.of(
                item, imageUrls, item.getDepositArea(), quizRequired, quizPassed, false
        );
    }

    /**
     * 서약 후 보관 장소 공개
     */
    @Transactional(readOnly = true)
    public String getDepositArea(Long lostItemId, Long memberId) {
        Member member = memberRepository.getById(memberId);
        LostItem item = lostItemRepository.getWithCategoryAndAreaById(lostItemId);

        boolean quizRequired = !item.isNotQuizCategory();
        boolean pledgedByMe = pledgeRepository.existsByLostItem_IdAndOwner_Id(item.getId(), member.getId());
        boolean quizPassed = quizAttemptRepository
                .existsByLostItem_IdAndMember_IdAndIsCorrectTrue(item.getId(), member.getId());

        boolean authorized = quizRequired ? (pledgedByMe && quizPassed) : pledgedByMe;
        if (!authorized) {
            throw new ApplicationException(LostItemException.ACCESS_FORBIDDEN);
        }
        return item.getDepositArea();
    }
}
