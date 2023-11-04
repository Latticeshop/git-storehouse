package com.cbg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbg.reggie.domain.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    AddressBook setDefault(AddressBook addressBook);

    AddressBook getDefault();

    List<AddressBook> byList(AddressBook addressBook);

    /**
     * 删除地址
     *
     * @param id
     * @return
     */
    void remove(Long id);
}
