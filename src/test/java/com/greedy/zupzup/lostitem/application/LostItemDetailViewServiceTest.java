package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.common.fixture.LostItemFixture;
import com.greedy.zupzup.common.fixture.MemberFixture;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.member.domain.Member;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

class LostItemDetailViewServiceTest extends ServiceUnitTest {

    @InjectMocks
    private LostItemDetailViewService service;

    private static final Long MEMBER_ID = 1L;
    private static final Long ITEM_ID   = 10L;
    private static final Long NOT_FOUND = 99999L;

    private Member member;
    private LostItem 전자기기_서약완료;
    private LostItem 전자기기_서약미완료;
    private LostItem 기타카테고리_서약미완료;

    @BeforeEach
    void setUp() {
        member = MemberFixture.MEMBER();
        setId(member, MEMBER_ID);
        given(memberRepository.getById(MEMBER_ID)).willReturn(member);

        전자기기_서약완료 = LostItemFixture.ALREADY_PLEDGED_LOST_ITEM();
        setId(전자기기_서약완료, ITEM_ID);
        ReflectionTestUtils.setField(전자기기_서약완료, "foundAreaDetail", "AI 센터 B205");
        ReflectionTestUtils.setField(전자기기_서약완료, "description", "검정색 아이폰");
        ReflectionTestUtils.setField(전자기기_서약완료, "depositArea", "학술정보원 2층 데스크");

        전자기기_서약미완료 = LostItemFixture.PLEDGEABLE_ELECTRONIC_LOST_ITEM();
        setId(전자기기_서약미완료, ITEM_ID);
        ReflectionTestUtils.setField(전자기기_서약미완료, "foundAreaDetail", "AI 센터 B205");
        ReflectionTestUtils.setField(전자기기_서약미완료, "description", "검정색 아이폰");
        ReflectionTestUtils.setField(전자기기_서약미완료, "depositArea", "학술정보원 2층 데스크");

        기타카테고리_서약미완료 = LostItemFixture.NON_QUIZ_CATEGORY_LOST_ITEM();
        setId(기타카테고리_서약미완료, ITEM_ID);
        ReflectionTestUtils.setField(기타카테고리_서약미완료, "foundAreaDetail", "학생회관 1층");
        ReflectionTestUtils.setField(기타카테고리_서약미완료, "description", "키링");
        ReflectionTestUtils.setField(기타카테고리_서약미완료, "depositArea", "학생회관 1층 보관소");

        lenient().when(lostItemImageRepository.findImageUrlsByLostItemId(ITEM_ID))
                .thenReturn(List.of("https://example.com/default-image.jpg"));

        lenient().when(lostItemRepository.getWithCategoryAndAreaById(NOT_FOUND))
                .thenThrow(new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }

    @Nested
    class GetImagesAfterQuiz {

        @Test
        void 퀴즈_통과시_200_OK를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(전자기기_서약미완료);
            given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectTrue(ITEM_ID, MEMBER_ID))
                    .willReturn(true);

            LostItemDetailViewCommand result = service.getImagesAfterQuiz(ITEM_ID, MEMBER_ID);

            assertThat(result).isNotNull();
            assertThat(result.quizRequired()).isTrue();
            assertThat(result.quizAnswered()).isTrue();
            assertThat(result.imageUrls()).isNotEmpty();
        }

        @Test
        void 퀴즈_미통과면_403_FORBIDDEN를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(전자기기_서약완료);
            given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectTrue(ITEM_ID, MEMBER_ID))
                    .willReturn(false);

            assertThatThrownBy(() -> service.getImagesAfterQuiz(ITEM_ID, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        void 분실물_없으면_404_NOT_FOUND를_응답합니다() {
            assertThatThrownBy(() -> service.getImagesAfterQuiz(NOT_FOUND, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        void 비퀴즈_카테고리는_바로_200_OK를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(기타카테고리_서약미완료);

            LostItemDetailViewCommand result = service.getImagesAfterQuiz(ITEM_ID, MEMBER_ID);

            assertThat(result).isNotNull();
            assertThat(result.quizRequired()).isFalse();
        }
    }

    @Nested
    class GetDepositArea {

        @Test
        void 퀴즈_통과_및_서약완료면_보관위치와_200_OK를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(전자기기_서약완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(true);
            given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectTrue(ITEM_ID, MEMBER_ID))
                    .willReturn(true);

            String area = service.getDepositArea(ITEM_ID, MEMBER_ID);

            assertThat(area).isEqualTo("학술정보원 2층 데스크");
        }

        @Test
        void 분실물_없으면_404_NOT_FOUND를_응답합니다() {
            assertThatThrownBy(() -> service.getDepositArea(NOT_FOUND, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        void 비퀴즈_카테고리는_서약만_되면_200_OK를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(기타카테고리_서약미완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(true);

            String area = service.getDepositArea(ITEM_ID, MEMBER_ID);

            assertThat(area).isEqualTo("학생회관 1층 보관소");
        }

        @Test
        void 비퀴즈_서약_안했으면_403_FORBIDDEN를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(기타카테고리_서약미완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(false);

            assertThatThrownBy(() -> service.getDepositArea(ITEM_ID, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    @Nested
    class GetDetail {

        @Test
        void 퀴즈_통과_및_서약완료면_200_OK를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(전자기기_서약완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(true);
            given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectTrue(ITEM_ID, MEMBER_ID))
                    .willReturn(true);

            LostItemDetailViewCommand result = service.getDetail(ITEM_ID, MEMBER_ID);

            assertThat(result).isNotNull();
            assertThat(result.depositArea()).isEqualTo("학술정보원 2층 데스크");
        }

        @Test
        void 서약만_했고_퀴즈미통과면_403_FORBIDDEN을_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(전자기기_서약완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(true);
            given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectTrue(ITEM_ID, MEMBER_ID))
                    .willReturn(false);

            assertThatThrownBy(() -> service.getDetail(ITEM_ID, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        void 분실물_없으면_404_NOT_FOUND를_응답합니다() {
            assertThatThrownBy(() -> service.getDetail(NOT_FOUND, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        void 비퀴즈_카테고리는_서약만_되면_보관장소와_200_OK를_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(기타카테고리_서약미완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(true);

            LostItemDetailViewCommand result = service.getDetail(ITEM_ID, MEMBER_ID);

            assertThat(result).isNotNull();
            assertThat(result.depositArea()).isEqualTo("학생회관 1층 보관소");
        }

        @Test
        void 서약_안했으면_403_FORBIDDEN을_응답합니다() {
            given(lostItemRepository.getWithCategoryAndAreaById(ITEM_ID)).willReturn(전자기기_서약완료);
            given(pledgeRepository.existsByLostItem_IdAndOwner_Id(ITEM_ID, MEMBER_ID)).willReturn(false);

            assertThatThrownBy(() -> service.getDetail(ITEM_ID, MEMBER_ID))
                    .isInstanceOf(ApplicationException.class);
        }
    }
}
