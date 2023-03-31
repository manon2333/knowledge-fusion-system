var entityType = getUrlParam('entityType'); //搜索类型
var entitySx = getUrlParam('entitySx'); //搜索属性
var sxMax = getUrlParam('sxMax'); //搜索属性最大值
var sxMin = getUrlParam('sxMin'); //搜索属性最小值
var id = getUrlParam('id'); //实体id
var adminpages = getUrlParam('adminpages') == null ? 1 : getUrlParam('adminpages') ; //后台分页
var latest = getUrlParam('latest'); //是否取最新版本的实体数据，默认为true
var version = getUrlParam('version'); //实体版本号，非必要参数。但如果 latest 为false，则这个参数必须为一个长整形数字，用于标识指定的实体副本
var zsusername = $.cookie('zsusername'); //currentUser 当前用户昵称
var zsusernameId = $.cookie('zsusernameId'); //currentUser 当前用户昵称
token = $.cookie('token');

//jquery全局配置
$.ajaxSetup({
    // dataType: "json",
    // cache: false,
    headers: {
        "token": token
        // "token": 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzIiwiZXhwIjoxNjYwODMyMTUyLCJpYXQiOjE2NjA4MjEzNTJ9.jmEde2o2DtKHQk_G66j9MkchtOSLViI5r0wgT0D'
    },
    // xhrFields: {
    //     withCredentials: true
    // },
    complete: function(xhr) {
        // token过期，则跳转到登录页面
        // console.log(xhr);
        // return false;
        if(xhr.status == 401){
            parent.location.href = '/admin/login.html';
        }
    }
});

//前台搜索按钮
layui.use(['form','layer'], function(){
    var form = layui.form;
    var layer = layui.layer;
    form.render();

    $(document).on("input propertychange", "select[lay-search] ~ div input", function () {

        if ($(this).val() == "") {
            //清空原选项值
            $(this).val('');
            //清除layui选中状态
            // $(this).parent().next().find(".layui-this").addClass(" layui-hide").removeClass("layui-this");
            // console.log();
            $('.layui-select-tips').click()
            // $(this).click();
            //form.render('select');
        }
    });


    //根据实体类型 查找实体属性
    form.on('select(sxType)', function(data){
        // console.log(data.elem); //得到checkbox原始DOM对象
        console.log(data.value); //复选框value值，也可以通过data.elem.value得到
        // console.log(data.othis); //得到美化后的DOM对象
        sxMax = '';
        sxMin = '';
        entitySx = '';
        $('input[name=sxMin]').val(sxMin);
        $('input[name=sxMax]').val(sxMax);
        getStSxFn(data.value);
    });

    //搜索 监听提交
    form.on('submit(formDemo)', function(res){
        // console.log(res.field);
        var data = res.field;
        if(data.sxMax != '' && data.sxMin != ''){
            if(parseFloat(data.sxMax) < parseFloat(data.sxMin)){
                layer.msg('您的最大值需大于等于最小值');
                return false;
            }
        }
        window.location = '/home/list.html?entityType=' + encodeURI(data.entityType) + '&keyword=' + encodeURI(data.keyword) + '&entitySx=' + encodeURI(data.entitySx) + '&sxMin=' + data.sxMin + '&sxMax=' + data.sxMax;
        return false;

    });

    /*
        历史版本弹出层
       */
    $('.lsbb').on('click',function(){
        layer.open({
            id:'lsbb',
            type: 2,
            title:'编辑历史',
            area: ['1000px', '550px'],
            content: './menuOpen/lsbb.html', //这里content是一个URL，如果你不想让iframe出现滚动条，你还可以content: ['http://sentsin.com', 'no']
            success: function(layero, index){
                var body = layer.getChildFrame('body', index);
                var iframeWin = window[layero.find('iframe')[0]['name']]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
                iframeWin.child(entityType,id);
            }
        });
    });

});
//获取地址栏参数
function getUrlParam(name) {
    //构造一个含有目标参数的正则表达式对象
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    //匹配目标参数
    var r = window.location.search.substr(1).match(reg);
    //返回参数
    if (r != null) {
        // return unescape(r[2]);
        return decodeURI(r[2]);
    } else {
        return null;
    }
}
//时间戳 转 字符串 1995-12-05 00:00:00
function getTime(str){
    if(str == ''){
        return '';
    }
    var oDate = new Date(str),
        oYear = oDate.getFullYear(),
        oMonth = oDate.getMonth()+1,
        oDay = oDate.getDate(),
        oHour = oDate.getHours(),
        oMin = oDate.getMinutes(),
        oSen = oDate.getSeconds(),
        // oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay) +' '+ getzf(oHour) +':'+ getzf(oMin) +':'+getzf(oSen);//最后拼接时间
        oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay) +' '+ getzf(oHour) +':'+ getzf(oMin) +':'+getzf(oSen);//最后拼接时间
    return oTime;

}
//补0操作
function getzf(num){
    if(parseInt(num) < 10){
        num = '0'+num;
    }
    return num;
}
// 英文引号 转 中文引号
function replaceDqm(str) {
    var val=str.replace(/"([^"]*)"/g ,"“$1”");
    if(val.indexOf('"')<0){
        return val;
    }
    return replaceDqm(val);
}
//详情页 url 跳转字符换拼接
function comUrl(entityType,id,latest,version){
    var str = '?id=' + id + '&latest=' + latest + '&version=' + version + '&entityType=' + entityType;
    return str;
}
//查找实体类型
function entTypeList() {
    //搜索 类型选项
    $.ajax({
        url:'/query/enttp',
        type:'get',
        //data:{type:entityType,kw:keyword,results:results,page:page},
        success:function(msg){
            if(msg.success){
                $('.xzs').html('');
                var data = JSON.parse(msg.json);
                // console.log(data);
                var str = '<option value="">不限</option>';
                for(var i=0; i<data.entityType.length; i++){
                    var seled = '';
                    if(data.entityType[i].name == entityType){
                        seled = 'selected';
                    }
                    str += '<option value="' + data.entityType[i].name + '" ' + seled + '>' + data.entityType[i].name + '</option>';
                }
                $('.xzs').append(str);
                // form.render('select');
                layui.use('form', function() {
                    var form = layui.form;
                    form.render('select');
                });
            }
        },
        error:function(err){
            console.log(err);
        }
    });
}
//根据实体类型 查找实体属性
function getStSxFn(type){

    $.ajax({
        url:'/query/getPropertyList',
        type:'get',
        data:{type:type},
        success:function(msg){
            // console.log(msg);
            if(msg.success){
                $('.sxxz').html('');
                var data = JSON.parse(msg.json);
                // console.log(data);
                var str = '<option value="">不限</option>';
                for(var i=0; i<data.propertyList.length; i++){
                    var seled = '';
                    if(data.propertyList[i] == entitySx){
                        seled = 'selected';
                    }
                    str += '<option value="' + data.propertyList[i] + '" ' + seled + '>' + data.propertyList[i] + '</option>';
                }
                $('.sxxz').append(str);
                // form.render('select');
                layui.use('form', function() {
                    var form = layui.form;
                    form.render('select');
                });
                $('#gjSx').show();
            }else{
                sxMax = '';
                sxMin = '';
                entitySx = '';
                $('#gjSx').hide();
            }
        },
        error:function(err){
            console.log(err);
        }
    });
}

