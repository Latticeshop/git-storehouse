package com.cbg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbg.reggie.domain.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    ShoppingCart add(ShoppingCart shoppingCart);

    ShoppingCart sub(ShoppingCart shoppingCart);

    List<ShoppingCart> byList();

}
