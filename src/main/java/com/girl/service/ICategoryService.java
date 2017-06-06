package com.girl.service;

import com.girl.common.ServerResponse;
import com.girl.pojo.Category;

import java.util.List;

/**
 * Created by girl on 2017/6/1.
 */
public interface ICategoryService {

    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(Integer categoryId);
}