// 关联实体 遍历渲染 方法
function showGlst(data){
    // console.log(data);
    if(data.brothersEntityListA){
        $('.queList1').show();
        var str2 = '';
        var dtl2 = data.brothersEntityListA;
        $('.queList1 .entType').text(dtl2[0].entityType);
        for(var i=0; i<dtl2.length; i++){
            var imgSrc = dtl2[i].picture ? dtl2[i].picture : '/images/img404.png' ;
            str2 += '<li title="' + dtl2[i].name + '"><a href="/home/content.html?id=' + dtl2[i].id + '&latest=' + dtl2[i].latest + '&version=' + dtl2[i].version + '&entityType=' + dtl2[i].entityType + '" target="_blank">' +
                '<span class="imgbk"><img src="' + imgSrc + '" alt="' + dtl2[i].name + '"></span>' +
                '<h6>' + dtl2[i].name + '</h6>' +
                '</a></li>';
        }
        $('.queList1 .cbl_ul').html(str2);
    }else{
        $('.queList1').hide();
    }

    if(data.brothersEntityListB){
        $('.queList2').show();
        var str3 = '';
        var dtl3 = data.brothersEntityListB;
        $('.queList2 .entType').text(dtl3[0].entityType);
        for(var i=0; i<dtl3.length; i++){
            var imgSrc = dtl3[i].picture ? dtl3[i].picture : '/images/img404.png' ;
            str3 += '<li title="' + dtl3[i].name + '"><a href="/home/content.html?id=' + dtl3[i].id + '&latest=' + dtl3[i].latest + '&version=' + dtl3[i].version + '&entityType=' + dtl3[i].entityType + '" target="_blank">' +
                '<span class="imgbk"><img src="' + imgSrc + '" alt="' + dtl3[i].name + '"></span>' +
                '<h6>' + dtl3[i].name + '</h6>' +
                '</a></li>';
        }
        $('.queList2 .cbl_ul').html(str3);
    }else{
        $('.queList2').hide();
    }
}
//详情页 实体详情  图模型 树模型 跳转方法
function tz(str){

    var u = '?id=' + id + '&latest=' + latest + '&version=' + version + '&entityType=' + encodeURI(entityType);
    var url = '';
    if(str == 'st'){
        url = "/home/content.html" + u;
    }else if(str == 't'){
        url = "/home/content_02.html" + u;
    }else if(str == 's'){
        url = "/home/content_03.html" + u;
    }
    console.log(url);
    location.href = url;
}

