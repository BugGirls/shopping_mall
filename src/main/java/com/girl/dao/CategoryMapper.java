package com.girl.dao;

import com.girl.pojo.Category;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    // 通过ID获取子节点下平级节点的信息
    List<Category> selectCategoryChildrenByParentId(Integer parentId);
}