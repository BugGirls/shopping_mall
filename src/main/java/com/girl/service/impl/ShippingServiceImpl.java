package com.girl.service.impl;

import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.dao.ShippingMapper;
import com.girl.pojo.Shipping;
import com.girl.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by girl on 2017/6/6.
 */
@Service
public class ShippingServiceImpl implements IShippingService {

    @Resource
    private ShippingMapper shippingMapper;

    /**
     * 添加或修改收货地址
     *
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse addOrUpdate(Integer userId, Shipping shipping) {
        if (shipping == null || userId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }

        shipping.setUserId(userId);

        if (shipping.getId() != null) {// 更新
            // 为防止横向越权，需要设置shipping.setUserId(userId)的值，确保更新的为当前登录用户的收货地址
            int updateCount = this.shippingMapper.updateShippingByUserIdAndShipping(shipping);
            if (updateCount > 0) {
                return ServerResponse.createBySuccessMessage("收货地址更新成功");
            }

            return ServerResponse.createByErrorMessage("收货地址更新失败");
        } else {// 添加
            int insertCount = this.shippingMapper.insert(shipping);
            // 与前台的约定：当一个新的收货地址插入成功时，返回新插入地址的ID给前台
            if (insertCount > 0) {
                Map result = Maps.newHashMap();
                result.put("shippingId", shipping.getId());
                return ServerResponse.createBySuccess("收货地址添加成功", result);
            }

            return ServerResponse.createByErrorMessage("收货地址添加失败");
        }
    }

    /**
     * 删除收货地址
     *
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse delete(Integer userId, Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }

        // 为了防止横向越权，需要传入当前登录用户的ID和收货地址id值，确保只能删除当前登录用户下的收货地址
        int deleteCount = this.shippingMapper.deleteShippingByUserIdAndShippingId(userId, shippingId);
        if (deleteCount > 0) {
            return ServerResponse.createBySuccessMessage("收货地址删除成功");
        }

        return ServerResponse.createByErrorMessage("收货地址删除失败");
    }

    /**
     * 查询一个收货地址
     *
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse selectOneShipping(Integer userId, Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }

        Shipping shipping = this.shippingMapper.selectShippingByUserIdAndShippingId(userId, shippingId);
        if (shipping != null) {
            return ServerResponse.createBySuccess(shipping);
        }

        return ServerResponse.createByErrorMessage("查询不到该收货地址");
    }

    /**
     * 查询收货地址列表
     *
     * @param pageNum
     * @param pageSize
     * @param userId
     * @return
     */
    public ServerResponse<PageInfo> list(int pageNum, int pageSize, Integer userId) {
        PageHelper.startPage(pageNum, pageSize);

        List<Shipping> shippingList = this.shippingMapper.selectShippingListByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);

        return ServerResponse.createBySuccess(pageInfo);
    }
}
