package com.greedy.zupzup.admin.lostitem.application;

import com.greedy.zupzup.admin.lostitem.application.dto.AdminLostItemSimpleCommand;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.repository.AdminLostItemSimpleProjection;
import com.greedy.zupzup.category.application.dto.FeatureOptionDto;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLostItemService {

    private final LostItemRepository lostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final S3ImageFileManager s3ImageFileManager;
    private final LostItemFeatureRepository lostItemFeatureRepository;

    @Transactional
    public ApproveLostItemsResponse approveBulk(ApproveLostItemsRequest request) {
        List<Long> lostItemIds = request.lostItemIds();

        int successCount = lostItemRepository.updateStatusBulkByIds(
                lostItemIds,
                LostItemStatus.REGISTERED,
                LostItemStatus.PENDING
        );

        return ApproveLostItemsResponse.of(successCount, lostItemIds.size());
    }

    @Transactional
    public RejectLostItemsResponse rejectBulk(RejectLostItemsRequest request) {
        List<Long> lostItemIds = request.lostItemIds();

        List<String> imageKeys = lostItemImageRepository.findImageKeysByLostItemIds(lostItemIds);
        imageKeys.forEach(s3ImageFileManager::delete);

        lostItemFeatureRepository.deleteByLostItemIds(lostItemIds);

        lostItemImageRepository.deleteByLostItemIds(lostItemIds);

        int deletedCount = lostItemRepository.deleteBulkByIds(lostItemIds);

        return RejectLostItemsResponse.of(deletedCount, lostItemIds.size());
    }

    @Transactional(readOnly = true)
    public AdminPendingLostItemListResponse getPendingLostItems(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<AdminLostItemSimpleProjection> projectionPage = lostItemRepository.findPendingList(
                LostItemStatus.PENDING, pageable
        );

        List<Long> lostItemIds = projectionPage.getContent().stream()
                .map(AdminLostItemSimpleProjection::getId)
                .toList();

        List<LostItemFeature> features = lostItemFeatureRepository.findFeaturesForLostItems(lostItemIds);

        Map<Long, List<FeatureOptionDto>> featuresMap = features.stream()
                .collect(Collectors.groupingBy(
                        lif -> lif.getLostItem().getId(),
                        Collectors.mapping(
                                lif -> new FeatureOptionDto(lif.getFeature().getId(), lif.getSelectedOption().getOptionValue()),
                                Collectors.toList()
                        )
                ));

        Page<AdminLostItemSimpleCommand> commandPage = projectionPage.map(p -> {
            List<String> imageKeys = (p.getImageKeysString() == null || p.getImageKeysString().isEmpty())
                    ? Collections.emptyList()
                    : List.of(p.getImageKeysString().split(","));

            List<FeatureOptionDto> featureOptions = featuresMap.getOrDefault(p.getId(), Collections.emptyList());

            return new AdminLostItemSimpleCommand(
                    p.getId(), p.getCategoryId(), p.getCategoryName(), p.getSchoolAreaId(), p.getSchoolAreaName(),
                    p.getFoundAreaDetail(), p.getCreatedAt().toString(), p.getDescription(), p.getDepositArea(),
                    imageKeys, featureOptions
            );
        });

        return AdminPendingLostItemListResponse.of(commandPage);
    }
}
