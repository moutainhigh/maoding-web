package com.maoding.dynamicForm.service.impl;

import com.maoding.attach.dto.FileDataDTO;
import com.maoding.commonModule.dto.QueryCopyRecordDTO;
import com.maoding.commonModule.dto.SaveCopyRecordDTO;
import com.maoding.commonModule.service.AuditCopyService;
import com.maoding.commonModule.service.CopyRecordService;
import com.maoding.core.base.dto.CoreShowDTO;
import com.maoding.core.base.service.GenericService;
import com.maoding.core.constant.CopyTargetType;
import com.maoding.core.constant.ExpenseConst;
import com.maoding.core.constant.NetFileType;
import com.maoding.core.constant.SystemParameters;
import com.maoding.core.util.BeanUtils;
import com.maoding.core.util.StringUtil;
import com.maoding.core.util.StringUtils;
import com.maoding.dynamicForm.dao.DynamicFormFieldValueDao;
import com.maoding.dynamicForm.dto.*;
import com.maoding.dynamicForm.entity.DynamicFormFieldSelectableValueEntity;
import com.maoding.dynamicForm.entity.DynamicFormFieldValueEntity;
import com.maoding.dynamicForm.service.DynamicFormFieldValueService;
import com.maoding.dynamicForm.service.DynamicFormService;
import com.maoding.exception.CustomException;
import com.maoding.financial.dao.ExpMainDao;
import com.maoding.financial.dto.*;
import com.maoding.financial.entity.ExpMainEntity;
import com.maoding.financial.service.ExpCategoryService;
import com.maoding.financial.service.ExpMainService;
import com.maoding.org.dto.CompanyUserDataDTO;
import com.maoding.process.dao.ProcessInstanceRelationDao;
import com.maoding.process.dao.ProcessTypeDao;
import com.maoding.process.entity.ProcessInstanceRelationEntity;
import com.maoding.process.entity.ProcessTypeEntity;
import com.maoding.process.service.ProcessService;
import com.maoding.project.dao.ProjectDao;
import com.maoding.project.dto.ProjectDTO;
import com.maoding.project.service.ProjectSkyDriverService;
import com.maoding.system.entity.DataDictionaryEntity;
import com.maoding.system.service.DataDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态表单自定义的数据层接口- 自定义数据字段- 可选择提供
 */
@Service("dynamicFormFieldValueService")
public class DynamicFormFieldValueServiceImpl extends GenericService<DynamicFormFieldSelectableValueEntity> implements DynamicFormFieldValueService {

    @Autowired
    private AuditCopyService auditCopyService;

    @Autowired
    private DynamicFormService dynamicFormService;

    @Autowired
    private DynamicFormFieldValueDao dynamicFormFieldValueDao;

    @Autowired
    private ExpMainService expMainService;

    @Autowired
    private ExpCategoryService expCategoryService;

    @Autowired
    private ProjectSkyDriverService projectSkyDriverService;

    @Autowired
    private CopyRecordService copyRecordService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DataDictionaryService dataDictionaryService;

    @Autowired
    private ProcessTypeDao processTypeDao;

    @Autowired
    private ExpMainDao expMainDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProcessInstanceRelationDao processInstanceRelationDao;

    private Integer detailType = 9;


    void validateSaveAuditDetail(SaveDynamicAuditDTO dto) throws Exception{
        if(StringUtils.isEmpty(dto.getType())){
            throw new CustomException("参数错误");
        }
    }

