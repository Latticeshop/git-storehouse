package com.cbg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbg.reggie.domain.entity.ShoppingCart;
import com.cbg.reggie.mapper.ShoppingCartMapper;
import com.cbg.reggie.service.ShoppingCartService;
import com.cbg.web.config.BaseContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Override
    @Transactional
    public ShoppingCart add(ShoppingCart shoppingCart) {
        //设置用户id，指定当前是哪个用户的购物车数据
        ShoppingCart cartServiceOne = getShoppingCart(shoppingCart);

        if (cartServiceOne != null) {
            //如果存在，就在原来数量上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
//            //设置乘数倍率
//            BigDecimal times = new BigDecimal((number + 1.0) / number);
//            //总价也得加一，新价格=每份价格*新数量 每份价格=旧价格/旧数量 -> 新价格=旧价格*(新数量/旧数量)
//            BigDecimal newAmount = cartServiceOne.getAmount().multiply(times);
//            cartServiceOne.setAmount(newAmount);
//            shoppingCartService.updateById(cartServiceOne); 在自己的service中不用@Autowired自己
            updateById(cartServiceOne);
        } else {
            //如果不存在，则添加一条新购物车数据，数量默认一
            shoppingCart.setNumber(1);
            save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return cartServiceOne;
    }

    @Override
    @Transactional
    public ShoppingCart sub(ShoppingCart shoppingCart) {
        ShoppingCart cartServiceOne = getShoppingCart(shoppingCart);

        //减去数量
        Integer number = cartServiceOne.getNumber();
        if (number - 1 > 0) {
            //如果减去后存在，就在原来数量上减一
            cartServiceOne.setNumber(number - 1);

            updateById(cartServiceOne);
        } else {
            //如果减去后不存在，则删除对应数据
            shoppingCart.setNumber(0);
            removeById(cartServiceOne);
        }

        return cartServiceOne;
    }

    public List<ShoppingCart> byList() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        return list(queryWrapper);
    }

    private ShoppingCart getShoppingCart(ShoppingCart shoppingCart) {
        //设置用户id，指定当前是哪个用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查询当前菜品或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        //查询是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        if (dishId != null) {
            //添加购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else if (setmealId != null) {
            //添加购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        //SQL:select from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = getOne(queryWrapper);
        return cartServiceOne;
    }
}
