package com.girl.service;

import com.girl.common.ServerResponse;
import com.girl.pojo.Shipping;
import com.github.pagehelper.PageInfo;

/**
 * Created by girl on 2017/6/6.
 */
public interface IShippingService {

    ServerResponse addOrUpdate(Integer userId, Shipping shipping);

    ServerResponse delete(Integer userId, Integer shippingId);

    ServerResponse selectOneShipping(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(int pageNum, int pageSize, Integer userId);

}