    /**
     * 作者：FYT
     * 日期：2018/9/13
     * 描述：保存审核表单内容
     * @return
     * @throws Exception
     */
    @Override
    public int saveAuditDetail(SaveDynamicAuditDTO dto) throws Exception {
        validateSaveAuditDetail(dto);
        ProcessTypeEntity processType = processTypeDao.getCurrentProcessTypeByFormId(dto.getCurrentCompanyId(),dto.getType());
        String conditionFieldId = processType.getConditionFieldId();
        double conditionFieldValue = 0;

        //todo 1.添加审批表单主表的记录
        ExpMainEntity mainEntity = new ExpMainEntity();
        mainEntity.initEntity();
        if(StringUtils.isNotEmpty(dto.getTargetId())){
            mainEntity.setId(dto.getTargetId());
        }
        mainEntity.setType(dto.getType());
        String mainId = mainEntity.getId();
        //todo 2.添加页面上的fieldId 对应的 value
        for(DynamicFormFieldValueDTO valueDTO: dto.getFieldList()) {
            if(valueDTO.getFieldId().equals(conditionFieldId)){
                conditionFieldValue = conditionFieldValue + Double.parseDouble(valueDTO.getFieldValue());
            }
            valueDTO.setMainId(mainId);
            if (valueDTO.getFieldType() == detailType) {//如果是明细
                DynamicFormFieldValueEntity fieldValue = this.saveDynamicFormFieldValue(valueDTO);
                String fieldValuePid = fieldValue.getId();
                //todo 3.添加明细的List对应value
                Integer groupNum = 1;
                boolean isStatistic = false;
                double filedValue = 0;
                for(int i = 0;i<valueDTO.getDetailFieldList().size();i++,groupNum++){
                    for(DynamicFormFieldValueDTO detailDTO:valueDTO.getDetailFieldList().get(i)){
                        if(detailDTO.getFieldId().equals(conditionFieldId)){
                            conditionFieldValue = conditionFieldValue + Double.parseDouble(detailDTO.getFieldValue());
                        }
                        detailDTO.setMainId(mainId);
                        detailDTO.setGroupNum(groupNum);
                        detailDTO.setFieldValuePid(fieldValuePid);
                        this.saveDynamicFormFieldValue(detailDTO);
                        if(detailDTO.getIsStatistics()!=null && detailDTO.getIsStatistics()==1
                                &&  detailDTO.getRequiredType()!=null &&detailDTO.getRequiredType()==1){
                            isStatistic = true;
                            filedValue += Double.parseDouble(detailDTO.getFieldValue());
                        }
                    }
                }
                if(isStatistic){
                    fieldValue.setFieldValue(filedValue+"");
                    dynamicFormFieldValueDao.updateById(fieldValue);
                }
            }else {
                this.saveDynamicFormFieldValue(valueDTO);
            }
        }

        //todo 4.插入主表记录，因为，在插入主表记录的时候，要启动流程，分条件流程还需要携带条件值，所有放在最后插入
        if(StringUtils.isNotEmpty(conditionFieldId)){
            dto.setConditionFieldValue(conditionFieldValue);
        }
        this.expMainService.saveExpMain(mainEntity,dto);
        //处理抄送人
        List<String> companyUserIdList = new ArrayList<>();
        dto.getCcCompanyUserList().stream().forEach(c->{
            companyUserIdList.add(c.getId());
        });
        this.expMainService.saveCopy(companyUserIdList,dto.getCurrentCompanyUserId(),mainId,mainId);
        return 1;
    }


    private DynamicFormFieldValueEntity saveDynamicFormFieldValue(DynamicFormFieldValueDTO valueDTO){
        DynamicFormFieldValueEntity dynamicFormFieldValueEntity = BeanUtils.createFrom(valueDTO,DynamicFormFieldValueEntity.class);
        dynamicFormFieldValueEntity.initEntity();
        dynamicFormFieldValueEntity.setFieldValue((String)valueDTO.getFieldValue());
        dynamicFormFieldValueDao.insert(dynamicFormFieldValueEntity);
        return dynamicFormFieldValueEntity;
    }


