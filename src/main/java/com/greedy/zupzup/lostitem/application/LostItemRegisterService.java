package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LostItemRegisterService {

    private final LostItemRepository lostItemRepository;
    private final CategoryRepository categoryRepository;


}
