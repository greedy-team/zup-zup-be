package com.greedy.zupzup.pledge.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PledgeService {


    private final LostItemRepository lostItemRepository;
    private final MemberRepository memberRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PledgeRepository pledgeRepository;


    @Transactional
    public Pledge createPledge(Long lostItemId, Long memberId) {

        Member member = memberRepository.getById(memberId);
        LostItem lostItem = lostItemRepository.getById(lostItemId);

        validatePledgeCreation(lostItem, member);

        Pledge pledge = savePledge(lostItem, member);
        lostItem.pledge();

        return pledge;
    }

    private void validatePledgeCreation(LostItem lostItem, Member member) {
        if (!lostItem.isPledgeable()) {
            throw new ApplicationException(LostItemException.ALREADY_PLEDGED);
        }

        if (!lostItem.isEtcCategory()) {
            QuizAttempt quizAttempt = quizAttemptRepository.getByLostItemIdAndMemberId(lostItem.getId(), member.getId());
            if (!quizAttempt.getIsCorrect()) {
                throw new ApplicationException(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED);
            }
        }
    }

    private Pledge savePledge(LostItem lostItem, Member member) {
        Pledge pledge = Pledge.builder()
                .owner(member)
                .lostItem(lostItem)
                .build();
        return pledgeRepository.save(pledge);
    }
}
