package com.maoding.core.constant;

import java.util.HashMap;
import java.util.Map;

public interface ProjectCostConst {

    //费用申请开始
    Integer FEE_STATUS_START = 0;
    //费用审批通过
    Integer FEE_STATUS_APPROVE = 1;
    //费用审批拒绝
    Integer FEE_STATUS_NOT_APPROVE = 2;
    //费用审批中
    Integer FEE_STATUS_APPROVE_ING = 5;
    //撤销
    Integer FEE_STATUS_REPEAL = 3;

    /**
     * 费用类型：1-合同回款，2-技术审查费，3-合作设计费，4-其他费用（付款），5-其他费用（收款）
     */
    int FEE_TYPE_CONTRACT = 1;
    int FEE_TYPE_TECHNICAL = 2;
    int FEE_TYPE_COOPERATE = 3;
    int FEE_TYPE_OUT = 4;
    int FEE_TYPE_IN = 5;
    //合作设计费收款
    int FEE_TYPE_COOPERATE_GAIN = 10;
    //合作设计费付款
    int FEE_TYPE_COOPERATE_PAY = 11;

    //收款
    int PAY_TYPE_IN = 1;
    //付款
    int PAY_TYPE_OUT = 2;

    Map<String,String> COST_TYPE_MAP = new HashMap<String,String>(){
        {
            put("1","合同回款");
            put("2","技术审查费");
            put("3","合作设计费");
            put("4","其他付款");
            put("5","其他收款");
        }
    };
}
