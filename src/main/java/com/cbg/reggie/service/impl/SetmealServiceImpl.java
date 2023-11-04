package com.cbg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbg.common.domain.CustomException;
import com.cbg.reggie.domain.dto.SetmealDto;
import com.cbg.reggie.domain.entity.Setmeal;
import com.cbg.reggie.domain.entity.SetmealDish;
import com.cbg.reggie.mapper.SetmealMapper;
import com.cbg.reggie.service.SetmealDishService;
import com.cbg.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    /**
     * 新增套餐，保存套餐和菜品的关联信息
     * Transactional事务注解 保证事务的一致性
     *
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     *
     * @param ids
     */
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,...) and status = 1
        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids).eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);

        //如果不能删除，抛出一个业务异常
        if (count > 0)
            throw new CustomException("套餐正在售卖中，不能删除");

        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);

        //删除关系表中的数据--setmeal_dish
        //delete from setmeal_dish where setmeal_id in (1,2,...)
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(dishQueryWrapper);
    }
}