//详情页 获取实体属性
function getSt(entityType,id,latest,version){

    $.ajax({
        url:'/query/entid',
        type:'post',
        data:{entityType:entityType,id:id,latest:latest,version:version},
        success:function(msg){
            if(msg.success){
                var data = JSON.parse(msg.json);
                // console.log(data);
                if(!data.entity.verified){
                    $('.xd').text('未修订').removeClass('fa');
                }else{
                    $('.xd').text('已修订').addClass('fa');
                }

                //标题 及 属性标志
                $('.sub h3').html('<b class="logo-01">' + data.entity.entityType.charAt(data.entity.entityType.length-1) + '</b>' + data.entity.name);
                //摘要
                $('.sub .content').text(data.entity.description);

                var properties = data.entity.properties;
                var relations = data.entity.relations;
                var imgDt = data.entity.picture ? data.entity.picture : '/images/img404.png' ;
                $('.imgDt').prop('src',imgDt);
                var str1 = '';

                for(var i=0; i<properties.length; i++){

                    var rg = '';
                    var rgName = '';
                    if(properties[i].src.prefix == '人工校对' || properties[i].src.prefix == '人工校对_海'){
                        rg = 'rg';
                        rgName = '人工校对';
                    }else{
                        rg = '';
                        rgName = properties[i].src.prefix;
                    }

                    str1 += '<li>' +
                        '  <span class="lis1">' + properties[i].name + '</span>' +
                        '  <span class="lis2">' + properties[i].value + '</span>' +
                        // '  <span class="lis3"><a href="javascript:;" class="' + rg + '">' + rgName + '</a></span>' +
                        '  <span class="lis3"><a href="javascript:;">' + rgName + '</a></span>' +
                        '</li>';
                }

                $('.properties').html(str1);

                var str2 = '';
                for(var i=0; i<relations.length; i++){

                    var rg = '';
                    var rgName = '';
                    if(relations[i].dataSource.prefix == '人工校对' || relations[i].dataSource.prefix == '人工校对_海'){
                        rg = 'rg';
                        rgName = '人工校对';
                    }else{
                        rg = '';
                        rgName = relations[i].dataSource.prefix;
                    }

                    str2 += '<li>' +
                        '  <span class="lis1rec">' + relations[i].srcName + '</span>' +
                        '  <span class="lis2re">' + relations[i].relType + '</span>' +
                        '  <span class="lis2end">' + relations[i].endName + '</span>' +
                        // '  <span class="lis3"><a href="javascript:;">' + relations[i].dataSource.prefix + '</a></span>' +
                        '  <span class="lis3"><a href="javascript:;">' + rgName + '</a></span>' +
                        '</li>';
                }

                $('.relations').html(str2);

                showGlst(data);
            }else{
                layer.msg(msg.errMsg);
            }
        },
        error:function(err){
            console.log(err);
        }
    });
}

//获取随机颜色
function randomColor1(){
    var r = Math.floor(Math.random()*256);
    var g = Math.floor(Math.random()*256);
    var b = Math.floor(Math.random()*256);
    if(r < 16){
        r = "0"+r.toString(16);
    }else{
        r = r.toString(16);
    }
    if(g < 16){
        g = "0"+g.toString(16);
    }else{
        g = g.toString(16);
    }
    if(b < 16){
        b = "0"+b.toString(16);
    }else{
        b = b.toString(16);
    }
    return "#"+r+g+b;
}

//深色
var getRandomColor1 = function() {
    return '#' +
        (function(color) {
            return(color += '0123401234abcabc' [Math.floor(Math.random() * 16)]) &&
            (color.length == 6) ? color : arguments.callee(color);
        })('');
};

//浅色
var getRandomColor2 = function() {
    return '#' +
        (function(color) {
            return(color += '5678956789defdef' [Math.floor(Math.random() * 16)]) &&
            (color.length == 6) ? color : arguments.callee(color);
        })('');
};