package com.maoding.activiti.dto;


import com.maoding.core.base.dto.CoreEditDTO;

import java.util.List;

/**
 * 深圳市设计同道技术有限公司
 * @author : 张成亮
 * @date   : 2018/7/27
 * @package: DeplymentEditDTO
 * @description : 流程修改信息，用于增加流程、删除或关闭流程
 */
public class ProcessDefineDetailEditDTO extends CoreEditDTO {
    /** id: 流程key */

    /** 流程名称 */
    private String name;

    /** 流程说明 */
    private String documentation;

    /** 流程分类关键字，定义见ProcessTypeConst.PROCESS_TYPE_xxx */
    private String key;

    /** 流程类型，定义见ProcessTypeConst.TYPE_xxx */
    private Integer type;

    /** 分条件流程内的变量名称 **/
    private String varName;

    /** 分条件流程内的变量单位 **/
    private String varUnit;

    /** 条件控件编号 **/
    private String conditionFieldId;

    /** 允许启动流程的角色编号列表 */
    private List<String> candidateStarterGroups;

    /** 允许启动流程的用户编号列表 */
    private List<String> candidateStarterUsers;

    /** 允许启动流程的角色列表 */
    private List<FlowGroupDTO> candidateStarterGroupList;

    /** 允许启动流程的用户列表 */
    private List<FlowUserDTO> candidateStarterUserList;

    /** 流程需更改的任务节点序列 */
    private List<FlowTaskGroupEditDTO> flowTaskGroupList;

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getVarUnit() {
        return varUnit;
    }

    public void setVarUnit(String varUnit) {
        this.varUnit = varUnit;
    }

    public String getConditionFieldId() {
        return conditionFieldId;
    }

    public void setConditionFieldId(String conditionFieldId) {
        this.conditionFieldId = conditionFieldId;
    }

    public List<FlowTaskGroupEditDTO> getFlowTaskGroupList() {
        return flowTaskGroupList;
    }

    public void setFlowTaskGroupList(List<FlowTaskGroupEditDTO> flowTaskGroupList) {
        this.flowTaskGroupList = flowTaskGroupList;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCandidateStarterGroups() {
        return candidateStarterGroups;
    }

    public void setCandidateStarterGroups(List<String> candidateStarterGroups) {
        this.candidateStarterGroups = candidateStarterGroups;
    }

    public List<FlowGroupDTO> getCandidateStarterGroupList() {
        return candidateStarterGroupList;
    }

    public void setCandidateStarterGroupList(List<FlowGroupDTO> candidateStarterGroupList) {
        this.candidateStarterGroupList = candidateStarterGroupList;
    }

    public List<String> getCandidateStarterUsers() {
        return candidateStarterUsers;
    }

    public void setCandidateStarterUsers(List<String> candidateStarterUsers) {
        this.candidateStarterUsers = candidateStarterUsers;
    }

    public List<FlowUserDTO> getCandidateStarterUserList() {
        return candidateStarterUserList;
    }

    public void setCandidateStarterUserList(List<FlowUserDTO> candidateStarterUserList) {
        this.candidateStarterUserList = candidateStarterUserList;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
