package com.girl.service.impl;

import com.girl.common.ServerResponse;
import com.girl.dao.CategoryMapper;
import com.girl.pojo.Category;
import com.girl.service.ICategoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by girl on 2017/6/1.
 */
@Service
public class CategoryServiceImpl implements ICategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    /**
     * 添加商品类别
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        // 验证参数的合法性
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        // 创建对象
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);// 设置类别状态为：正常

        // 添加类别
        int insertCount = this.categoryMapper.insert(category);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMessage("商品类别添加成功");
        }

        return ServerResponse.createByErrorMessage("商品类别添加失败");
    }

    /**
     * 修改商品类别名称
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        // 验证参数的合法性
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        System.out.println(categoryName + "..................................");

        // 创建对象
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        // 更新操作
        int updateCount = this.categoryMapper.updateByPrimaryKeySelective(category);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("商品类别名称更新成功");
        }

        return ServerResponse.createByErrorMessage("商品类别名称更新失败");
    }

    /**
     * 通过ID获取子节点下平级节点的信息
     *
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = this.categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("没有找到当前分类的子分类");
        }

        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 通过id递归获取本节点和该节点下所有子节点信息
     *
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        // 递归算法，算出子节点
        findChildrenCategory(categorySet, categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category categoryItem : categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }

        return ServerResponse.createBySuccess(categoryIdList);
    }

    // 递归算法，算出子节点
    private Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = this.categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }

        // 查找子节点，递归算法一定要有一个退出的条件
        List<Category> categoryList = this.categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            findChildrenCategory(categorySet, categoryItem.getId());
        }

        return categorySet;
    }
}