    @Override
    public Map<String,Object> initDynamicAudit(FormFieldQueryDTO dto) throws Exception {
        String id = dto.getId();
        String targetType = null;
        String conditionFieldId = null;
        AuditCommonDTO expMainDTO = new AuditCommonDTO();
        if(StringUtils.isNotEmpty(id)){
            expMainDTO = expMainService.getAuditDataById(id);
            dto.setFormId(expMainDTO.getType());
        }
        if(dto.getId()==null ){//如果是新增审批单，必需要存在processType
            ProcessTypeEntity processType = this.processTypeDao.getCurrentProcessTypeByFormId(dto.getCurrentCompanyId(),dto.getFormId());
            if( processType==null){
                throw new CustomException("参数错误");
            }
            targetType =  processType.getTargetType();
            conditionFieldId = processType.getConditionFieldId();
        }
        String conditionValue = null;
        List<AuditDTO> auditList = null;
        List<CompanyUserDataDTO> ccCompanyUserList = null;
        List<FileDataDTO> expAttachList = null;
        Map<String,Object> result = new HashMap<>();
        //1.查询模板+数据
        List<DynamicFormFieldValueDTO> fieldList = dynamicFormFieldValueDao.listFormFieldValueByFormId(dto);
        SaveDynamicAuditDTO dynamicAudit = this.handleDynamicFormField(fieldList,dto,conditionFieldId);
        dynamicAudit.setType(dto.getFormId());
        //2.把数据匹配上
        if(StringUtils.isNotEmpty(id)){
            //2.3 查询附件
            Map<String,Object> param = new HashMap<>();
            param.put("targetId", id);
            param.put("type", NetFileType.EXPENSE_ATTACH);
            expAttachList = this.projectSkyDriverService.getAttachDataList(param);
            //4.查询知会人
            ccCompanyUserList = copyRecordService.getCopyRecode(new QueryCopyRecordDTO(id));
            //查询审批记录
            auditList = this.expMainService.getAuditList(id,null);//暂时不需要expMainEntity
            dynamicAudit.setAuditFlag(this.getAuditFlag(expMainDTO.getApproveStatus(),dto.getCurrentCompanyUserId(),auditList));//请务必跟在getAuditList后面，不要移动位置
            dynamicAudit.setIsEdit(this.getIsEdit(expMainDTO,dto.getCurrentCompanyUserId()));
            dynamicAudit.setId(id);
        }else {
            //查询模板中的知会人,待处理
            ccCompanyUserList = auditCopyService.listAuditCopyUser(dto.getCurrentCompanyId(),dto.getFormId());
        }
        //3.查询流程 返回流程标识，给前端控制是否要给审批人，以及按钮显示的控制
        AuditEditDTO audit = BeanUtils.createFrom(dto,AuditEditDTO.class);
        audit.setMainId(dto.getId());
        audit.setAuditType(targetType);//此处是用于新增审批单的时候， 查找相应的流程，如果是详情界面，则不需要，直接通过id去找相应的流程实例
        Map<String,Object> processData = processService.getCurrentTaskUser(audit,auditList,this.getConditionValue(dto.getId()));
        dynamicAudit.setAttachList(expAttachList);
        dynamicAudit.setCcCompanyUserList(ccCompanyUserList);
        dynamicAudit.setAuditList(auditList);
        dynamicAudit.setBaseAuditData(expMainDTO);
        result.put("dynamicAudit",dynamicAudit);
        result.put("formName",this.dynamicFormService.getFormName(dto.getFormId()));
        result.putAll(processData);
        return result;
    }

    @Override
    public List<DynamicFormFieldValueDTO> listFormFieldValueByFormId(FormFieldQueryDTO dto) throws Exception {
        List<DynamicFormFieldValueDTO> fieldList = dynamicFormFieldValueDao.listFormFieldValueByFormId(dto);
        this.handleDynamicFormField(fieldList,dto,null);
        return fieldList;
    }

    /**
     * 如果mainId不为null，表示是查询详情，查询getConditionValue 中的参数conditionFieldId必须要从ProcessInstanceRelationEntity 中获取
     * 如果mainId为null，直接返回null就好了。
     * getConditionValue：主要用于分条件流程 条件值获取，便于详情中获取auditList
     */
    public String getConditionValue(String mainId){
        ProcessInstanceRelationEntity instanceRelation = this.processInstanceRelationDao.getProcessInstanceRelation(mainId);
        if(instanceRelation!=null && StringUtils.isEmpty(instanceRelation.getConditionFieldId()) || StringUtils.isEmpty(mainId)){
            return null;
        }
        return dynamicFormFieldValueDao.getConditionValue(mainId,instanceRelation.getConditionFieldId())+"";
    }


    public void setStatisticalCondition(DynamicFormFieldValueDTO dto,String conditionFieldId){
        if(StringUtils.isNotEmpty(conditionFieldId)) {
            if(dto.getFieldId().equals(conditionFieldId)){
                dto.setStatisticalCondition(1);//设置为分条件统计的字段
            }
        }
    }

    public Integer getAuditFlag(String approveStatus,String currentCompanyUserId,List<AuditDTO> auditList){
        if(("0".equals(approveStatus) || "5".equals(approveStatus)) && !CollectionUtils.isEmpty(auditList)){
            AuditDTO auditDTO = auditList.get(auditList.size()-1);
            if(currentCompanyUserId!=null && currentCompanyUserId.equals(auditDTO.getCompanyUserId())){
                return 1;//处于审批的状态
            }
        }
        return 0;
    }

