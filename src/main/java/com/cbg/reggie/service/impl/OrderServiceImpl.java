package com.cbg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbg.reggie.domain.entity.*;
import com.cbg.reggie.mapper.OrderMapper;
import com.cbg.reggie.service.*;
import com.cbg.web.config.BaseContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    private ShoppingCartService shoppingCartService;

    private UserService userService;

    private AddressBookService addressBookService;

    private OrderDetailService orderDetailService;

    public OrderServiceImpl(ShoppingCartService shoppingCartService, UserService userService, AddressBookService addressBookService, OrderDetailService orderDetailService) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.addressBookService = addressBookService;
        this.orderDetailService = orderDetailService;
    }

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0)
            throw new RuntimeException("购物车为空，不能下单");

        //查询用户数据
        User user = userService.getById(userId);
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null)
            throw new RuntimeException("用户地址信息有误，不能下单");

        //向订单表插入数据，一条
        long orderId = IdWorker.getId(); //订单号

        //多线程下也不会出问题
        AtomicInteger amount = new AtomicInteger(0);

        //处理金额
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(orderId)
                    .number(item.getNumber())
                    .dishFlavor(item.getDishFlavor())
                    .dishId(item.getDishId())
                    .setmealId(item.getSetmealId())
                    .name(item.getName())
                    .image(item.getImage())
                    .amount(item.getAmount())
                    .build();

            //计算总金额=数量*金额 addAndGet是累加
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        Orders ordersNew = Orders.builder()
                .addressBookId(orders.getAddressBookId())
                .payMethod(orders.getPayMethod())
                .remark(orders.getRemark())
                .amount(new BigDecimal(amount.get())) //总金额
                .id(orderId)
                .number(String.valueOf(orderId))
                .orderTime(LocalDateTime.now())
                .checkoutTime(LocalDateTime.now())
                .status(2)
                .userId(userId)
                .userName(user.getName())
                .consignee(addressBook.getConsignee())
                .phone(addressBook.getPhone())
                .address((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                        + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                        + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                        + (addressBook.getDetail() == null ? "" : addressBook.getDetail()))
                .build();

        this.save(ordersNew);

        //向订单详细表插入数据，多条
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
}
