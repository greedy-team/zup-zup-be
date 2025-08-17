package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
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

    @Transactional(readOnly = true)
    public LostItemDetailViewCommand getDetail(Long lostItemId, Long memberId) {

        Member member = memberRepository.getById(memberId);

        LostItem item = lostItemRepository.getWithCategoryAndAreaById(lostItemId);

        boolean quizRequired = !item.isNotQuizCategory();

        boolean pledgedByMe = pledgeRepository.existsByLostItem_IdAndOwner_Id(item.getId(), member.getId());
        boolean quizAnswered = quizAttemptRepository
                .findByLostItemIdAndMemberId(item.getId(), member.getId())
                .map(QuizAttempt::getIsCorrect)
                .orElse(false);

        if (quizRequired && !pledgedByMe) {
            throw new ApplicationException(CommonException.ACCESS_FORBIDDEN);
        }

        String depositArea = item.getDepositArea();

        List<String> imageUrls = imageRepository.findImageUrlsByLostItemId(item.getId());

        return LostItemDetailViewCommand.of(item, imageUrls, depositArea, quizRequired, quizAnswered, pledgedByMe);
    }
}
