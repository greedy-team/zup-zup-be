package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.pledge.domain.Pledge;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class LostItemPledgeServiceTest extends ServiceUnitTest {

    @InjectMocks
    private LostItemPledgeService lostItemPledgeService;

    @Test
    void 서약_취소가_정상적으로_성공해야_한다() {

        // given
        Long memberId = 1L;
        Long lostItemId = 10L;

        LostItem lostItem = mock(LostItem.class);
        Pledge pledge = mock(Pledge.class);

        given(lostItemRepository.findById(lostItemId))
                .willReturn(Optional.of(lostItem));
        given(lostItem.getStatus()).willReturn(LostItemStatus.PLEDGED);

        given(pledgeRepository.findByLostItemId(lostItemId))
                .willReturn(Optional.of(pledge));

        given(pledge.getLostItem()).willReturn(lostItem);

        // when
        var result = lostItemPledgeService.cancelPledge(memberId, lostItemId);

        // then
        assertThat(result.status()).isEqualTo("REGISTERED");

        then(pledge).should().validateOwner(memberId);
        then(pledgeRepository).should().delete(pledge);
        then(lostItem).should().changeStatus(LostItemStatus.REGISTERED);
    }

    @Test
    void 본인이_서약하지_않은_분실물은_취소할_수_없다() {

        // given
        Long memberId = 1L;
        Long lostItemId = 10L;

        LostItem lostItem = mock(LostItem.class);
        Pledge pledge = mock(Pledge.class);

        given(lostItemRepository.findById(lostItemId))
                .willReturn(Optional.of(lostItem));
        given(lostItem.getStatus()).willReturn(LostItemStatus.PLEDGED);
        given(pledgeRepository.findByLostItemId(lostItemId))
                .willReturn(Optional.of(pledge));

        willThrow(new ApplicationException(LostItemException.PLEDGE_NOT_BY_THIS_USER))
                .given(pledge).validateOwner(memberId);

        // when & then
        assertThatThrownBy(() ->
                lostItemPledgeService.cancelPledge(memberId, lostItemId)
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(LostItemException.PLEDGE_NOT_BY_THIS_USER.getDetail());

        then(pledgeRepository).should(never()).delete(any());
    }

    @Test
    void 서약상태가_아닌_분실물은_취소할_수_없다() {

        // given
        Long memberId = 1L;
        Long lostItemId = 10L;

        LostItem lostItem = mock(LostItem.class);
        given(lostItemRepository.findById(lostItemId))
                .willReturn(Optional.of(lostItem));

        given(lostItem.getStatus()).willReturn(LostItemStatus.REGISTERED);

        // when & then
        assertThatThrownBy(() ->
                lostItemPledgeService.cancelPledge(memberId, lostItemId)
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(LostItemException.INVALID_STATUS_FOR_PLEDGE_CANCEL.getDetail());

        then(pledgeRepository).should(never()).delete(any());
    }

    @Test
    void pledge_기록이_없으면_취소할_수_없다() {

        // given
        Long memberId = 1L;
        Long lostItemId = 10L;

        LostItem lostItem = mock(LostItem.class);

        given(lostItemRepository.findById(lostItemId))
                .willReturn(Optional.of(lostItem));
        given(lostItem.getStatus()).willReturn(LostItemStatus.PLEDGED);

        given(pledgeRepository.findByLostItemId(lostItemId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                lostItemPledgeService.cancelPledge(memberId, lostItemId)
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(LostItemException.PLEDGE_NOT_FOUND.getDetail());
    }

    @Test
    void 습득처리가_정상적으로_완료되어야_한다() {

        // given
        Long memberId = 1L;
        Long lostItemId = 10L;

        LostItem lostItem = mock(LostItem.class);
        Pledge pledge = mock(Pledge.class);

        given(lostItemRepository.findById(lostItemId))
                .willReturn(Optional.of(lostItem));
        given(lostItem.getStatus()).willReturn(LostItemStatus.PLEDGED);

        given(pledgeRepository.findByLostItemId(lostItemId))
                .willReturn(Optional.of(pledge));

        given(pledge.getLostItem()).willReturn(lostItem);

        // when
        var result = lostItemPledgeService.completeFound(memberId, lostItemId);

        // then
        assertThat(result.status()).isEqualTo("FOUND");

        then(pledge).should().validateOwner(memberId);
        then(lostItem).should().changeStatus(LostItemStatus.FOUND);
        then(pledgeRepository).should().delete(pledge);
    }
}