    public Integer getIsEdit(AuditCommonDTO commonDTO,String currentCompanyUserId){
        //以下屏蔽，暂时不做编辑功能
//        String approveStatus = commonDTO.getApproveStatus();
//        Integer expFlag = commonDTO.getExpFlag();
//        String expCompanyUserId = commonDTO.getCompanyUserId();
//        if(("2".equals(approveStatus) || "3".equals(approveStatus)) && expFlag!=null && expFlag != 1){
//            if(currentCompanyUserId!=null && currentCompanyUserId.equals(expCompanyUserId)){
//                return 1;//处于可编辑的状态
//            }
//        }
        return 0;
    }

    public SaveDynamicAuditDTO handleDynamicFormField(List<DynamicFormFieldValueDTO> fieldList,FormFieldQueryDTO dto,String conditionFieldId) throws Exception{
        SaveDynamicAuditDTO dynamicAudit = new SaveDynamicAuditDTO();
        Map<String,Object> valueMap = new HashMap<>();
        for(DynamicFormFieldValueDTO field:fieldList){
            this.setSelectValue(field,dto,valueMap);
            this.setStatisticalCondition(field,conditionFieldId);
            if(field.getFieldType()==9){
                List<List<DynamicFormFieldValueDTO>> detailFieldValueList = new ArrayList<>();
                dto.setFieldPid(field.getFieldId());
                List<DynamicFormFieldValueDTO> detailList = dynamicFormFieldValueDao.listFormFieldValueByFormId(dto);
                Map<String,List<DynamicFormFieldValueDTO>> detailMap = new HashMap<>();
                for(DynamicFormFieldValueDTO val:detailList){
                    this.setSelectValue(val,dto,valueMap);
                    this.setStatisticalCondition(val,conditionFieldId);
                    String key = val.getGroupNum()+"";
                    if(detailMap.containsKey(key)){
                        detailMap.get(key).add(val);
                    }else {
                        List<DynamicFormFieldValueDTO> detailList2 = new ArrayList<>();
                        detailList2.add(val);
                        detailMap.put(key,detailList2);
                        detailFieldValueList.add(detailList2);
                    }
                }
                field.setDetailFieldList(detailFieldValueList);
            }
        }
        dynamicAudit.setFieldList(fieldList);
        return dynamicAudit;
    }

    private void setFiledValueText(DynamicFormFieldValueDTO field){
        String id = field.getFieldValue();
        field.setFieldValueText(field.getFieldValue());
        if(StringUtils.isNotEmpty(id)) {
            if (field.getFieldType() == 12) {//如果是项目
                field.setFieldValueText(this.projectDao.getProjectName(id));
            }
            if (field.getFieldType() == 11) {//如果是审批数据
                if (!StringUtils.isEmpty(id)) {
                    AuditCommonDTO expMainDTO = expMainService.getAuditDataById(id);
                    field.setFieldValueText(expMainDTO.getUserName() + "的" + expMainDTO.getExpTypeName() + "申请");
                }
            }
            if (field.getFieldType() == 6) {
                if ("3".equals(field.getFieldSelectValueType())) {
                    field.setFieldValueText(ExpenseConst.getTypeName(id));
                }
            }
        }
    }

    /**
     * 1.设置控件的候选值
     * 2.设置控件的文本值（供前端显示），因为在后台保存的可能是id，在前端需要展示相应的文本
     * valueMap 主要用于有候选值的，控件类型一样，并且所选择的候选类型一样，不是自定义的数据，以免多次查找
     */
    private void setSelectValue(DynamicFormFieldValueDTO field,FormFieldQueryDTO dto,Map<String,Object> valueMap) throws Exception{
        setFiledValueText(field);
        if(isNeedSelectValue(field) && field.getFieldSelectValueType()!=null){
            if("0".equals(field.getFieldSelectValueType())){//需要全部重新获取，不要put valueMap中
                List<CoreShowDTO>  selectList = this.getDynamicFormFieldSelectedList(field.getFieldId());
                field.setFieldSelectedValueList(selectList);
               // valueMap.put(key,selectList);
            }else {
                String key = field.getFieldType()+"_"+field.getFieldSelectValueType();
                if(valueMap.containsKey(key)){
                    field.setFieldSelectedValueList(valueMap.get(key));
                }
                if(field.getFieldType() == 6){
                    if("1".equals(field.getFieldSelectValueType()) || "2".equals(field.getFieldSelectValueType())){
                        List<Map<String,Object>>  selectList =  this.getExpList(dto);
                        field.setFieldSelectedValueList(selectList);
                        valueMap.put(key,selectList);
                    }
                    if("3".equals(field.getFieldSelectValueType())){
                        List<CoreShowDTO> selectList = this.getLeaveTypeList();
                        field.setFieldSelectedValueList(selectList);
                        valueMap.put(key,selectList);
                    }
                }
            }
        }

        if(field.getFieldType()==11){//todo 从审批表中获取数据,过后处理
            String key = field.getFieldType()+"_"+field.getFieldSelectValueType();
            if(valueMap.containsKey(key)){
                field.setFieldSelectedValueList(valueMap.get(key));
            }else {
                List<CoreShowDTO> selectList = getAuditTypeList(dto,field.getFieldSelectValueType());
                field.setFieldSelectedValueList(selectList);
                valueMap.put(key,selectList);
            }
        }
        if(field.getFieldType()==12){//从我的项目中获取
            String key = field.getFieldType()+"_"+field.getFieldSelectValueType();
            if(valueMap.containsKey(key)){
                field.setFieldSelectedValueList(valueMap.get(key));
            }else {
                List<CoreShowDTO> selectList = this.getProjectList(dto,field.getFieldSelectValueType());
                field.setFieldSelectedValueList(selectList);
                valueMap.put(key,selectList);
            }
        }
    }

