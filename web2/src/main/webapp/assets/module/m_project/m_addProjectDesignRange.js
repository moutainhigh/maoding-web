/**
 * 设计范围
 * Created by wrb on 2016/12/20.
 */
;(function ($, window, document, undefined) {

    "use strict";
    var pluginName = "m_addProjectDesignRange",
        defaults = {
            $title:null,
            $isDialog:true,
            $projectId:'',
            $projectDesignRange:null,
            $placement:null,
            $okCallBack:null
        };

    // The actual plugin constructor
    function Plugin(element, options) {
        this.element = element;

        this.settings = $.extend({}, defaults, options);
        this._defaults = defaults;
        this._designRangeList = null;
        this._name = pluginName;
        this.init();
    }

    // Avoid Plugin.prototype conflicts
    $.extend(Plugin.prototype, {
        init: function () {
            var that = this;
            that.getRangeData(function(data){
                that.initHtmlData(data);
            });
        },
        //初始化数据
        initHtmlData:function (data) {
            var that = this;
            var $data = that.dealDesignRange();
            var $top = 'top',left = $(that.element).position().left;
            if(parseInt(left)>600){
                $top = 'left';
            }
            $(that.element).m_popover({

                placement: $top,
                content: template('m_project/m_addProjectDesignRange', $data),
                titleHtml: '<h3 class="popover-title">'+(that.settings.$title?that.settings.$title:'设计范围')+'</h3>',
                onShown: function ($popover) {

                    that.bindActionClick();
                    that.editOtherRangeValid();
                    $('.i-checks').iCheck({
                        checkboxClass: 'icheckbox_square-blue',
                        radioClass: 'iradio_square-green'
                    });
                },
                onSave: function ($popover) {

                    if ($('.designRangeBox form').valid()) {
                        that.saveEditDesignRange();
                    }else{
                        return false;
                    }

                }
            }, true);

        },
        //保存设计范围编辑
        saveEditDesignRange:function (text) {
            var that = this;
            var text = '';
            var otherRangeList = $('.otherRangeRow input[type="text"]');
            otherRangeList.each(function(){
                if($(this).val()==''||$(this).val()==null){
                    return text = '设计范围不能为空！';

                };
                if($(this).val().length>50){return text = '设计范围名称过长，请控制在50个字符内'};
            });
            // var check = $(".designRangeBox form").valid();

            if(text && text!=''){
                    return text;
                }
            else{
                if($('.popover .editable-error-block').length>0){
                    $('.popover .editable-error-block').html('');
                }
                var projectDesignRangeList = [];
                $('.designRangeBox input[type="checkbox"][name="range"]:checked').each(function () {
                    projectDesignRangeList.push({designRange:$(this).val(),id:$(this).attr('data-id')});
                });

                 $('.designRangeBox input[type="checkbox"][name="otherRange"]:checked').each(function () {
                         projectDesignRangeList.push({designRange:$(this).closest('.liBox').find('input[name="designRange"]').val()});
                 });

                 if(that.settings.$okCallBack!=null){
                     that.settings.$okCallBack(projectDesignRangeList);
                    }
                 }
                return text;

         }
        //生成html
        ,initHtmlTemplate:function (callBack,data,classIdObj) {
            var that = this;
            var html = template('m_project/m_addProjectDesignRange',data);
            classIdObj.find('div.editable-input').html(html);
            if(callBack!=null){
                callBack();
            }
            //给所有已有的自定义范围的checkbox添加事件
            $.each(data.otherRange,function(i,item){
                var obj = $('input#'+item.id).parents('.liBox');
                that.bindOtherRangeCk(obj);
            });
        }
        //把设计范围解析(哪些是来自基础数据，来自自定义)
        ,dealDesignRange:function () {
            var that = this;
            var rangeListClone = that.settings.$projectDesignRange;//已选中的设计范围
            var designRangeList = that._designRangeList;//数据字典的设计范围
            var rangeList=[];
            var otherRange=[];
            if(rangeListClone!=null && rangeListClone.length>0){
                for (var i = 0; i < rangeListClone.length; i++) {
                    var isCon = false;
                    for (var j = 0; j < designRangeList.length; j++) {
                        if (rangeListClone[i].designRange == designRangeList[j].name) {
                            isCon = true;
                            designRangeList[j].isChecked = 1;//初始化选中
                            rangeList.push(rangeListClone[i]);
                            continue;
                        }
                    }
                    if (!isCon) {
                        rangeListClone[i].isChecked = 1;//初始化选中
                        otherRange.push(rangeListClone[i]);
                    }
                }
            }
            var $data = {};
            $data.rangeList = rangeList;
            $data.otherRange = otherRange;
            $data.designRangeList = designRangeList;
            return $data;
        }
        //获取设计范围基础数据
        ,getRangeData:function (callBack) {
            var that = this;
            var option  = {};
            var rangeList = [];
            option.classId = that.element;
            option.url = restApi.url_getDesignRangeList;
            m_ajax.get(option,function (response) {
                if(response.code=='0'){
                    rangeList = response.data;
                    that._designRangeList = rangeList;
                    return callBack();
                }else {
                    S_layer.error(response.info);
                }

            })
        }
        //添加自定义设计范围事件
        ,bindAddRange:function (obj) {
            var that = this;

            var iHtml = '';
            iHtml+='<div class="col-md-4 liBox">';
            iHtml+='    <div class="col-md-2 no-padding" >';
            iHtml+='        <label class="i-checks" title="">';
            iHtml+='            <input name="otherRange" class="checkbox" checked type="checkbox"/>';
            iHtml+='            <i></i>';
            iHtml+='        </label>';
            iHtml+='    </div>';
            iHtml+='    <div class="col-md-10 out-box" style="padding-left: 0;padding-right: 0;">';
            iHtml+='        <label class="input">';
            iHtml+='            <input id="designRange" class="designRange form-control input-sm" type="text" maxlength="50" name="designRange" placeholder="请输入名称" />';
            iHtml+='        </label>';
            iHtml+='    </div>';
            iHtml+='</div>';

            obj.parent().before(iHtml);
            that.bindOtherRangeCk(obj.parent().prev());
            that.bindIcheckbox(obj.parent().prev());
            that.editOtherRangeValid();
        }
        //绑定checkbox显示
        ,bindIcheckbox:function($el){
            var that = this;
            $el.find('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-blue',
                radioClass: 'iradio_square-blue'
            });
        }
        //绑定选择自定义设计范围事件
        ,bindOtherRangeCk:function (obj) {
            obj.find('input[name="otherRange"]').on('ifUnchecked.s',function () {
                $(this).parents('.liBox').find('label.error').hide();
                $(this).parents('.liBox').remove();
                if($('.otherRangeRow div.liBox').length<1){
                    $('.popover .editable-error-block').html('');
                }
                return false;
            });
            obj.find('input[name="designRange"]').keyup(function () {
                $(this).parents('.liBox').find('label.error').hide();
                return false;
            });
        }
        //按钮事件绑定
        ,bindActionClick:function () {
            var that = this;
            $('.designRangeBox').find('label[data-action]').on('click',function () {
                var dataAction = $(this).attr('data-action');
                if(dataAction=='addOtherRange'){
                    that.bindAddRange($(this));
                    return false;
                }
            });
        }
        //自定义范围时的验证
        ,editOtherRangeValid:function(){
            $(".designRangeBox form").validate({
                rules: {
                    otherRange:{
                        ckDesignRange:true
                    },
                    designRange:{
                        ckDesignRange:true,
                        maxlengthCK:50
                    }
                },
                messages: {
                    otherRange:{
                        ckDesignRange:'请输入设计范围名称!'
                    },
                    designRange:{
                        ckDesignRange:'请输入设计范围名称!',
                        maxlengthCK:"设计范围名称不能超过50个字！"
                    }
                },
                errorPlacement: function (error, element) { //指定错误信息位置
                    /*if (element.is(':radio') || element.is(':checkbox')) {
                        error.appendTo(element.closest('.liBox'));
                    } else {
                        error.insertAfter(element);
                    }*/
                    $('.designRangeBox form').find('.error-box .col-md-12').html(error);
                }
            });
            $.validator.addMethod('ckDesignRange', function(value, element) {
                var isTrue = true;
                $('.designRangeBox form').find(' input[name="otherRange"]:checked').each(function () {
                    var val = $(this).closest('.liBox').find('input[name="designRange"]').val();
                    if($.trim(val).length===0){
                        isTrue = false;
                        return false;
                    }
                });
                if(isTrue){
                    $('.designRangeBox form').find('.error-box .col-md-12').html('');
                }
                return  isTrue;
            }, '请输入设计范围名称!');
            $.validator.addMethod('maxlengthCK', function(value, element) {
                var isTrue = true;
                $('.designRangeBox form').find(' input[name="otherRange"]:checked').each(function () {
                    var val = $(this).closest('.liBox').find('input[name="designRange"]').val();
                    if($.trim(val).length>50){
                        isTrue = false;
                        return false;
                    }
                });
                if(isTrue){
                    $('.designRangeBox form').find('.error-box .col-md-12').html('');
                }
                return  isTrue;
            }, '设计范围名称不能超过50个字');
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
