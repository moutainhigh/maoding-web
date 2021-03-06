package com.maoding.projectcost.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maoding.core.base.dto.CoreQueryDTO;

import java.util.Date;

/**
 * 深圳市卯丁技术有限公司
 * 日期: 2018/8/10
 * 类名: com.maoding.projectcost.dto.ProjectCostSummaryQueryDTO
 * 作者: 张成亮
 * 描述:
 **/
public class ProjectCostSummaryQueryDTO extends CoreQueryDTO {
    /** 所属的组织编号 */
    private String companyId;

    /** 起始日期 */
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private Date startDate;

    /** 终止日期 */
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private Date endDate;

    /** 款项类型：1-合同回款，2-技术审查费，3-合作设计费，4-其他费用 */
    private Integer costType;

    /** 收支类型：1-收款，2-付款 **/
    private Integer payType;

    /** 查询明细 */
    private String isDetail;

    /** 所属项目编号 **/
    private String projectId;

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getIsDetail() {
        return isDetail;
    }

    public void setIsDetail(String isDetail) {
        this.isDetail = isDetail;
    }

    public Integer getCostType() {
        return costType;
    }

    public void setCostType(Integer costType) {
        this.costType = costType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
