package com.cbg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbg.reggie.domain.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    void submit(Orders orders);
}
