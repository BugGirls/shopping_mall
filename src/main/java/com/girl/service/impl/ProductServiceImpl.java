package com.girl.service.impl;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.dao.CategoryMapper;
import com.girl.dao.ProductMapper;
import com.girl.pojo.Category;
import com.girl.pojo.Product;
import com.girl.service.ICategoryService;
import com.girl.service.IProductService;
import com.girl.util.DateTimeUtil;
import com.girl.util.PropertiesUtil;
import com.girl.vo.ProductDetailVo;
import com.girl.vo.ProductListVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by girl on 2017/6/2.
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ICategoryService iCategoryService;

    /**
     * 保存或者更新产品（如果为更新，需要传入ID值）
     *
     * @param product
     * @return
     */
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            // 如果子图不为空，就获取第一个子图作为产品的主图(子图的分割条件为逗号)
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }

            // 如果id不为空，说明为更新操作
            if (product.getId() != null) {// 更新
                int updateCount = this.productMapper.updateByPrimaryKey(product);
                if (updateCount > 0) {
                    return ServerResponse.createBySuccess("产品更新成功");
                } else {
                    return ServerResponse.createByErrorMessage("产品更新失败");
                }
            } else {// id为空，为添加操作
                int insertCount = this.productMapper.insert(product);
                if (insertCount > 0) {
                    return ServerResponse.createBySuccessMessage("产品添加成功");
                } else {
                    return ServerResponse.createByErrorMessage("产品添加失败");
                }
            }
        }

        return ServerResponse.createByErrorMessage("新增或更新产品参数错误");
    }

    /**
     * 修改商品销售状态
     *
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "非法参数");
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int updateCount = this.productMapper.updateByPrimaryKeySelective(product);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("商品销售状态修改成功");
        }

        return ServerResponse.createByErrorMessage("商品销售状态修改失败");
    }

    /**
     * 获取商品详情
     *
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> manageProductDetails(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "非法参数");
        }

        Product product = this.productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品已经下架或删除");
        }

        // VO对象--value object
        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        // 设置imageHost,通过properties配置文件来获取
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.girl.com/"));

        // 设置parentCategoryId
        Category category = this.categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);// 默认为根节点
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        // createTime：从DB中获取的是时间戳，需要转换成自己期望的日期格式
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        // updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    /**
     * 获取商品列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        // startPage
        PageHelper.startPage(pageNum, pageSize);

        // 填充自己的sql查询逻辑
        List<Product> productList = this.productMapper.selectProductList();

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = this.assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        // pageHelper
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);

        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.girl.com/"));
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setName(product.getName());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubTitle(product.getSubtitle());

        return productListVo;
    }

    /**
     * 商品搜索
     *
     * @param productId
     * @param productName
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> productSearch(Integer productId, String productName, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }

        List<Product> productList = this.productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = this.assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 前台-获取商品详情
     *
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "非法参数");
        }

        Product product = this.productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品已经被删除");
        }

        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("商品已经下架");
        }

        // VO对象--value object
        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 前台用户进行商品搜索，当在输入框中输入需要的商品时，keyword不为空，当点击左侧商品信息节点时，categoryId不为空
     *
     * @param keyword：搜索关键字
     * @param categoryId：商品分类id
     * @param orderBy：排序条件
     * @param pageNum：当前页数
     * @param pageSize：每页显示多少条数据
     * @return
     */
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize) {
        // 如果关键字和分类id都没有传入，则规定为参数错误
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }

        List<Integer> categoryIdList = new ArrayList<Integer>();

        // 如果分类id不为空，即点击左侧商品节点来显示该节点下的商品信息
        if (categoryId != null) {
            Category category = this.categoryMapper.selectByPrimaryKey(categoryId);
            // 没有该分类，并且没有关键字，这个时候并不是查询错误，而是没有命中数据库中的数据，因此要返回一个空的结果
            if (category == null && StringUtils.isBlank(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);

                return ServerResponse.createBySuccess(pageInfo);
            }

            // 通过递归算法，获取该分类节点和子节点的商品ID
            categoryIdList = this.iCategoryService.getCategoryAndDeepChildrenCategory(categoryId).getData();
        }

        // 如果关键字不为空，即在输入框中输入了想要查询的商品信息
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        // 动态排序处理
        // 规则：前台传入一个orderBy，格式为“price_desc”,其中price表示排序的字段为通过价格进行排序，desc表示排序的方式为降序
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(orderBy)) {
            // 传入的字符串orderBy存在于Const定义的常量PRICE_ASC_DESC中
            // 常量PRICE_ASC_DESC使用Set集合的目的：Set中contains()方法的时间复杂度为O1，而List中contains()方法的时间复杂度为On
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                // PageHelper的orderBy方法传入值的格式为："price desc";
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }

        // 商品搜索处理
        List<Product> productList = this.productMapper.selectByKeywordAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword, categoryIdList.size()==0?null:categoryIdList);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product:productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

}
