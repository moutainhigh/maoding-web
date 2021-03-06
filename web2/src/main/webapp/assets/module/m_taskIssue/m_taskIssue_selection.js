/**
 * 选择签发任务
 * Created by wrb on 2016/12/26.
 */
;(function ($, window, document, undefined) {

    "use strict";
    var pluginName = "m_taskIssue_selection",
        defaults = {
            $title:null,
            $isDialog:true,
            $okCallBack:null,
            $projectId:null,//项目ID
            $companyUserId:null,//员工ID
            $companyUserName:null,//员工姓名
            $isFirstSetDesign:false,//是否第一次设置设计人
            $allTaskList:null//任务列表
        };

    // The actual plugin constructor
    function Plugin(element, options) {
        this.element = element;

        this.settings = $.extend({}, defaults, options);
        this._defaults = defaults;
        this._name = pluginName;


        this.init();
    }

    // Avoid Plugin.prototype conflicts
    $.extend(Plugin.prototype, {
        init: function () {
            var that = this;
            var $data = {};
            $data.allTaskList = that.settings.$allTaskList;//response.data;
            $data.companyUserName = that.settings.$companyUserName;
            $data.isFirstSetDesign = that.settings.$isFirstSetDesign;
            var html = template('m_taskIssue/m_taskIssue_selection',$data);
            that._renderDialog(html,function () {
            });
        },
        //初始化数据
        _renderDialog:function (html,callBack) {
            var that = this;
            if(that.settings.$isDialog===true){//以弹窗编辑
                var title = that.settings.$isFirstSetDesign==true?'选择设计负责人':'设置设计负责人';
                S_layer.dialog({
                    title: that.settings.$title||title,
                    area : '750px',
                    content:html,
                    cancel:function () {
                    },
                    ok:function () {

                        that._saveTransferTaskDesigner();
                    }

                },function(layero,index,dialogEle){//加载html后触发
                    that.settings.$isDialog = index;//设置值为index,重新渲染时不重新加载弹窗
                    that.element = dialogEle;
                    if(callBack)
                        callBack();
                });

            }else{//不以弹窗编辑
                $(that.element).html(html);
                if(callBack)
                    callBack();
            }
        }
        //初始化checkbox并处理点击事件
        ,_initCheckBox:function () {
            var that = this,taskCkObj = $(that.element).find('input[name="taskCk"]'),
                allChoseCk = $(that.element).find('input[name="allChoseCk"]');

            $('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-blue',
                radioClass: 'iradio_square-blue'
            }).on('ifClicked.s', function (event) {
                var name = $(event.target).attr('name');
                if(name=='allChoseCk'){//全选或全不选触发
                    if ($(event.target).is(':checked')) {
                        taskCkObj.iCheck('uncheck');
                    }else{
                        taskCkObj.iCheck('check');
                    }
                }else{
                    //处理当taskck存在去选时，全选checkbox去选
                    var isCheckAllFalse = false;
                    taskCkObj.each(function (i) {
                        if ($(this).is(':checked')) {
                            isCheckAllFalse = true;
                            return false;
                        }
                    });
                    if(isCheckAllFalse){
                        allChoseCk.iCheck('uncheck');
                    }
                }
            });
        }

        //保存移交
        ,_saveTransferTaskDesigner:function () {
            var that = this,option  = {},$data = {};
            option.postData={};
            if(that.settings.$isFirstSetDesign){
                option.url = restApi.url_updateProjectManager;
                option.postData.type = '2';

            }else{
                option.url = restApi.url_transferTaskDesigner;
            }

            var taskList = [];
            $(that.element).find('input[name="taskCk"]:checked').each(function () {
                taskList.push($(this).val())
            });

            option.postData.projectId=that.settings.$projectId;
            option.postData.companyUserId=that.settings.$companyUserId;
            option.postData.taskList=taskList;

            m_ajax.postJson(option,function (response) {
                if(response.code=='0'){
                    S_toastr.success('操作成功！');

                    if(that.settings.$okCallBack!=null){
                        that.settings.$okCallBack();
                    }
                }else {
                    S_layer.error(response.info);
                }
            })

        }

    });

    $.fn[pluginName] = function (options) {
        return this.each(function () {

            //if (!$.data(this, "plugin_" + pluginName)) {
            $.data(this, "plugin_" +
                pluginName, new Plugin(this, options));
            //}
        });
    };

})(jQuery, window, document);


