package com.cbg.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cbg.common.domain.R;
import com.cbg.reggie.domain.entity.Orders;
import com.cbg.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    //submit(@RequestBody Orders from) 应该全为form 避免数据库改名
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据:{}", orders);
        orderService.submit(orders);

        return R.success("下单成功");
    }

    /**
     * 用户分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        log.info("page=" + page + ",pageSize=" + pageSize);
        Page pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //选择账号状态1的
        queryWrapper.eq(Orders::getStatus, 1);

        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
}
