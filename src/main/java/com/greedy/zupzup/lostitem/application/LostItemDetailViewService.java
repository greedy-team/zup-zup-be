package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
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
        memberRepository.getById(memberId);

        LostItem li = lostItemRepository.getWithCategoryAndAreaById(lostItemId);

        boolean quizRequired = !li.isNotQuizCategory();
        boolean pledgedByMe = pledgeRepository.existsByLostItem_IdAndOwner_Id(li.getId(), memberId);
        boolean quizAnswered = quizAttemptRepository
                .findByLostItemIdAndMemberId(li.getId(), memberId)
                .map(QuizAttempt::getIsCorrect)
                .orElse(false);

        if (quizRequired && !pledgedByMe) {
            throw new ApplicationException(CommonException.ACCESS_FORBIDDEN);
        }

        List<String> imageUrls = imageRepository.findAllByLostItemIdOrderByImageOrder(li.getId())
                .stream()
                .map(LostItemImage::getImageKey)
                .toList();

        return new LostItemDetailViewCommand(
                li.getId(),
                li.getStatus(),
                li.getCategory().getId(),
                li.getCategory().getName(),
                li.getCategory().getIconUrl(),
                li.getFoundArea().getId(),
                li.getFoundArea().getAreaName(),
                li.getFoundAreaDetail(),
                li.getDescription(),
                imageUrls,
                li.getDepositArea(),
                li.getPledgedAt(),
                li.getCreatedAt(),
                quizRequired,
                quizAnswered,
                pledgedByMe
        );
    }
}
