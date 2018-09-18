package com.maoding.dynamicForm.dao;

import com.maoding.core.base.dao.BaseDao;
import com.maoding.dynamicForm.entity.DynamicFormGroupEntity;

import java.util.List;

/**
 * 深圳市卯丁技术有限公司
 * 日期: 2018/9/14
 * 类名: com.maoding.dynamicForm.dao.DynamicFormGroupDao
 * 作者: 张成亮
 * 描述:
 **/
public interface DynamicFormGroupDao extends BaseDao<DynamicFormGroupEntity> {

    List<DynamicFormGroupEntity> listDefaultFormGroup();

    List<DynamicFormGroupEntity> listFormGroupByCompanyId(String companyId);

    boolean isInitFormGroup(String companyId);

    Integer selectMaxSeq(String  currentCompanyId);

}
