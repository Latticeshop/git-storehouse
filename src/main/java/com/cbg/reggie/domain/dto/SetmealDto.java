package com.cbg.reggie.domain.dto;

import com.cbg.reggie.domain.entity.Setmeal;
import com.cbg.reggie.domain.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
