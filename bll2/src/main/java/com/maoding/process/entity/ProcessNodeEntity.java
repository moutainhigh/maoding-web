package com.maoding.process.entity;

import com.maoding.core.base.entity.BaseEntity;

public class ProcessNodeEntity extends BaseEntity{

    private String processId;//所属流程id

    private String nodeName;//节点的名称

    private String nodeValue;//节点的值

    private String description;//描述

    private String relationContent;//节点关联的内容

    private Integer nodeType; //节点类型

    private Integer isAllAgree;//是否全部同意

    private Integer seq;

    private Integer deleted;//删除标识：0：有效，1：无效

    /** 项目收支流程 **/
    private Integer nodeStatus1;//应收状态
    private Integer nodeStatus2;//到账/付款状态
    private Integer nodeStatus3;//同步状态

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId == null ? null : processId.trim();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName == null ? null : nodeName.trim();
    }

    public String getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Integer getNodeType() {
        return nodeType;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    public Integer getIsAllAgree() {
        return isAllAgree;
    }

    public void setIsAllAgree(Integer isAllAgree) {
        this.isAllAgree = isAllAgree;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getNodeStatus1() {
        return nodeStatus1;
    }

    public void setNodeStatus1(Integer nodeStatus1) {
        this.nodeStatus1 = nodeStatus1;
    }

    public Integer getNodeStatus2() {
        return nodeStatus2;
    }

    public void setNodeStatus2(Integer nodeStatus2) {
        this.nodeStatus2 = nodeStatus2;
    }

    public Integer getNodeStatus3() {
        return nodeStatus3;
    }

    public void setNodeStatus3(Integer nodeStatus3) {
        this.nodeStatus3 = nodeStatus3;
    }

    public String getRelationContent() {
        return relationContent;
    }

    public void setRelationContent(String relationContent) {
        this.relationContent = relationContent;
    }
}