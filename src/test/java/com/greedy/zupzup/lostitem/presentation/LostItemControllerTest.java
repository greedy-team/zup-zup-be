package com.greedy.zupzup.lostitem.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.exception.LostItemFeatureException;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.SchoolAreaFixture;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.exception.LostItemImageException;
import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterResponse;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.exception.SchoolAreaException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

class LostItemControllerTest extends ControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    private Category category;
    private Category ectCategory;
    private SchoolArea schoolArea;

    @BeforeEach
    void setUp() {
        category = givenElectronicsCategory();
        ectCategory = givenEtcCategory();
        schoolArea = schoolAreaRepository.save(SchoolAreaFixture.AI_CENTER());
    }

    @Test
    void 분실물_등록에_성공하면_등록된_분실물의_id와_201_CREATED를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("images", "image.png", imageData, "image/png")
                .multiPart("images", "image.gif", imageData, "image/gif")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();


        // then
        LostItemRegisterResponse response = extract.as(LostItemRegisterResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(201);
            softly.assertThat(response.lostItemId()).isEqualTo(1L);
            softly.assertThat(response.message()).isEqualTo("분실물 등록에 성공했습니다.");
        });
    }


    @Test
    void 기타_분실물_등록에_성공하면_등록된_분실물의_id와_201_CREATED를_응답해야_한다() throws Exception {

        // given
        Long categoryId = ectCategory.getId();
        Long schoolAreaId = schoolArea.getId();
        LostItemRegisterRequest request = createECtRequest(schoolAreaId, categoryId);

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("images", "image.png", imageData, "image/png")
                .multiPart("images", "image.gif", imageData, "image/gif")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();


        // then
        LostItemRegisterResponse response = extract.as(LostItemRegisterResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(201);
            softly.assertThat(response.lostItemId()).isEqualTo(1L);
            softly.assertThat(response.message()).isEqualTo("분실물 등록에 성공했습니다.");
        });
    }

    @Test
    void 분실물_상세정보를_입력하지_않아도_분실물_등록이_성공해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = new LostItemRegisterRequest(
                null,
                "학술 정보원 2층 데스크",
                schoolAreaId,
                "AI 센터 B205",
                categoryId,
                List.of(featureRequest1, featureRequest2)
        );

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        LostItemRegisterResponse response = extract.as(LostItemRegisterResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(201);
            softly.assertThat(response.lostItemId()).isEqualTo(1L);
            softly.assertThat(response.message()).isEqualTo("분실물 등록에 성공했습니다.");
        });
    }

    @Test
    void 기타_카테고리가_아닌데_특징값_입력을_누락한_경우_400_BAD_REQUEST를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        LostItemRegisterRequest request = createECtRequest(schoolAreaId, categoryId);

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("images", "image.png", imageData, "image/png")
                .multiPart("images", "image.gif", imageData, "image/gif")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        ErrorResponse response = extract.as(ErrorResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(400);
            softly.assertThat(response.status()).isEqualTo(LostItemException.FEATURE_REQUIRED_FOR_NON_ETC_CATEGORY.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(LostItemException.FEATURE_REQUIRED_FOR_NON_ETC_CATEGORY.getTitle());
            softly.assertThat(response.detail()).isEqualTo(LostItemException.FEATURE_REQUIRED_FOR_NON_ETC_CATEGORY.getDetail());
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }


    @Test
    void 잘못된_학교_구역_id로_분실물을_등록하면_404_NOT_FOUND를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = 99L;
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("images", "image.png", imageData, "image/png")
                .multiPart("images", "image.gif", imageData, "image/gif")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        ErrorResponse response = extract.as(ErrorResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(404);
            softly.assertThat(response.status()).isEqualTo(SchoolAreaException.SCHOOL_AREA_NOT_FOUND.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(SchoolAreaException.SCHOOL_AREA_NOT_FOUND.getTitle());
            softly.assertThat(response.detail()).isEqualTo(SchoolAreaException.SCHOOL_AREA_NOT_FOUND.getDetail());
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }

    @Test
    void 이미지_파일을_등록하지_않으면_400_BAD_REQUEST를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when - images 누락
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();
        ErrorResponse response = extract.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(400);
            softly.assertThat(response.status()).isEqualTo(CommonException.MISSING_REQUEST_PART.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(CommonException.MISSING_REQUEST_PART.getTitle());
            softly.assertThat(response.detail()).contains("images");
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }

    @Test
    void 이미지_파일을_4개_이상_등록하면_400_BAD_REQUEST를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("images", "image.png", imageData, "image/png")
                .multiPart("images", "image.gif", imageData, "image/gif")
                .multiPart("images", "image.jpeg", imageData, "image/jpeg")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        ErrorResponse response = extract.as(ErrorResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(400);
            softly.assertThat(response.status()).isEqualTo(LostItemImageException.INVALID_IMAGE_COUNT.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(LostItemImageException.INVALID_IMAGE_COUNT.getTitle());
            softly.assertThat(response.detail()).isEqualTo(LostItemImageException.INVALID_IMAGE_COUNT.getDetail());
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }

    @Test
    void 존재하지_않는_카테고리에_대해_분실물을_등록하면_404_NOT_FOUND를_응답해야_한다() throws Exception {

        // given
        Long categoryId = 99L;
        Long schoolAreaId = 3L;
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        ErrorResponse response = extract.as(ErrorResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(404);
            softly.assertThat(response.status()).isEqualTo(CategoryException.CATEGORY_NOT_FOUND.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(CategoryException.CATEGORY_NOT_FOUND.getTitle());
            softly.assertThat(response.detail()).isEqualTo(CategoryException.CATEGORY_NOT_FOUND.getDetail());
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }

    @Test
    void 카테고리에_대해_일치하지_않는_특징으로_분실물을_등록하면_400_BAD_REQUEST를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(99L, feature2.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        ErrorResponse response = extract.as(ErrorResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(400);
            softly.assertThat(response.status()).isEqualTo(CategoryException.INVALID_CATEGORY_FEATURE.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(CategoryException.INVALID_CATEGORY_FEATURE.getTitle());
            softly.assertThat(response.detail()).isEqualTo(CategoryException.INVALID_CATEGORY_FEATURE.getDetail());
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }

    @Test
    void 특징에_대해_일치하지_않는_옵션으로_분실물을_등록하면_400_BAD_REQUEST를_응답해야_한다() throws Exception {

        // given
        Long categoryId = category.getId();
        Long schoolAreaId = schoolArea.getId();
        Feature feature1 = category.getFeatures().get(0);
        Feature feature2 = category.getFeatures().get(1);
        ItemFeatureRequest featureRequest1 = createFeatureRequest(feature1.getId(), feature1.getOptions().get(0).getId());
        ItemFeatureRequest featureRequest2 = createFeatureRequest(feature2.getId(), feature1.getOptions().get(0).getId());
        LostItemRegisterRequest request = createRequest(schoolAreaId, categoryId, List.of(featureRequest1, featureRequest2));

        given(imageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        byte[] imageData = "더미 이미지 데이터".getBytes();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("images", "image.jpg", imageData, "image/jpg")
                .multiPart("lostItemRegisterRequest", jsonRequest, "application/json")
                .when()
                .post("/api/lost-items")
                .then().log().all()
                .extract();

        // then
        ErrorResponse response = extract.as(ErrorResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(400);
            softly.assertThat(response.status()).isEqualTo(LostItemFeatureException.INVALID_FEATURE_OPTION.getHttpStatus().value());
            softly.assertThat(response.title()).isEqualTo(LostItemFeatureException.INVALID_FEATURE_OPTION.getTitle());
            softly.assertThat(response.detail()).isEqualTo(LostItemFeatureException.INVALID_FEATURE_OPTION.getDetail());
            softly.assertThat(response.instance()).isEqualTo("/api/lost-items");
        });
    }

    private LostItemRegisterRequest createRequest(Long schoolAreaId, Long categoryId, List<ItemFeatureRequest> featureRequests) {
        return new LostItemRegisterRequest(
                "핸드폰 액정이 깨져 있어요.",
                "학술 정보원 2층 데스크",
                schoolAreaId,
                "AI 센터 B205",
                categoryId,
                featureRequests
        );
    }

    private LostItemRegisterRequest createECtRequest(Long schoolAreaId, Long categoryId) {
        return new LostItemRegisterRequest(
                "핸드폰 액정이 깨져 있어요.",
                "학술 정보원 2층 데스크",
                schoolAreaId,
                "AI 센터 B205",
                categoryId,
                List.of()
        );
    }

    private ItemFeatureRequest createFeatureRequest(Long featureId, Long optionId) {
        return new ItemFeatureRequest(featureId, optionId);
    }
}