    public List<Map<String,Object>> getExpList(FormFieldQueryDTO dto) throws Exception{
        List<ExpTypeDTO> selectList =  expCategoryService.getExpCategoryTypeList(dto.getCurrentCompanyId(),dto.getAccountId());
        List<Map<String,Object>> list = new ArrayList<>();
        selectList.stream().forEach(s->{
            Map<String,Object> map = new HashMap<>();
            map.put("id",s.getParent().getName());
            map.put("name",s.getParent().getName());
            List<CoreShowDTO> childList = new ArrayList<>();
            map.put("child",childList);
            s.getChild().stream().forEach(c->{
                childList.add(new CoreShowDTO(s.getParent().getName()+"-"+c.getName(),c.getName()));
            });
            list.add(map);
        });
        return list;
    }

    public List<CoreShowDTO> getLeaveTypeList(){
        List<CoreShowDTO> list = new ArrayList<>();
        List<DataDictionaryEntity> selectList = this.dataDictionaryService.getSubDataByCode(SystemParameters.LEAVE);
        selectList.stream().forEach(s->{
            list.add(new CoreShowDTO(s.getVl(),s.getName()));
        });
        return list;
    }

    private List<CoreShowDTO> getProjectList(FormFieldQueryDTO dto,String fieldSelectValueType){
        List<ProjectDTO> selectList =  expMainService.getProjectListWS(dto.getCurrentCompanyId(),dto.getAccountId(),fieldSelectValueType);
        List<CoreShowDTO> list = new ArrayList<>();
        selectList.stream().forEach(s->{
            list.add(new CoreShowDTO(s.getId(),s.getProjectName()));
        });
        return list;
    }


    private List<CoreShowDTO> getAuditTypeList(FormFieldQueryDTO dto,String fieldSelectValueType){
        List<CoreShowDTO> list = new ArrayList<>();
        QueryAuditDTO query = new QueryAuditDTO();
        query.setCompanyId(dto.getCurrentCompanyId());
        query.setExpTypes(fieldSelectValueType);
        List<AuditCommonDTO> auditList = expMainDao.getAuditDataForWeb(query);
        auditList.stream().forEach(audit->{
            list.add(new CoreShowDTO(audit.getId(),audit.getUserName()+"的"+audit.getExpTypeName()+"，单号："+audit.getExpNo()));
        });
        return list;
    }


    private List<CoreShowDTO> getDynamicFormFieldSelectedList(String fieldId){
        List<CoreShowDTO> list = new ArrayList<>();
        List<DynamicFormFieldSelectedValueDTO>  selectList = dynamicFormService.listOptional(fieldId);
        selectList.stream().forEach(s->{
            if(s.getSelectableValue()==null){
                s.setSelectableValue(s.getSelectableName());
            }
            list.add(new CoreShowDTO(s.getSelectableValue(),s.getSelectableName()));
        });
        return list;
    }

    private boolean isNeedSelectValue(DynamicFormFieldBaseDTO field){
        if (field.getFieldType() == 6 || field.getFieldType() == 7 || field.getFieldType() == 8 ){
            return true;
        }
        return false;
    }
}
