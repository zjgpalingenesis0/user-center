package com.zjg.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjg.usercenter.model.domain.Tag;
import com.zjg.usercenter.service.TagService;
import com.zjg.usercenter.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2025-10-27 20:39:08
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




