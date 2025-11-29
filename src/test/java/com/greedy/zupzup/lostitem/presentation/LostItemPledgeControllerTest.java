package com.greedy.zupzup.lostitem.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.PledgeFixture;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.pledge.domain.Pledge;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class LostItemPledgeControllerTest extends ControllerTest {

    private Member owner;
    private Member other;
    private String ownerToken;
    private String otherToken;
    private LostItem pledgedItem;

    @BeforeEach
    void setUp() {

        // given
        owner = givenMember(123456, "password1");
        other = givenMember(999999, "password2");

        ownerToken = givenAccessToken(owner);
        otherToken = givenAccessToken(other);

        pledgedItem = givenRegisteredLostItem(givenElectronicsCategory());
        pledgedItem.changeStatus(LostItemStatus.PLEDGED);
        lostItemRepository.save(pledgedItem);

        Pledge pledge = PledgeFixture.PLEDGE(owner, pledgedItem);
        pledgeRepository.save(pledge);
    }

    @Test
    void 서약을_성공적으로_취소하면_200과_REGISTERED_상태를_반환해야_한다() {

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .cookie("access_token", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel")
                .then().log().all()
                .extract();

        LostItem updated = lostItemRepository.getById(pledgedItem.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(200);
            softly.assertThat(updated.getStatus()).isEqualTo(LostItemStatus.REGISTERED);
        });
    }

    @Test
    void 본인이_서약한_아이템이_아니면_403_FORBIDDEN을_반환해야_한다() {

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .cookie("access_token", otherToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel")
                .then().log().all()
                .extract();

        ErrorResponse response = extract.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(403);
            softly.assertThat(response.status())
                    .isEqualTo(LostItemException.PLEDGE_NOT_BY_THIS_USER.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(LostItemException.PLEDGE_NOT_BY_THIS_USER.getTitle());
            softly.assertThat(response.detail()).isEqualTo(LostItemException.PLEDGE_NOT_BY_THIS_USER.getDetail());
            softly.assertThat(response.instance())
                    .isEqualTo("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel");
        });
    }

    @Test
    void 서약상태가_아닌_분실물은_취소할_수_없으며_409_CONFLICT를_반환해야_한다() {

        // given
        pledgedItem.changeStatus(LostItemStatus.REGISTERED);
        lostItemRepository.save(pledgedItem);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .cookie("access_token", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel")
                .then().log().all()
                .extract();

        ErrorResponse response = extract.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(409);
            softly.assertThat(response.status())
                    .isEqualTo(LostItemException.INVALID_STATUS_FOR_PLEDGE_CANCEL.getHttpStatus().value());
            softly.assertThat(response.title())
                    .isEqualTo(LostItemException.INVALID_STATUS_FOR_PLEDGE_CANCEL.getTitle());
            softly.assertThat(response.detail())
                    .isEqualTo(LostItemException.INVALID_STATUS_FOR_PLEDGE_CANCEL.getDetail());
            softly.assertThat(response.instance())
                    .isEqualTo("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel");
        });
    }

    @Test
    void pledge_기록이_없으면_404_NOT_FOUND를_반환해야_한다() {

        // given
        pledgeRepository.deleteAll();

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .cookie("access_token", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel")
                .then().log().all()
                .extract();

        ErrorResponse response = extract.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(404);
            softly.assertThat(response.status()).isEqualTo(LostItemException.PLEDGE_NOT_FOUND.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(LostItemException.PLEDGE_NOT_FOUND.getTitle());
            softly.assertThat(response.detail()).isEqualTo(LostItemException.PLEDGE_NOT_FOUND.getDetail());
            softly.assertThat(response.instance())
                    .isEqualTo("/api/lost-items/" + pledgedItem.getId() + "/pledge/cancel");
        });
    }

    @Test
    void 습득_완료에_성공하면_상태가_FOUND로_변경되어야_한다() {

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .cookie("access_token", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/lost-items/" + pledgedItem.getId() + "/found")
                .then().log().all()
                .extract();

        LostItem updated = lostItemRepository.getById(pledgedItem.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(200);
            softly.assertThat(updated.getStatus()).isEqualTo(LostItemStatus.FOUND);
        });
    }
}
